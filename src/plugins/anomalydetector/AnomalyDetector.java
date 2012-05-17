
package plugins.anomalydetector;

import exceptions.IllegalComparisonException;
import exceptions.NotEnoughDataException;
import exceptions.PipelineException;
import exceptions.UnsupportedCommandException;
import java.util.ArrayList;
import misc.CircularBuffer;
import misc.Landmark;
import mjmisc.log.MyLog;
import misc.Timestamp;
import plugins.FlowElement;
import plugins.PipDefs;
import plugins.Pipelineable;


/* Anomaly detector for ONE LANDMARK. */
public class AnomalyDetector implements Pipelineable{
    //private static final int SECONDS_AS_REFERENCE = 24*60*60; /* 24 HS. */
    private static final int SECONDS_AS_REFERENCE = 20;

    public static final String COMMAND_SNAPSHOT = "AnomalyDetector-snapshot";
    public static final String COMMAND_GET_ANOMALY_VECTOR = "AnomalyDetector-getanomalyvector";
    private static final boolean USE_T_STUDENT = false;
    private static final int SUCCESIVE_CONFIRMATIONS = 1;

    private int succesiveAnomalies = 0;

    private ArrayList<Pipelineable> sinks;
    private Landmark landmark;
    private int landmarkIndex = -1;

    private static final boolean useRealWindows = true;

    private CircularBuffer<Float> rttAvgHistory;
    private CircularBuffer<Integer> anomalyHistory;

    private float rttAverage = 0.0f;
    private float rttStd = 0.0f;
    private float alpha = 0.0f;

    private boolean boundsUninitialized = true;
    private int generalCounter = 0;
    private float upperBoundAbnormalyReference = Float.MAX_VALUE;
    private float lowerBoundAbnormalyReference = 0.0f;
    private float avgRTTReference = 0.0f;
    private float significanceLevel = 0.0f;

    public AnomalyDetector(Landmark landmark){        

        sinks = new ArrayList<Pipelineable>();
        this.landmark = landmark;
        
    }

    public void addAsSink(Pipelineable p) {
        sinks.add(p);
    }


    private float obtainGeneralCurrentRTT(FlowElement fe) throws Exception{
        if (this.landmarkIndex==-1){
            updateLandmarkIndex(fe);
        }
        Timestamp[][][] timestamp_pairs = (Timestamp[][][]) fe.get(PipDefs.FE_TIMESTAMP_PAIRS);
        Timestamp[][] ts_landmark = timestamp_pairs[this.landmarkIndex];
        Integer pings_count = (Integer)fe.get(PipDefs.FE_COUNT);

        if (pings_count!=ts_landmark.length){
            MyLog.log(this, "ERROR: The amount of pings measured is not equal to count (landmark " + this.landmark + " index " + this.landmarkIndex+").");
            //MyLog.logWithDump(fe, "Dump of fe.");
            //MyLog.logWithDump(timestamp_pairs, "Dump of timestamp_pairs.");
            throw new Exception("ERROR: The amount of pings measured ("+ts_landmark.length+") is not equal to the given by 'count' ("+pings_count+").");
        }

        if (pings_count<2){
            throw new IllegalArgumentException("The attribute 'count' cannot be less than 2 (is " + pings_count + ").");
        }

        float currRTT;

        try{
            currRTT = obtainParticularCurrentRTT(ts_landmark, pings_count); /* Current RTT. */
        }catch(NotEnoughDataException e){
            currRTT = -1.0f; /* Return negative value if all attempts of pings failed. */
        }
        return currRTT;
    }

    private void updateHistoriesSizes(int T_camp_ms){
        int windowSize = (int)((float)(SECONDS_AS_REFERENCE*1000)/T_camp_ms); /* Such that 24 hs. are covered. */

        if (useRealWindows==true){
            windowSize = windowSize<10?10:windowSize;
            if ((rttAvgHistory==null) || (rttAvgHistory.getSize() != windowSize)){
                rttAvgHistory = new CircularBuffer<Float>(windowSize); /* If the interval has changed we discard previos values. */
                anomalyHistory = new CircularBuffer<Integer>(windowSize);
            }
        }else{
            /* Nothing here. */
            alpha = 1.0f / windowSize;
            print("alpha = " + alpha);
        }
    }

