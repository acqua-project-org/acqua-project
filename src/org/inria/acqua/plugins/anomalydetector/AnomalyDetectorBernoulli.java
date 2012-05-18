
package org.inria.acqua.plugins.anomalydetector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.inria.acqua.exceptions.IllegalComparisonException;
import org.inria.acqua.exceptions.NotEnoughDataException;
import org.inria.acqua.exceptions.PipelineException;
import org.inria.acqua.exceptions.UnsupportedCommandException;
import org.inria.acqua.misc.CircularBuffer;
import org.inria.acqua.misc.Landmark;
import org.inria.acqua.misc.MiscMatlab;
import org.inria.acqua.misc.Timestamp;
import org.inria.acqua.mjmisc.Misc;
import org.inria.acqua.plugins.FlowElement;
import org.inria.acqua.plugins.PipDefs;
import org.inria.acqua.plugins.Pipelineable;



/* Anomaly detector for ONE LANDMARK. */
public class AnomalyDetectorBernoulli implements Pipelineable{
	private static Logger logger = Logger.getLogger(AnomalyDetectorBernoulli.class.getName()); 
    public static final String EPSILON_KEY = "epsilon-key";
    public static final String LOWER_INDEX_KEY = "lower-index-key";
    public static final String UPPER_INDEX_KEY = "upper-index-key";
    
    private static final boolean DUMP_REGISTRY = true;
    public static final String COMMAND_SNAPSHOT = "AnomalyDetector-snapshot";
    public static final String COMMAND_GET_ANOMALY_VECTOR = "AnomalyDetector-getanomalyvector";
    public static final String COMMAND_GET_RTT_VECTOR = "AnomalyDetector-getrttvector";

    private ArrayList<Pipelineable> sinks;
    private Landmark landmark;
    private int lastAnomaly = 0;

    private CircularBuffer<Double> rttAvgHistoryTraining; /* Updated with every sample. (very long) */
    private CircularBuffer<Double> rttAvgHistoryMedianFilter; /* Updated with every sample. (very short) */
    private CircularBuffer<Integer> rttAvgHistoryRareness; /* Updated with every prob. matching result. */

    private double alphaOfSuccess; /* 'alphaOfSuccess' is defined as the fact that a sample is judged as normal. */
    private double upperProbability;
    private double lowerProbability;
    private double dcReferenceOfTraining;
    private int medianFilterNumber;
    private double desiredEpsilon;
    private double optimalEpsilon;
    private double lowerIndex;
    private double upperIndex;
    private int myCounter = 0;

    private static final int detectionWindowLength = 4;
    private boolean normalProfileReady = false;

    

    public AnomalyDetectorBernoulli(Landmark landmark, HashMap<String, Object> parameters){
        sinks = new ArrayList<Pipelineable>();
        this.landmark = landmark;

        try{
            desiredEpsilon = (Double)parameters.get(EPSILON_KEY);
            lowerIndex = (Double)parameters.get(LOWER_INDEX_KEY);
            upperIndex = (Double)parameters.get(UPPER_INDEX_KEY);
        }catch(NullPointerException e){
            throw new IllegalArgumentException("Some needed parameters of AnomalyDetectorBernoulli were not given.");
        }

//        lowerIndex = 0.3;
//        upperIndex = 0.9;

        rttAvgHistoryRareness = new CircularBuffer<Integer>(detectionWindowLength); /* Fixed. */
        rttAvgHistoryMedianFilter = new CircularBuffer<Double>(1); /* Initial length. */
        rttAvgHistoryTraining = new CircularBuffer<Double>(60*24);


        
        
        //lowerIndex = 0.6;
        //upperIndex = 0.9;

        
    }


    public void addAsSink(Pipelineable p) {
        sinks.add(p);
    }

