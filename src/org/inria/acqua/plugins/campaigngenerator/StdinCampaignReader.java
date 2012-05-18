
package org.inria.acqua.plugins.campaigngenerator;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.inria.acqua.exceptions.*;
import org.inria.acqua.plugins.FlowElement;
import org.inria.acqua.plugins.PipDefs;
import org.inria.acqua.plugins.Pipelineable;

/**
 * This class converts string-lines coming from stdin into flowElements containing
 * Ping Campaings (pings done to many landmarks). */
public class StdinCampaignReader implements Pipelineable{
	private static Logger logger = Logger.getLogger(StdinCampaignReader.class.getName()); 
    private ArrayList<Pipelineable> sinks;
    private Gson gson;
//    private BufferedReader breader;
    private Scanner breader;

//    public StdinCampaignReader(BufferedReader reader){
    public StdinCampaignReader(Scanner reader){
        this.sinks = new ArrayList<Pipelineable>();
        gson = new Gson();
        breader = reader;
    }

    public void addAsSink(Pipelineable p) {
        sinks.add(p);
    }

    public void insertFlowElement(FlowElement fe, String signature) throws Exception {
    	// This pipeline-element expects FlowElements of the type SIGN_INPUT. 
        if (!PipDefs.SIGN_INPUT.equals(signature)){
            throw new PipelineException("Expected INPUT FlowElement but received " + signature + ".");
        }

    	logger.info("Reading line from stdin...");
//        String str = breader.readLine(); // We obtain a json element from stdin. 
        String str; 
        try{
			str  = breader.nextLine(); // We obtain a json element from stdin. 
        }catch(Exception e){
        	logger.info("Error while obtaining string...");
        	return;
        }
        // This json element is converted into a JsonDumpeableFlowElement by 
        // the Google Gson library. There must be a perfect matching between the 
        // json element read and the definition of JsonDumpleableFlowElement. 
        if (str != null){
        	logger.info("Obtained string: '" + str + "'.");
            JsonDumpeableFlowElement fe2 = gson.fromJson(str, JsonDumpeableFlowElement.class);
            // Now that the JsonDumpleableFlowElement has in its fields all the information
            // about the campaign, we process it a bit to obtain a standard FlowElement. 
            FlowElement fe3 = fe2.dumpToFlowElement();
            
            fe3.put(PipDefs.FE_ANOMALY_DETECTOR_PARAM1, fe.get(PipDefs.FE_ANOMALY_DETECTOR_PARAM1));

            // Put this flow element in the next pipeline element. 
            for (Pipelineable p: sinks){
                p.insertFlowElement(fe3, PipDefs.SIGN_PINGGEN);
            }
        }
    }

    public ArrayList<Object> sendCommand(String command, ArrayList<Object> args) throws UnsupportedCommandException {
    	// This pipeline-element does not support any kind of command.
        throw new UnsupportedCommandException("No command.");
    }
}
