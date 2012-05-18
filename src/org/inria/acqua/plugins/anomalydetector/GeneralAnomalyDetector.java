
package org.inria.acqua.plugins.anomalydetector;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.inria.acqua.exceptions.PipelineException;
import org.inria.acqua.exceptions.UnsupportedCommandException;
import org.inria.acqua.misc.Landmark;
import org.inria.acqua.misc.Timestamp;
import org.inria.acqua.mjmisc.Misc;
import org.inria.acqua.plugins.FlowElement;
import org.inria.acqua.plugins.PipDefs;
import org.inria.acqua.plugins.Pipelineable;


public class GeneralAnomalyDetector implements Pipelineable{
	private static Logger logger = Logger.getLogger(GeneralAnomalyDetector.class.getName()); 
    private ArrayList<Pipelineable> sinks;
    private ArrayList<Landmark> currentLandmarksList;
    private ArrayList<Pipelineable> currentAnomalyDetectors;
    private Integer inputID;
    private HashMap<Landmark, FlowElement> currentLandmarkFlowElements;
    private FlowElement baseFlowElement;
    private HashMap<String, Object> parameters;

    public static final int ST_UNINITIALIZED = 7;
    public static final int ST_NORMAL = 0;
    public static final int ST_ABNORMAL_SHIFT = 1;
    public static final int ST_ABNORMAL_UNREACHABLE = 2;
    
    public GeneralAnomalyDetector(HashMap<String, Object> parameters){
        sinks = new ArrayList<Pipelineable>();
        currentLandmarkFlowElements = new HashMap<Landmark, FlowElement>();
        this.parameters = parameters;
        currentAnomalyDetectors = new ArrayList<Pipelineable>();
    }

    public void addAsSink(Pipelineable p) {
        sinks.add(p);
    }

    private void print(String pr){
        System.out.println(pr);
    }

    public void insertFlowElement(FlowElement fe, String signature) throws Exception {
        /********************************/
        /* New real FlowElement coming. */
        /********************************/
        if (PipDefs.SIGN_PINGGEN.equals(signature)){ 
            //print("[GeneralAD] initial FlowElement.");
            if (landmarksHaveChangedThenUpdateLandmarksList(fe)==true){ /* Do we need to change the set of landmarks?
                                                            (if so this also sets 'landmarks') */
                /* Landmarks list has changed ('landmarks' has changed)... */

                /* Now we don't change all of them, we just add or remove according to the changes in 'landmarks'. */


                
//                for(Landmark l:landmarks){
//                    AnomalyDetectorBernoulli ad = new AnomalyDetectorBernoulli(l, parameters);
//                    ad.addAsSink(this);
//                    anomalyDetectors.add(ad);
//                }

                ArrayList<Landmark> currentAnomalyDetector_landmarks = new ArrayList<Landmark>();
                for(Pipelineable a: currentAnomalyDetectors){
                    AnomalyDetectorBernoulli b = (AnomalyDetectorBernoulli) a;
                    currentAnomalyDetector_landmarks.add(b.getLandmark());
                }

                AbstractCollection<Landmark> newOnes;
                AbstractCollection<Landmark> deletedOnes;

                /* currentLandmarkList is already updated, but the AnomalyDetectors are NOT updated. */
                newOnes = Misc.getDifference_ZsNotPresentInB(currentLandmarksList, currentAnomalyDetector_landmarks);
                deletedOnes = Misc.getDifference_ZsNotPresentInB(currentAnomalyDetector_landmarks, currentLandmarksList);
                


                for(Landmark l:newOnes){
                    /* We check if landmark 'l' of current set is present (if not we remove it). */
                    AnomalyDetectorBernoulli ad = new AnomalyDetectorBernoulli(l, parameters);
                    ad.addAsSink(this);
                    currentAnomalyDetectors.add(ad);
                }

                for (Landmark l:deletedOnes){
                    this.removeAnomalyDetector(l);
                    
                }
            }else{
                /* Landmarks list did not change. */
            }

            currentLandmarkFlowElements.clear();
            this.baseFlowElement = fe;

            /* Insert the incoming FlowElement in each Mini-AnomalyDetector.*/
            for(Pipelineable p:currentAnomalyDetectors){
                p.insertFlowElement(fe, signature);
            }


        /****************************************************/
        /* subFlowElement from Mini-AnomalyDetector coming. */
        /****************************************************/
        }else if(signature.startsWith(PipDefs.SIGN_ANDETLAN)){ 
            {
                Landmark landmark = (Landmark)fe.get(PipDefs.FE_RELATED_LANDMARK);

                if (currentLandmarkFlowElements.get(landmark)==null){ /* We didn't collect it yet. */
                    currentLandmarkFlowElements.put(landmark, fe);
                    logger.info("*** Inserted landmark %landmark.getDescriptiveName()s FE (" + currentLandmarkFlowElements.size() + "/" + this.currentLandmarksList.size() + " elements).");
                }else{
                    logger.warn("*** ERROR: FE landmark already received ('" + landmark.getDescriptiveName() + "') .\n");
                    throw new Exception("Landmark already received '" + landmark + "'.");
                }
            }
            if (allLandmarkOutputsCollected()==true){
                double signifLevelSummatory = 0;
                logger.info("+++ All landmarks collected!!!\n\n");
                for(Landmark landm: currentLandmarkFlowElements.keySet()){
                    FlowElement currFE = currentLandmarkFlowElements.get(landm);
                    baseFlowElement.put(PipDefs.SIGN_ANDETLAN + landm.toString(), currFE);
                    signifLevelSummatory = signifLevelSummatory + ((Double)currFE.get(PipDefs.FE_CALCULATED_SIGNIFICANCE_LEVEL_MINI));
                }
                Float calcSignificanceLevel = (float)signifLevelSummatory / currentLandmarkFlowElements.keySet().size();

                Timestamp timestamp = calculateTimestamp(baseFlowElement);
                baseFlowElement.put(PipDefs.FE_TIMESTAMP, timestamp);

                baseFlowElement.put(PipDefs.FE_CALCULATED_SIGNIFICANCE_LEVEL, calcSignificanceLevel);
                for(Pipelineable p: sinks){
                    p.insertFlowElement(baseFlowElement, PipDefs.SIGN_ANDETGEN);
                }
            }

        }else{
            throw new PipelineException("Signature '"+signature+"' not supported.");
        }
    }


