
package plugins.anomalydetector;

import exceptions.IllegalComparisonException;
import exceptions.NotEnoughDataException;
import exceptions.PipelineException;
import exceptions.UnsupportedCommandException;
import java.util.ArrayList;
import java.util.Locale;
import misc.CircularBuffer;
import misc.Landmark;
import mjmisc.Misc;
import mjmisc.log.MyLog;
import misc.QuickSort;
import misc.Timestamp;
import plugins.FlowElement;
import plugins.PipDefs;
import plugins.Pipelineable;


/* Anomaly detector for ONE LANDMARK. */
public class AnomalyDetectorRawHistogram implements Pipelineable{
    private static final int SECONDS_AS_REFERENCE = 24*60*60; /* 24 HS. */

    private static final boolean DUMP_REGISTRY = true;
    public static final String COMMAND_SNAPSHOT = "AnomalyDetector-snapshot";
    public static final String COMMAND_GET_ANOMALY_VECTOR = "AnomalyDetector-getanomalyvector";
    public static final String COMMAND_GET_RTT_VECTOR = "AnomalyDetector-getrttvector";

    private static final int SUCCESIVE_CONFIRMATIONS = 5;

    private int succesiveAnomalies = 0;

    private ArrayList<Pipelineable> sinks;
    private Landmark landmark;
    private int landmarkIndex = -1;


    private CircularBuffer<Float> rttAvgHistory;
    private CircularBuffer<Integer> anomalyHistory;

    private boolean boundsUninitialized = true;
    private int myCounter = 0;
    private float upperBoundAbnormalyReference = Float.MAX_VALUE;
    private float lowerBoundAbnormalyReference = 0.0f;
    private float avgRTTReference = 0.0f;
    private float significanceLevel = 0.0f;

    public AnomalyDetectorRawHistogram(Landmark landmark){        

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
        //int windowSize = (int)((float)(SECONDS_AS_REFERENCE*1000)/T_camp_ms); /* Such that 24 hs. are covered. */
        int windowSize = 24*60;
        
        windowSize = windowSize<10?10:windowSize;
        if ((rttAvgHistory==null) || (rttAvgHistory.getSize() != windowSize)){
            rttAvgHistory = new CircularBuffer<Float>(windowSize); /* If the interval has changed we discard previos values. */
            anomalyHistory = new CircularBuffer<Integer>(windowSize);
        }

    }