    public synchronized void insertFlowElement(FlowElement fe, String signature) throws Exception {
        double currShift;
        int currAnomalyStatus;

        if (!PipDefs.SIGN_PINGGEN.equals(signature)){
            throw new PipelineException("Expected ping generator FlowElement.");
        }

        //int T_ping_ms = ((Integer)fe.get(PipDefs.FE_T_PING_MS)).intValue();
        int T_camp_ms = ((Integer)fe.get(PipDefs.FE_T_CAMP_MS)).intValue();
        //updateHistoriesSizes(T_camp_ms);

        double currRTT = obtainGeneralCurrentRTT(fe);


        rttAvgHistoryTraining.insert(currRTT); /* We update both circular buffers. */
        rttAvgHistoryMedianFilter.insert(currRTT); /* Negative values will be kept but ignored. */
        
        boolean landReachable = (currRTT>=0);

        double probability = -1;

        if (landReachable){
            if (normalProfileReady == true){ /* Already have a normal profile. */
                /* Normal profile and reachable */
                ArrayList<Double> medianFilterBufferValidSamples =
                        this.getOnlyValidSamples(rttAvgHistoryMedianFilter.getNewArrayList());

                //print("\tMedian buffer (only valid RTTs/"+rttAvgHistoryMedianFilter.getSize()+"): "); MiscMatlab.printArray(medianFilterBuffer);

                /* We get the median from the short-window. */
                /* Since it is reachable, there should be at least one value greater than zero in 'medianFilterBuffer'. */
                double medianRTTValue = MiscMatlab.getMedian(medianFilterBufferValidSamples);

                //print("\tIs this filtered RTT rare? " + (dcReferenceOfTraining - optimalEpsilon) + " < " + medianRTTValue + " < " + (dcReferenceOfTraining + optimalEpsilon) + "?" );
                /* Depending on the median value, we declare it rare sample or not. */
                if ((medianRTTValue < dcReferenceOfTraining - optimalEpsilon)
                        || (medianRTTValue > dcReferenceOfTraining + optimalEpsilon)){
                    rttAvgHistoryRareness.insert(1); /* Rare sample. */
                    //print("YES, IT IS RARE!");
                }else{
                    rttAvgHistoryRareness.insert(0); /* Non-rare sample. */
                    //print("NO");
                }

                /* Done the */
                /**/
                /**/
                int raresamples = getOnlyRareElements(rttAvgHistoryRareness.getNewArrayList()).size();
                //print("\tSo WE have " + raresamples + " rare samples/"+detectionWindowLength+" samples.");

                int ksuccess = detectionWindowLength - raresamples;

                probability = MiscMatlab.binomialFunction(ksuccess, detectionWindowLength, alphaOfSuccess);

                //print("\tIt happens with probability=" + probability + " (minimum=" + this.lowerProbability + " and maximum="+this.upperProbability+")");
                if (lastAnomaly != GeneralAnomalyDetector.ST_NORMAL){ /* Last time an anomaly was declared. */
                    if (probability >= this.upperProbability){
                        currAnomalyStatus = GeneralAnomalyDetector.ST_NORMAL; //print("\tSo NOW all is normal.");
                    }else{
                        currAnomalyStatus = GeneralAnomalyDetector.ST_ABNORMAL_SHIFT; //print("\tSO STILL ANOMALY!!!!!!!!<<<<<<<<");
                    }
                }else{ /* Last time NO anomaly was declared. */
                    if (probability <= this.lowerProbability){
                        currAnomalyStatus = GeneralAnomalyDetector.ST_ABNORMAL_SHIFT; //print("\tSO NOW ANOMALY!!!!!!!!<<<<<<<<");
                    }else{
                        currAnomalyStatus = GeneralAnomalyDetector.ST_NORMAL; //print("\tSo STILL all is normal.");
                    }
                }

                if (currAnomalyStatus == GeneralAnomalyDetector.ST_NORMAL){
                    currShift = 0.0;
                }else{
                    currShift = currRTT - this.dcReferenceOfTraining;
                }
            }else{
                /* No normal profile and reachable. */
                currAnomalyStatus = GeneralAnomalyDetector.ST_NORMAL;
                currShift = 0.0;
                rttAvgHistoryRareness.insert(0); /* Normal. */
            }
        }else{
            /* Non reachable. */
            currAnomalyStatus = GeneralAnomalyDetector.ST_ABNORMAL_UNREACHABLE;
            currShift = 0.0;
            rttAvgHistoryRareness.insert(1); /* Rare sample. */
        }

        lastAnomaly = currAnomalyStatus;

        //MiscMatlab.print("\tRareness array now:");
        //MiscMatlab.printArray(rttAvgHistoryRareness.getNewArrayList());

        /* Meanings:
         * UNINITIALIZED ->
         * NORMAL -> RTT
         * ABNORMAL_SHIFT -> RTT SHIFT
         * ABNORMAL_UNREACHABLE ->
         */

        FlowElement output = new FlowElement();



        if (DUMP_REGISTRY){
            String toDump;
            int raresamples = getOnlyRareElements(rttAvgHistoryRareness.getNewArrayList()).size();
            double minn = dcReferenceOfTraining - optimalEpsilon;
            double maxx = dcReferenceOfTraining + optimalEpsilon;
            toDump =  String.format(Locale.ENGLISH,"%d\t%d\t%f\t%f\t%f\t%d\t%d\t%f\t%f\t%f\n",
                    myCounter, currAnomalyStatus, minn, currRTT, maxx, raresamples, detectionWindowLength,
                    lowerProbability, probability, upperProbability);
            Misc.appendToFile("landmark" + landmark.toString() + ".logger", toDump);

        }

        
        output.put(PipDefs.FE_THIS_LANDMARK_ANOMALY, new Integer(lastAnomaly));     /* Depending on its value, the others are meaningful or not. */
        output.put(PipDefs.FE_THIS_LANDMARK_SHIFT, new Float(currShift));
        output.put(PipDefs.FE_THIS_LANDMARK_RTT, new Float(currRTT));
        output.put(PipDefs.FE_RELATED_LANDMARK, this.landmark);

        output.put(PipDefs.FE_CALCULATED_SIGNIFICANCE_LEVEL_MINI, 1 - Math.pow((1.0-this.alphaOfSuccess),detectionWindowLength));
        
        if (!sinks.isEmpty()){
            for(Pipelineable sink:sinks){
                sink.insertFlowElement(output,PipDefs.SIGN_ANDETLAN+this.landmark);
            }
        }else{
            System.err.println("There is no sink connected.");
        }

        checkIfWeShouldTakeASnapshot();        
    }