    private synchronized void takeSnapshotAsNormalNow() throws Exception{
        float stdRTT;
        int validHistNumber;

        if (useRealWindows==false && USE_T_STUDENT==true){
            throw new Exception("Cannot use feedback window and t-student simultaneously.");
        }

        if (useRealWindows==true){
            Object[] ret;
            try{
                ret = computeAvgAndStd(rttAvgHistory, anomalyHistory);
            }catch(Exception e){
                throw new Exception("Error with Landmark " + this.landmark.getDescriptiveName() + ": " + e.getMessage());
            }

            /* We'll reach this point unless there is no valid history at all. */
            avgRTTReference = ((Float)ret[0]).floatValue();
            stdRTT = ((Float)ret[1]).floatValue();
            validHistNumber = ((Integer)ret[2]).intValue();

            MyLog.logP(this, "SNAPSHOT '%s' Valid history (rtt %f std %f valid %d)\n",this.landmark.getDescriptiveName(), (float)avgRTTReference, (float)stdRTT, validHistNumber);
        }else{
            avgRTTReference = this.rttAverage;
            stdRTT = this.rttStd;
        }

        if (USE_T_STUDENT==true){
            float z = MapperTStudentInverse.getZFromTStudent(significanceLevel, validHistNumber);
            upperBoundAbnormalyReference = (avgRTTReference+(z*stdRTT));
            lowerBoundAbnormalyReference = (avgRTTReference-(z*stdRTT));
        }else{
            float val = NormalDistrib.getInvCDF(significanceLevel + ((1.0f-significanceLevel)/2), avgRTTReference, stdRTT);
            upperBoundAbnormalyReference = val;
            lowerBoundAbnormalyReference = avgRTTReference - (upperBoundAbnormalyReference - avgRTTReference);
        }

        MyLog.appendPToLogFile(this, "\tNew values (avgRTT %f upperBound %f lowerBound %f)\n\n",
                avgRTTReference, upperBoundAbnormalyReference, lowerBoundAbnormalyReference);
    }

    public synchronized void insertFlowElement(FlowElement fe, String signature) throws Exception {
        
        float currRTT;
        int  currAnom;
        double currShift;
        boolean landReachable;


        if (!PipDefs.SIGN_PINGGEN.equals(signature)){
            throw new PipelineException("Expected ping generator FlowElement.");
        }

        MyLog.logP(this, "<<<<<<<Inserting new flow element landmark %s...\n", landmark.getDescriptiveName());
        significanceLevel = (Float)fe.get(PipDefs.FE_ANOMALY_DETECTOR_PARAM1);
        //int T_ping_ms = ((Integer)fe.get(PipDefs.FE_T_PING_MS)).intValue();
        int T_camp_ms = ((Integer)fe.get(PipDefs.FE_T_CAMP_MS)).intValue();
        updateHistoriesSizes(T_camp_ms);


        currRTT = obtainGeneralCurrentRTT(fe);
        landReachable = (currRTT>=0);

        MyLog.appendPToLogFile(this, "Landmark '%s' status: ", this.landmark.getDescriptiveName());
        if (landReachable == true){
            MyLog.appendPToLogFile(this, "\tReachable\n");
            try{
                if ((lowerBoundAbnormalyReference <= currRTT) && (currRTT <= upperBoundAbnormalyReference)){
                    currAnom = GeneralAnomalyDetector.ST_NORMAL;
                    currShift = 0.0f;
                    MyLog.appendPToLogFile(this, "\tNormal ");
                    MyLog.appendPToLogFile(this, "status %f<%f<%f\n",
                            (lowerBoundAbnormalyReference), currRTT, (upperBoundAbnormalyReference));
                    succesiveAnomalies = 0;
                    /* Everything is okay. */
                }else{
                    
                    currAnom = GeneralAnomalyDetector.ST_ABNORMAL_SHIFT;
                    currShift = currRTT - avgRTTReference;
                    MyLog.appendPToLogFile(this, "\tAbnormal (shift) status (shift %f) ", currShift);
                    MyLog.appendPToLogFile(this, "status %f<%f<%f\n",
                            (lowerBoundAbnormalyReference), currRTT, (upperBoundAbnormalyReference));
                    succesiveAnomalies++;
                    /* There is an anomaly. */
                }
            }catch(Exception e){
                /* Ups, not enough history valid. */
                MyLog.appendPToLogFile(this, "\tNOT valid history\n");
                /* Then we add this current value as normal (for next measurement). */
                currAnom = GeneralAnomalyDetector.ST_NORMAL;
                currShift = 0.0f;
                succesiveAnomalies = 0;
            }
        }else{ /* Landmark was not reached in this attemp. No way to compare with history. */
            MyLog.appendPToLogFile(this, "\tUNreachable\n");
            currRTT = 0.0f;
            currAnom = GeneralAnomalyDetector.ST_ABNORMAL_UNREACHABLE;
            currShift = 0.0f;
            succesiveAnomalies++;
        }

        if (useRealWindows==true){
            rttAvgHistory.insert(currRTT);
            anomalyHistory.insert(currAnom);
        }else{
            if (includeMeasInAvgStd(currAnom)==true){
                rttStd = alpha * Math.abs(rttAverage-currRTT) + (1.0f-alpha)*this.rttStd;
                rttAverage = alpha * currRTT  + (1.0f-alpha)*this.rttAverage;
                print("rtt avg " + rttAverage + " rttstd " + rttStd ); 
            }
        }

        /* Meanings:
         * UNINITIALIZED ->
         * NORMAL -> RTT
         * ABNORMAL_SHIFT -> RTT SHIFT
         * ABNORMAL_UNREACHABLE -> 
         */

        FlowElement output = new FlowElement();

        if (succesiveAnomalies<SUCCESIVE_CONFIRMATIONS && currAnom!=GeneralAnomalyDetector.ST_NORMAL){
            MyLog.appendPToLogFile(this, "Counting anomaly but not telling about it (%d°/%d).\n",
                    succesiveAnomalies, SUCCESIVE_CONFIRMATIONS);
            currAnom = GeneralAnomalyDetector.ST_NORMAL;
            currShift = 0.0f;
            /* No 'succesiveAnomalies = 0' because we keep cummulating them, 'no restart' is better. */
        }

        output.put(PipDefs.FE_THIS_LANDMARK_ANOMALY, new Integer(currAnom));     /* Depending on its value,
                                                                        the others are meaningful or not. */
        output.put(PipDefs.FE_THIS_LANDMARK_SHIFT, new Float(currShift));

        output.put(PipDefs.FE_THIS_LANDMARK_RTT, new Float(currRTT));
        
        //print("[AnomalyDetector]" + this.landmark.getDescriptiveName() + " rtt: " + currRTT);

        output.put(PipDefs.FE_RELATED_LANDMARK, this.landmark);

        if (!sinks.isEmpty()){
            for(Pipelineable sink:sinks){
                sink.insertFlowElement(output,PipDefs.SIGN_ANDETLAN+this.landmark);
            }
        }else{
            System.err.println("There is no sink connected.");
        }

        if (boundsUninitialized && generalCounter == 10){
            this.takeSnapshotAsNormalNow();
        }
        generalCounter++;

    }