    private synchronized void takeSnapshotAsNormalNow() throws Exception{
        float stdRTT;
        int validHistNumber;

        Object[] ret;
        try{
            ret = computeAvgAndLimits(rttAvgHistory, anomalyHistory, significanceLevel);
        }catch(Exception e){
            throw new Exception("Error with Landmark " + this.landmark.getDescriptiveName() + ": " + e.getMessage());
        }

        /* We'll reach this point unless there is no valid history at all. */
        avgRTTReference = ((Float)ret[0]).floatValue();
        stdRTT = ((Float)ret[1]).floatValue();
        validHistNumber = ((Integer)ret[2]).intValue();
        upperBoundAbnormalyReference = ((Float)ret[3]).floatValue();
        lowerBoundAbnormalyReference = ((Float)ret[4]).floatValue();

        boundsUninitialized = false;

        MyLog.logP(this, "SNAPSHOT '%s' Valid history (rtt %f std %f valid %d)\n",this.landmark.getDescriptiveName(), (float)avgRTTReference, (float)stdRTT, validHistNumber);

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

        //MyLog.logP(this, "<<<<<<<Inserting new flow element landmark %s...\n", landmark.getDescriptiveName());
        significanceLevel = (Float)fe.get(PipDefs.FE_ANOMALY_DETECTOR_PARAM1);
        //int T_ping_ms = ((Integer)fe.get(PipDefs.FE_T_PING_MS)).intValue();
        int T_camp_ms = ((Integer)fe.get(PipDefs.FE_T_CAMP_MS)).intValue();
        updateHistoriesSizes(T_camp_ms);

        String toDump = "";
        currRTT = obtainGeneralCurrentRTT(fe);
        landReachable = (currRTT>=0);

        //MyLog.appendPToLogFile(this, "Landmark '%s' status: ", this.landmark.getDescriptiveName());
        if (landReachable == true){
            //MyLog.appendPToLogFile(this, "\tReachable\n");
            try{
                if ((lowerBoundAbnormalyReference <= currRTT) && (currRTT <= upperBoundAbnormalyReference)){
                    currAnom = GeneralAnomalyDetector.ST_NORMAL;
                    currShift = 0.0f;
                    //MyLog.appendPToLogFile(this, "\tNormal ");
                    //MyLog.appendPToLogFile(this, "status %f<%f<%f\n",
                    //        (lowerBoundAbnormalyReference), currRTT, (upperBoundAbnormalyReference));
                    if (succesiveAnomalies>SUCCESIVE_CONFIRMATIONS){
                        succesiveAnomalies = SUCCESIVE_CONFIRMATIONS - 1;
                    }else{
                        succesiveAnomalies = (succesiveAnomalies<=0)?0:succesiveAnomalies-1;
                    }
                    /* Everything is okay. */
                }else{
                    
                    currAnom = GeneralAnomalyDetector.ST_ABNORMAL_SHIFT;
                    currShift = currRTT - avgRTTReference;
//                    MyLog.appendPToLogFile(this, "\tAbnormal (shift) status (shift %f) ", currShift);
//                    MyLog.appendPToLogFile(this, "status %f<%f<%f\n",
//                            (lowerBoundAbnormalyReference), currRTT, (upperBoundAbnormalyReference));
                    succesiveAnomalies++;
                    /* There is an anomaly. */
                }
                
            }catch(Exception e){
                /* Ups, not enough history valid. */
                //MyLog.appendPToLogFile(this, "\tNOT valid history\n");
                /* Then we add this current value as normal (for next measurement). */
                currAnom = GeneralAnomalyDetector.ST_NORMAL;
                currShift = 0.0f;
                succesiveAnomalies = 0;
                
            }
        }else{ /* Landmark was not reached in this attemp. No way to compare with history. */
            //MyLog.appendPToLogFile(this, "\tUNreachable\n");
            currRTT = 0.0f;
            currAnom = GeneralAnomalyDetector.ST_ABNORMAL_UNREACHABLE;
            currShift = 0.0f;
            succesiveAnomalies++;
        }

        rttAvgHistory.insert(currRTT);
        anomalyHistory.insert(currAnom);

        /* Meanings:
         * UNINITIALIZED ->
         * NORMAL -> RTT
         * ABNORMAL_SHIFT -> RTT SHIFT
         * ABNORMAL_UNREACHABLE -> 
         */

        FlowElement output = new FlowElement();

        if (succesiveAnomalies<SUCCESIVE_CONFIRMATIONS && currAnom!=GeneralAnomalyDetector.ST_NORMAL){
            //MyLog.appendPToLogFile(this, "Counting anomaly but not telling about it (%d°/%d).\n",
            //        succesiveAnomalies, SUCCESIVE_CONFIRMATIONS);
            currAnom = GeneralAnomalyDetector.ST_NORMAL;
            currShift = 0.0f;
            /* No 'succesiveAnomalies = 0' because we keep cummulating them, 'no restart' is better. */
        }

        if (DUMP_REGISTRY){
            toDump =  String.format(Locale.ENGLISH,"%d\t%f\t%f\t%f\n", currAnom, lowerBoundAbnormalyReference, currRTT, upperBoundAbnormalyReference);
            Misc.appendToFile("landmark" + landmark.toString() + ".logger", toDump);
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

        

        if (myCounter % 500 == 0){
            print("[AnomalyDetectorRawHi...] Counter " + myCounter);
        }
        myCounter++;

        if (/*(boundsUninitialized == true) && */(myCounter == 10)){ /* 10 minutes of samples. */
            this.takeSnapshotAsNormalNow();
        }

        if (/*(boundsUninitialized == true) && */(myCounter == 60)){ /* One hour of samples. */
            this.takeSnapshotAsNormalNow();
        }

        if (/*(boundsUninitialized == true) && */(myCounter == 3*60)){ /* 3 hours of samples. */
            this.takeSnapshotAsNormalNow();
        }

        if (/*(boundsUninitialized == true) && */(myCounter == 24*60)){ /* One day of samples. */
            this.takeSnapshotAsNormalNow();
        }
        
        
        
    }

    private void print(String pr){
        System.out.println(pr);
    }
    
    private boolean includeMeasInAvgStd(Integer anomaly){
        if (anomaly!=null){
            return ((anomaly==GeneralAnomalyDetector.ST_NORMAL) ||
                    (anomaly==GeneralAnomalyDetector.ST_ABNORMAL_SHIFT));
        }else{
            return false;
        }
    }

    private Object[] computeAvgAndLimits(CircularBuffer<Float> avg, CircularBuffer<Integer> anom, float significance) throws Exception{

        /*
        ret = computeAvgAndStd(rttAvgHistory, anomalyHistory, significanceLevel);
       
        avgRTTReference = ((Float)ret[0]).floatValue();
        stdRTT = ((Float)ret[1]).floatValue();
        validHistNumber = ((Integer)ret[2]).intValue();
        upperBoundAbnormalyReference = ((Float)ret[3]).floatValue());
        lowerBoundAbnormalyReference = ((Float)ret[4]).floatValue());
         */

        ArrayList<Float> goodSamples = new ArrayList<Float>();
        int valid = 0;
        float rtt_avg;
        //float rtt_std;
        float acumulator = 0.0f;
        
        for (int i=0; i<avg.getSize(); i++){
            Integer anomaly = anom.get(i);
            if (includeMeasInAvgStd(anomaly)){
                goodSamples.add(avg.get(i));
                acumulator+=avg.get(i);
                valid++;
            }
        }

        if (goodSamples.size()==0){
            throw new Exception("Not enough valid history.");
        }
        
        rtt_avg = acumulator/goodSamples.size();
        QuickSort qs = new QuickSort();
        qs.sort(goodSamples);

        print(" VALUES ");
        for(Float f:goodSamples){
            System.out.print("  " + f);
        }
        print(" VALUES \n");


        float remanent = (1.0f-significance)/2.0f;
        int poinf = (int)((  remanent)*(float)(goodSamples.size()-1));
        int posup = (int)((1-remanent)*(float)(goodSamples.size()-1));
        float inferior = goodSamples.get(poinf);
        float superior = goodSamples.get(posup);
        print(" inferior " + inferior + " pos " + poinf);
        print(" superior " + superior + " pos " + posup);
        Object[] ret = new Object[5];
        ret[0] = rtt_avg;
        ret[1] = 0.0f;
        ret[2] = valid;
        ret[3] = superior;
        ret[4] = inferior;
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
        MyLog.appendPToLogFile(this, "valid ones =%d rttavg=%f\n", valid_ones, rtt_ms_acum);
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
            ArrayList<Object> ret = new ArrayList<Object>();

            if (args==null || (args!=null && args.size()<1)){
                ret.add(this.anomalyHistory);
                return ret;
            }else{
                Landmark l = (Landmark) args.get(0);
                if (landmark.equals(l)){
                    ret.add(this.anomalyHistory);
                    return ret;
                }else{
                    return null;
                }
            }
        }else if(COMMAND_GET_RTT_VECTOR.equals(command)){
            ArrayList<Object> ret = new ArrayList<Object>();

            if (args==null || (args!=null && args.size()<1)){
                ret.add(this.rttAvgHistory);
                return ret;
            }else{
                Landmark l = (Landmark) args.get(0);
                if (landmark.equals(l)){
                    ret.add(this.rttAvgHistory);
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
