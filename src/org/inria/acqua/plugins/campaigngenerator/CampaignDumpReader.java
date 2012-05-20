
package org.inria.acqua.plugins.campaigngenerator;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.inria.acqua.exceptions.*;
import org.inria.acqua.misc.Landmark;
import org.inria.acqua.misc.Timestamp;
import org.inria.acqua.mjmisc.Misc;
import org.inria.acqua.mjmisc.MiscIP;
import org.inria.acqua.plugins.FlowElement;
import org.inria.acqua.plugins.PipDefs;
import org.inria.acqua.plugins.Pipelineable;
import org.inria.acqua.plugins.anomalydetector.AnomalyDetectorBernoulli;


public class CampaignDumpReader implements Pipelineable{
	private static Logger logger = Logger.getLogger(CampaignDumpReader.class.getName()); 
    private String dumpFilename;
    private ArrayList<Pipelineable> sinks;
    private Gson gson;
    private FileReader freader;
    private BufferedReader breader;
    private boolean firstFlowElement = true;
    private boolean deleteGateway = false;
    private ArrayList<Landmark> landmarksToDelete = null;

    public CampaignDumpReader(String dumpFilename){
        this.dumpFilename = dumpFilename;
        this.sinks = new ArrayList<Pipelineable>();
        gson = new Gson();
        freader = null;
        breader = null;
    }

    public void addAsSink(Pipelineable p) {
        sinks.add(p);
    }

    public void insertFlowElement(FlowElement fe, String signature) throws Exception {
        if (!PipDefs.SIGN_INPUT.equals(signature)){
            throw new PipelineException("Expected INPUT FlowElement but received " + signature + ".");
        }

        if (freader==null){
            File file = new File(dumpFilename);
            freader = new FileReader(file);
            breader = new BufferedReader(freader);
        }

        if(breader.ready()){
            String str = breader.readLine();
            JsonDumpeableFlowElement fe2 = gson.fromJson(str, JsonDumpeableFlowElement.class);
            FlowElement fe3 = fe2.dumpToFlowElement();

            
            if (firstFlowElement == true){
                
                File deleteLandmarks = new File("landmarks_to_delete.txt");
                
                if (deleteLandmarks.exists()){
                    landmarksToDelete = new ArrayList<Landmark>();
                    logger.info("[CampaignDumpReader] Deleting some landmarks...");        
                    ArrayList<String> ltd = Misc.filterEmptyLines(Misc.getLines(Misc.readAllFile(deleteLandmarks.getAbsolutePath())));
                    deleteGateway = ltd.contains("gateway");
                    if (deleteGateway){
                        ltd.remove("gateway");
                        logger.info("[CampaignDumpReader] Deleting gateway...");
                    }

                    for (String ltd_string: ltd){
                        landmarksToDelete.add(new Landmark(MiscIP.solveName(ltd_string)));
                    }


                    

                }
                /* End of deleting. */
            }

            /* Delete certain landmarks information here. */
            if (landmarksToDelete != null){
                fe3 = deleteDataOfLandmarks(fe3, landmarksToDelete, deleteGateway);
            }
            firstFlowElement = false;

            fe3.put(PipDefs.FE_ANOMALY_DETECTOR_PARAM1, fe.get(PipDefs.FE_ANOMALY_DETECTOR_PARAM1));
            //logger.info("NUEVO ELEMENTO PARA METER EL SIGNIFICANCE LEVEL EN EL FLOWELEMENT");
            

            for (Pipelineable p: sinks){
                p.insertFlowElement(fe3, PipDefs.SIGN_PINGGEN);
            }
        }else{
            breader.close();
            freader.close();
            freader = null;
            breader = null;
            throw new NoMoreWorkException("Reading done.");
        }
    }


    private FlowElement deleteDataOfLandmarks(FlowElement fe, ArrayList<Landmark> landmarksToDelete, boolean includeGateway){
        FlowElement ret;
        if (landmarksToDelete==null || landmarksToDelete.size()==0){
            ret = fe;
        }else{
            for(Landmark l: landmarksToDelete){
                fe = deleteDataOfLandmark(fe, l);
            }
            ret = fe;
        }
        if (includeGateway==true){
            ret = deleteDataOfLandmarkGateway(fe);
        }
        return ret;
    }

    private FlowElement deleteDataOfLandmarkGateway(FlowElement fe){
        try {
            int avoidindex = AnomalyDetectorBernoulli.getLandmarkGatewayIndex(fe);

            ArrayList<Landmark> landmarkList = (ArrayList<Landmark>)fe.get(PipDefs.FE_LANDMARKS_LIST);
            Landmark l = landmarkList.get(avoidindex);
            /* We remove the landmark from the list. */
            int oldsize = landmarkList.size();
            landmarkList.remove(l);
            int newsize = landmarkList.size();

            /* We remove the data of the landmark. */
            Timestamp[][][] timestampPairs = (Timestamp[][][])fe.get(PipDefs.FE_TIMESTAMP_PAIRS); /* Original. */
            Timestamp[][][] newTimestampPairs = new Timestamp[newsize][][];

            int newindex = 0;
            for(int oldindex=0;oldindex<oldsize;oldindex++){
                if (oldindex != avoidindex){ /* If it is not the one we want to avoid, copy it. */
                    newTimestampPairs[newindex++] = timestampPairs[oldindex];
                }
            }
            fe.put(PipDefs.FE_TIMESTAMP_PAIRS, newTimestampPairs); /* We replace the data in the FlowElement. */
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return fe;
    }


    private FlowElement deleteDataOfLandmark(FlowElement fe, Landmark l){
        try {
            int avoidindex = AnomalyDetectorBernoulli.getLandmarkIndex(fe, l);

            ArrayList<Landmark> landmarkList = (ArrayList<Landmark>)fe.get(PipDefs.FE_LANDMARKS_LIST);
            /* We remove the landmark from the list. */
            int oldsize = landmarkList.size();
            landmarkList.remove(l);             
            int newsize = landmarkList.size();

            /* We remove the data of the landmark. */
            Timestamp[][][] timestampPairs = (Timestamp[][][])fe.get(PipDefs.FE_TIMESTAMP_PAIRS); /* Original. */
            Timestamp[][][] newTimestampPairs = new Timestamp[newsize][][];

            int newindex = 0;
            for(int oldindex=0;oldindex<oldsize;oldindex++){
                if (oldindex != avoidindex){ /* If it is not the one we want to avoid, copy it. */
                    newTimestampPairs[newindex++] = timestampPairs[oldindex];
                }
            }
            fe.put(PipDefs.FE_TIMESTAMP_PAIRS, newTimestampPairs); /* We replace the data in the FlowElement. */
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        return fe;
    }

    public ArrayList<Object> sendCommand(String command, ArrayList<Object> args) throws UnsupportedCommandException {
        throw new UnsupportedCommandException("No command.");
    }

}