    private void print(String pr){
        System.out.println(pr);
    }
    
    private boolean includeMeasInAvgStd(int anomaly){
        return ((anomaly==GeneralAnomalyDetector.ST_NORMAL) ||
                (anomaly==GeneralAnomalyDetector.ST_ABNORMAL_SHIFT));
    }

    private Object[] computeAvgAndStd(CircularBuffer<Float> avg, CircularBuffer<Integer> anom) throws Exception{
        float acumulator = 0.0f;
        int valid = 0;
        float rtt_avg;
        float rtt_std;
        for (int i=0; i<avg.getSize(); i++){
            if (includeMeasInAvgStd(anom.get(i))){
                acumulator += avg.get(i);
                valid++;
            }
        }
        if (valid==0){
            throw new Exception("Not enough valid history.");
        }

        rtt_avg = acumulator/valid;

        acumulator = 0.0f;
        for (int i=0; i<avg.getSize(); i++){
            if (includeMeasInAvgStd(anom.get(i))){
                acumulator += Math.pow(Math.abs(avg.get(i) - rtt_avg), 2.0f);
            }
        }
        rtt_std = (float) Math.sqrt(acumulator/valid);


        Object[] ret = new Object[3];
        ret[0] = new Float(rtt_avg);
        ret[1] = new Float(rtt_std);
        ret[2] = new Integer(valid);
        return ret;
    }

    private float obtainParticularCurrentRTT(Timestamp[][] timestamps, Integer count) throws NotEnoughDataException{
        float rtt_ms_acum = 0;
        int valid_ones = 0;
        for (int i=1; i<count; i++){ /* We discard the first ping because it is impacted by ARP chaches refresh. */
            try{
                float rtt_ms = (float)Timestamp.getDifferenceInMS(timestamps[i][1], timestamps[i][0]);
                rtt_ms_acum+=rtt_ms;
                valid_ones++;
            }catch(IllegalComparisonException e){
                MyLog.appendPToLogFile(this, "Avoiding timed out stamp.");
            }
        }
        MyLog.appendPToLogFile(this,  "valid ones =%d rttavg=%f\n", valid_ones, rtt_ms_acum);
        if (valid_ones==0){
            throw new NotEnoughDataException("ERROR: There are no valid RTT (landmark '"+landmark+"', all timed out).");
        }
        rtt_ms_acum = rtt_ms_acum/(valid_ones); /* This is the new value obtained. */
        return rtt_ms_acum;
    }

    public void updateLandmarkIndex(FlowElement fe) throws Exception{
        ArrayList<Landmark> landmark_list = (ArrayList<Landmark>) fe.get(PipDefs.FE_LANDMARKS_LIST);

        int i=0;
        for(Landmark lanm: landmark_list){
            if (lanm.equals(this.landmark)){ /* We have found our landmark's index. */
                landmarkIndex = i;
                return;
            }
            i++;
        }
        throw new Exception("ERROR: Landmark assigned to this AnomalyDetector "+landmark+" is not present in the measurements.");
    }

    /* Testing. */
    public static void main(String args[]){

        
    }

    public ArrayList<Object> sendCommand(String command, ArrayList<Object> args) throws UnsupportedCommandException {
        if (COMMAND_SNAPSHOT.equals(command)){
            try {
                takeSnapshotAsNormalNow();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        }else if(COMMAND_GET_ANOMALY_VECTOR.equals(command)){
            if (args==null || (args!=null && args.size()<1)){
                ArrayList<Object> ret = new ArrayList<Object>();
                ret.add(this.anomalyHistory);
                return ret;
            }else{
                Landmark l = (Landmark) args.get(0);
                if (landmark.equals(l)){
                    ArrayList<Object> ret = new ArrayList<Object>();
                    ret.add(this.anomalyHistory);
                    return ret;
                }else{
                    return null;
                }
            }
        }else{
            throw new UnsupportedCommandException("");
        }   
    }

}