    public Landmark getLandmark() {
        return landmark;
    }
    
    public ArrayList<Object> sendCommand(String command, ArrayList<Object> args) throws UnsupportedCommandException {
        if (COMMAND_SNAPSHOT.equals(command)){
            try {
                takeSnapshotAsNormalNow();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        }else{
            throw new UnsupportedCommandException("");
        }
    }

    /*******************************/
    /*** PING EXTRACTION METHODS ***/
    /*******************************/


    private float obtainParticularCurrentRTT(Timestamp[][] timestamps, Integer count) throws NotEnoughDataException{
        float rtt_ms_acum = 0;
        int valid_ones = 0;
        for (int i=1; i<count; i++){ /* We discard the first ping because it is impacted by ARP chaches refresh. */
            try{
                float rtt_ms = (float)Timestamp.getDifferenceInMS(timestamps[i][1], timestamps[i][0]);
                rtt_ms_acum+=rtt_ms;
                valid_ones++;
            }catch(IllegalComparisonException e){
                logger.warn("Avoiding timed out stamp.\n");
            }
        }
        logger.info("Valid RTTs:" + valid_ones + " (Avg:"+rtt_ms_acum+")");
        if (valid_ones==0){
            throw new NotEnoughDataException("ERROR: There are no valid RTT (landmark '"+landmark+"', all timed out).");
        }
        rtt_ms_acum = rtt_ms_acum/(valid_ones); /* This is the new value obtained. */
        return rtt_ms_acum;
    }



    private float obtainGeneralCurrentRTT(FlowElement fe) throws Exception{
        
        int landmarkIndex = getLandmarkIndex(fe, this.landmark);
        
        Timestamp[][][] timestamp_pairs = (Timestamp[][][]) fe.get(PipDefs.FE_TIMESTAMP_PAIRS);
        Timestamp[][] ts_landmark = timestamp_pairs[landmarkIndex];
        Integer pings_count = (Integer)fe.get(PipDefs.FE_COUNT);

        if (pings_count!=ts_landmark.length){
            logger.warn("ERROR: The amount of pings measured is not equal to count (landmark " + this.landmark + " index " + landmarkIndex + ").");
            //MyLog.logWithDump(fe, "Dump of fe.");
            //MyLog.logWithDump(timestamp_pairs, "Dump of timestamp_pairs.");
            throw new Exception("ERROR: The amount of pings measured ("+ts_landmark.length+") is not equal to the given by 'count' ("+pings_count+").");
        }

        if (pings_count<2){
            throw new IllegalArgumentException("The attribute 'count' cannot be less than 2 (is " + pings_count + ").");
        }

        float currRTT;

        logger.info("Getting current RTT ("+this.landmark.getDescriptiveName()+")...");
        try{
            currRTT = obtainParticularCurrentRTT(ts_landmark, pings_count); /* Current RTT. */
        }catch(NotEnoughDataException e){
	        logger.warn("All failed...TT ");
            currRTT = -1.0f; /* Return negative value if all attempts of pings failed. */
        }
        return currRTT;
    }



    /*******************************/
    /*********   SNAPSHOT   ********/
    /*******************************/

    private synchronized void takeSnapshotAsNormalNow() throws Exception{

        print("SNAPSHOOOOOOOT");
        //MiscMatlab.addTestValues(rttAvgHistoryTraining);
        
        
        //ArrayList<Double> resMedianFilter = MiscMatlab.medianFilter(rttAvgHistory.getArrayList(), 5);
        //double std = MiscMatlab.getStd(resMedianFilter);
        
        

        ArrayList<Double> validHistoryRTT = 
                MiscMatlab.removeNegatives(rttAvgHistoryTraining.getNewArrayList()); // *1

        //print("valid rtts:");
        //MiscMatlab.printArray(validHistoryRTT);

        if (validHistoryRTT.size()>0){
            double variabilityIndex = MiscMatlab.getVariabilityIndex(validHistoryRTT, 5);
            print("variabilityIndex= " + variabilityIndex);
            medianFilterNumber = Math.max((int)Math.ceil(variabilityIndex)/4,1);

            print("Trying to filter with ntaps: '" + medianFilterNumber + "' the following array: ");
            MiscMatlab.printArray(validHistoryRTT);

            ArrayList<Double> trainingFiltered = MiscMatlab.medianFilter(validHistoryRTT, medianFilterNumber);

            dcReferenceOfTraining = MiscMatlab.getMedian(trainingFiltered);

            Object[] result = establishBetterEpsilon(trainingFiltered, dcReferenceOfTraining, desiredEpsilon);

            optimalEpsilon = (Double)result[0];
            alphaOfSuccess = (Double)result[1];

            setLowerANdUpperProbabilities(detectionWindowLength, alphaOfSuccess, lowerIndex, upperIndex);

            rttAvgHistoryMedianFilter = new CircularBuffer<Double>(medianFilterNumber); // MedianFilter

            normalProfileReady = true;
        }else{
            logger.warn("Not enough valid samples to make a profile [AnomDB for '" + landmark.getDescriptiveName() + "'].");
        }
    }

    private void checkIfWeShouldTakeASnapshot() throws Exception{
        //if (myCounter % (2*60) == 0){
        //    print("[AnomalyDetectorRawHi...] Counter " + myCounter);
        //}
        myCounter++;
//        if (/*(boundsUninitialized == true) && */(myCounter == 20)){ /* 10 minutes of samples. */
//            System.out.println("Delete thissS!!!!!!!");
//            this.takeSnapshotAsNormalNow();
//        }
//        if (/*(boundsUninitialized == true) && */(myCounter == 60)){ /* One hour of samples. */
//            this.takeSnapshotAsNormalNow();
//        }
        if (/*(boundsUninitialized == true) && */(myCounter == 3*60)){ /* 3 hours of samples. */
            //this.takeSnapshotAsNormalNow();
        }
        if (/*(boundsUninitialized == true) && */(myCounter == 12*60)){ /* One day of samples. */
            //this.takeSnapshotAsNormalNow();
        }
        if (/*(boundsUninitialized == true) && */(myCounter == 24*60)){ /* One day of samples. */
            //this.takeSnapshotAsNormalNow();
        }
        if (/*(boundsUninitialized == true) && */(myCounter == 48*60)){ /* One day of samples. */
            //this.takeSnapshotAsNormalNow();
        }
        if (/*(boundsUninitialized == true) && */(myCounter == 500)){ /* One day of samples. */
            this.takeSnapshotAsNormalNow();
        }
        if (/*(boundsUninitialized == true) && */(myCounter == 200)){ /* One day of samples. */
            //this.takeSnapshotAsNormalNow();
        }

        if (/*(boundsUninitialized == true) && */(myCounter == 800)){ /* One day of samples. */
            //this.takeSnapshotAsNormalNow();
        }

//        if (/*(boundsUninitialized == true) && */(myCounter == 800)){ /* X minutes of samples. */
//            this.takeSnapshotAsNormalNow();
//        }
    }

    /*******************************/
    /*****   AUXILIAR METHODS  *****/
    /*******************************/

    private ArrayList<Integer> getOnlyRareElements(ArrayList<Integer> samples){
        return MiscMatlab.getThoseGreaterThan(samples, 0.5); /* Rare elements are mentioned with 1. */
    }

    private ArrayList<Double> getOnlyValidSamples(ArrayList<Double> samples){
        return MiscMatlab.removeNegatives(samples);
    }

    private Object[] establishBetterEpsilon(ArrayList<Double> training, double reference, double epsilon){
        double alpha = -50.0;
        double epsilon_aux = epsilon;

        for (int i=0; i<100; i++){
            int validones = getOnlyValidSamples(training).size();
            int biggers = MiscMatlab.getThoseGreaterThan(training, reference + epsilon_aux).size();
            int lesse = MiscMatlab.getThoseLessThan(training, reference - epsilon_aux).size();

            alpha = ((double)(biggers+lesse)) / validones;

            if (alpha<0.2){
                break;
            }
            epsilon_aux = 1.1 * epsilon_aux;
            
        }
        print("Proposed epsilon: '" + epsilon + "', selected: '" + epsilon_aux + "', alpharare: '" + alpha + "'.");
        epsilon = epsilon_aux;
        double alphasuccess = 1-alpha;
        Object[] result = {epsilon, alphasuccess};
        return result;
        
    }


    private void setLowerANdUpperProbabilities(int N, double alphasuccess, double lowerIndex, double upperIndex){
        
        ArrayList<Double> binomDistribution = new ArrayList<Double>(N+1);
        for (int k=0;k<N+1; k++){
            binomDistribution.add(k, MiscMatlab.binomialFunction(k, N, alphasuccess));
        }

        Collections.sort(binomDistribution);

        print("Probabilities: ");
        MiscMatlab.printArray(binomDistribution);
        lowerProbability = mapInIndex(lowerIndex, binomDistribution);
        upperProbability = mapInIndex(upperIndex, binomDistribution);
        print("Selected " + lowerProbability + " and " + upperProbability);
    }


    private static double mapInIndex(double mindex, ArrayList<Double> domain){
        double output;
        if (mindex==1){
            output = MiscMatlab.getMax(domain) + 1;
        }else if (mindex==0){
            output = MiscMatlab.getMin(domain) - 1;
        }else{
            int index;
            index = (int)Math.ceil((domain.size()-1)*mindex) - 1;
            output = (domain.get(index) + domain.get(index+1))/2;
        }
        return output;
    }

    
    private void print(String pr){
        System.out.println(pr);
    }


    public static int getLandmarkIndex(FlowElement fe, Landmark landm) throws Exception{
        ArrayList<Landmark> landmark_list = (ArrayList<Landmark>) fe.get(PipDefs.FE_LANDMARKS_LIST);
        int i=0;
        for(Landmark lanm: landmark_list){
            if (lanm.equals(landm)){ /* We have found our landmark's index. */
                return i;
            }
            i++;
        }
        
        /* Error. The landmark was not found. Now we show which are the available landmarks. */
        String str = "";
        for(Landmark lanm: landmark_list){
            str = str + lanm + " ";
        }
        throw new Exception("Landmark '"+landm+"' is not present in the FlowElement. The availables are: '" + str + "'.");
    }


    public static int getLandmarkGatewayIndex(FlowElement fe) throws Exception{
        ArrayList<Landmark> landmark_list = (ArrayList<Landmark>) fe.get(PipDefs.FE_LANDMARKS_LIST);
        int i=0;
        for(Landmark lanm: landmark_list){
            if (lanm.getType()==Landmark.TYPE_KEYWORD_GW){ /* We have found our landmark's index. */
                return i;
            }
            i++;
        }

        /* Error. The landmark was not found. Now we show which are the available landmarks. */
        String str = "";
        for(Landmark lanm: landmark_list){
            str = str + lanm + " ";
        }
        throw new Exception("Landmark 'gateway' is not present in the FlowElement. The availables are: '" + str + "'.");
    }

    @Override
    public boolean equals(Object o){
        if (o instanceof Landmark){
            Landmark l = (Landmark) o;
            return (l.equals(landmark));
        }else if (o instanceof AnomalyDetectorBernoulli){
            AnomalyDetectorBernoulli ad = (AnomalyDetectorBernoulli) o;
            return(ad.getLandmark().equals(this.landmark));
        }else{
            return false;
        }
    }

}