    private Timestamp calculateTimestamp(FlowElement baseFlowElement){
        Timestamp[][][] timestamp_pairs = (Timestamp[][][]) baseFlowElement.get(PipDefs.FE_TIMESTAMP_PAIRS);
        return timestamp_pairs[0][0][0];
    }

    private boolean allLandmarkOutputsCollected(){
        for (Landmark l:currentLandmarksList){
            FlowElement f = currentLandmarkFlowElements.get(l);
            if (f==null){
                logger.info("+++ Remaining FlowElement of '"+l+"'");
                return false;
            }
        }
        return true;
    }
    private boolean landmarksHaveChangedThenUpdateLandmarksList(FlowElement fe){
        //Integer inputid = (Integer)fe.get(PipDefs.FE_INPUT_ID);
        if (currentLandmarksList!=null){ /* If we already have a set of landmarks... */
            ArrayList<Landmark> listcoming = (ArrayList<Landmark>)fe.get(PipDefs.FE_LANDMARKS_LIST);
            /* Equality condition: A contains all B and A is not bigger than B. */
            if (currentLandmarksList.containsAll(listcoming) && (listcoming.size()==currentLandmarksList.size())){
                return false; /* No change. */
            }else{
                /* Change. */
                this.currentLandmarksList = listcoming;
                return true;
            }


        }else{ /* If it's the first set of landmarks that come... */
            this.currentLandmarksList = (ArrayList<Landmark>)fe.get(PipDefs.FE_LANDMARKS_LIST);
            return true;
        }
//        if (this.inputID==null || inputID!=inputid){
//            inputID = inputid;
//            this.landmarks = (ArrayList<Landmark>)fe.get(PipDefs.FE_LANDMARKS_LIST);
//            return true;
//        }else{
//
//            return false;
//        }
    }

    public ArrayList<Object> sendCommand(String command, ArrayList<Object> args) throws UnsupportedCommandException {
        ArrayList<Object> retu = new ArrayList<Object>();
        for(Pipelineable ad: currentAnomalyDetectors){
            ArrayList<Object> ret = ad.sendCommand(command, args); /* If one of them doesn't support a command, all of them wont. */
            if (ret!=null){
                retu.addAll(ret);
            }
        }
        return retu;
    }

    private void removeAnomalyDetector(Landmark l){
        ArrayList<AnomalyDetectorBernoulli> toRemove = new ArrayList<AnomalyDetectorBernoulli>();
        for(Pipelineable p: currentAnomalyDetectors){
            AnomalyDetectorBernoulli ad = (AnomalyDetectorBernoulli)p;
            if (ad.getLandmark().equals(l)){
                toRemove.add(ad);
            }

        }
        
        currentAnomalyDetectors.removeAll(toRemove);
    }

}
