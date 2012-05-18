
package org.inria.acqua.plugins.gatewaychecker;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.inria.acqua.exceptions.PipelineException;
import org.inria.acqua.exceptions.UnsupportedCommandException;
import org.inria.acqua.misc.Landmark;
import org.inria.acqua.plugins.FlowElement;
import org.inria.acqua.plugins.PipDefs;
import org.inria.acqua.plugins.Pipeline;
import org.inria.acqua.plugins.Pipelineable;
import org.inria.acqua.plugins.anomalydetector.AnomalyDetector;
import org.inria.acqua.plugins.anomalydetector.GeneralAnomalyDetector;


public class GatewayChecker implements Pipelineable{
	private static Logger logger = Logger.getLogger(GatewayChecker.class.getName()); 
    public static final String COMMAND_CHECK_GATEWAY_IF_ABNORMAL = "command-checkgateway";
    private Pipeline pipeline;
    private ArrayList<Pipelineable> sinks;
    private boolean gatewayIsAbnormalCurrently;

    public GatewayChecker(Pipeline pipeline){
        this.pipeline = pipeline;
        sinks = new ArrayList<Pipelineable> ();
    }

    public void addAsSink(Pipelineable p) {
        sinks.add(p);
    }

    public void insertFlowElement(FlowElement fe, String signature) throws Exception {
        
        if (PipDefs.SIGN_ANDETGEN.equals(signature)){
            logger.debug("Inserting a FlowElement...\n");
            /* We have all the flowElements to proceed. */            
            gatewayIsAbnormalCurrently = tellIfGatewayIsAbnormal(fe); /* Look for the GW and check it. */
        }else{
            throw new PipelineException("Signature '"+signature+"' not supported.");
        }
    }

    private void print(String str){
        System.out.println(str);
    }

    private boolean thisIsAnomaly(int anomaly){
        return (anomaly == GeneralAnomalyDetector.ST_ABNORMAL_SHIFT || anomaly == GeneralAnomalyDetector.ST_ABNORMAL_UNREACHABLE);
    }

    private boolean tellIfGatewayIsAbnormal(FlowElement input) throws Exception{

        int gatewaysFound = 0;
        boolean gatewayAbnormal = false;

        ArrayList<Landmark> currentLandmarksList = (ArrayList<Landmark>)input.get(PipDefs.FE_LANDMARKS_LIST);
        for (Landmark lm: currentLandmarksList){
            if (lm.getType()==Landmark.TYPE_KEYWORD_GW){
                gatewaysFound++;
                FlowElement curr = (FlowElement) input.get(PipDefs.SIGN_ANDETLAN + lm.toString());
                if (curr==null){
                    throw new Exception("Landmark '" + lm + "' didn't deliver the FlowElement.");
                }
                
                Integer anomaly = (Integer)curr.get(PipDefs.FE_THIS_LANDMARK_ANOMALY);
                if (thisIsAnomaly(anomaly)==true){
                    gatewayAbnormal = true;
                }else{
                    gatewayAbnormal = false;
                }
            }
        }

        if (gatewaysFound<1){
            print("No gateway found.");
        }else if(gatewaysFound>1){
            print("More than one gateway was found.");
        }else{ /* Correct, one and only one gateway found. */
            return gatewayAbnormal;
        }
        return false;
    }

    public ArrayList<Object> sendCommand(String command, ArrayList<Object> args)
            throws UnsupportedCommandException {
        if (COMMAND_CHECK_GATEWAY_IF_ABNORMAL.equals(command)){ /*  Received a check_gateway command.
                                                        Let's tell the requestor the GW status. */
            ArrayList<Object> ret = new ArrayList<Object>();
            ret.add(new Boolean(gatewayIsAbnormalCurrently));
            return ret;
        }else{
            throw new UnsupportedCommandException("");
        }
    }
}
