package org.inria.acqua.plugins.ifecontroller;

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
import org.inria.acqua.plugins.gatewaychecker.GatewayChecker;

/**
 * Controls that IFE is in a given range. 
 * @author mjost
 */
public class IFController implements Pipelineable{
	private static Logger logger = Logger.getLogger(IFController.class.getName()); 
    public static final String COMMAND_NOTIFY_TO_USER = "command-notify-to-user";
    private static final float IFE_THRESHOLD = 0.6f;
    private ArrayList<Pipelineable> sinks;
    private Pipeline pipeline;

    public IFController(Pipeline pipeline){
        this.pipeline = pipeline;
        sinks = new ArrayList<Pipelineable> ();
    }

    public void addAsSink(Pipelineable p) {
        sinks.add(p);
    }

    private void print(String str){
        logger.info(str);
    }

    public void insertFlowElement(FlowElement fe, String signature) throws Exception {
        Float ife;
        boolean alarm = false;
        if (PipDefs.SIGN_IFECALC.equals(signature)){

            ife = (Float) fe.get(PipDefs.FE_IFE_THIS_CAMPAIGN);

            alarm = (ife > IFE_THRESHOLD);

            if (alarm == true){
                ArrayList<Object> answer = pipeline.putCommand(GatewayChecker.COMMAND_CHECK_GATEWAY_IF_ABNORMAL, null);
                ArrayList<Object> args = new ArrayList<Object>();
                args.add(new Landmark(Landmark.LANDMARK_GATEWAY));
                ArrayList<Object> answer2 = pipeline.putCommand(AnomalyDetector.COMMAND_GET_ANOMALY_VECTOR, args);
                print("[IFController] Anomaly vector: " + answer2.toString());

                Boolean gatewayIsAbnormal = (Boolean)answer.get(0);
                ArrayList<Object> retu = new ArrayList<Object>();
                if (gatewayIsAbnormal){
                    retu.add("Important IFactor: DUE TO THE GATEWAY.");
                    pipeline.putCommand(COMMAND_NOTIFY_TO_USER, retu);
                }else{
                    retu.add("Important IFactor: MAYBE due to the gateway.");
                    pipeline.putCommand(COMMAND_NOTIFY_TO_USER, retu);
                }
            }
        }else{
            throw new PipelineException("Signature not supported: '" + signature + "'.");
        }
    }

    public ArrayList<Object> sendCommand(String command, ArrayList<Object> args) throws UnsupportedCommandException {
        throw new UnsupportedCommandException("");
    }
    
}
