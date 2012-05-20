package org.inria.acqua.plugins.campaigngenerator;

import com.google.gson.Gson;
import java.util.*;

import org.apache.log4j.Logger;
import org.inria.acqua.exceptions.*;
import org.inria.acqua.misc.*;
import org.inria.acqua.mjmisc.Misc;
import org.inria.acqua.plugins.*;
import org.inria.acqua.plugins.campaigngenerator.pingabstraction.Pinger;

/**
 * First generation of the CampaignGenerator class. 
 * @author mjost
 * Pipeline element: CampaignGenerator.
 * Input:
 * - version
 * - input_id
 * - timeout_ms
 * - packet_size
 * - count
 * - T_ping_ms
 * - T_camp_ms
 * - landmark
 * Output:
 * - the same input
 * - For new measurements
 *  - version
 *  - login_name
 *  - input_id
 *  - IP_src
 *  - timestamp_pairs
 */

public class CampaignGenerator implements Pipelineable{
	private static Logger logger = Logger.getLogger(CampaignGenerator.class.getName()); 
    private ArrayList<Pipelineable> sinks;
    private String dumpFilename;
    private Gson gson;
    private Integer inputID;

    public CampaignGenerator(){
        this(null);
    }

    public CampaignGenerator(String dumpFilename){
        sinks = new ArrayList<Pipelineable>();
        this.dumpFilename = dumpFilename;
        if (dumpFilename!=null){
            try{
                Misc.deleteFile(dumpFilename);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        gson = new Gson();
    }
    /* Receives as input:
     version
     input_id
     timeout_ms
     packet_size
     count
     T_ping_ms    Time in ms between pings.
     T_camp_ms    Make this one with absolute reference, so it keeps timing along the time.
     landmark
     * Gives to the next element:
     Previous input data (maybe as an ID to another place where inputs are (since it will not change that frequently)).
     New measurements:
     *     - version
     *     - login_name
     *     - input_id
     *     - IP_src
     *     - timestamp_pairs (  [ [ping1sent, ping1recv] , ... , [pingNsent, pingNrecv] ],
     *                          [ [ping1sent, ping1recv] , ... , [pingNsent, pingNrecv] ]
     *                          ...
     *                       )
     */

    public void addAsSink(Pipelineable p) {
        sinks.add(p);
    }

    public void insertFlowElement(FlowElement fe, String signature) throws Exception{
        /*
         * OUTLINE
         * Obtain flowElement fe.
         * Get all the landmarks.
         * Create a timestamp_pairs matrix [][][].
         * Go through them one by one.
         * - Create a matrix[count][2] for current landmark.
         * - Ping it as many times as count tells using
         *   - In each ping fill [i][0] and [i][1] with the timestamps of sent and received (or timeout)
         *   - Remember to use for each ping
         *     - landmark
         *     - timeout_ms
         *     - packet_size
         *     - T_ping_ms as inter-ping time.
         * - Once all the pings are done join all the [][] matrix in the big one.
         * - Insert this big matrix in the flowElement as "timestamp_pairs".
         * - Call sink.insertFlowElement(fe)
         * - Done.
         */

        if (!PipDefs.SIGN_INPUT.equals(signature)){
            throw new PipelineException("Expected input FlowElement.");
        }

        Integer inputid = (Integer)fe.get(PipDefs.FE_INPUT_ID);
        if (this.inputID==null || inputID!=inputid){
            /* Anyway this class uses everything again (does not have memory). */
        }

        ArrayList<Landmark> landmarks_list = (ArrayList<Landmark>)fe.get(PipDefs.FE_LANDMARKS_LIST);
        Integer count = (Integer)fe.get(PipDefs.FE_COUNT);
        Integer timeout_ms = (Integer)fe.get(PipDefs.FE_TIMEOUT_MS);
        Integer packet_size = (Integer)fe.get(PipDefs.FE_PACKET_SIZE);
        Integer T_ping_ms = (Integer)fe.get(PipDefs.FE_T_PING_MS);
        Timestamp[][][] timestamp_pairs = new Timestamp[landmarks_list.size()][][];
        logger.info("Pinging started\n");
        for (int i=0; i<landmarks_list.size(); i++){
            Landmark landmark = landmarks_list.get(i);
            logger.info("\tlandmark '" + landmark.toString() + "'.\n");

            Timestamp[][] pings = new Timestamp[count][2];
            for (int p=0; p<count; p++){
                logger.info("\t\tping #'"+p+"'...");
                Date date = Calendar.getInstance().getTime();
                
                float ping_ms = Pinger.ping(landmark.toString(), timeout_ms / 1000, packet_size);
                pings[p][0] = new Timestamp(date);
                if (ping_ms<0){
                    logger.warn("timeout");
                    pings[p][1] = new Timestamp(Timestamp.TIMEOUT);
                }else{
                    Date dateplus = new Date(date.getTime()+(long)ping_ms);
                    logger.info(" "+ping_ms+"ms");
                    pings[p][1] = new Timestamp(dateplus);
                }
                logger.info("\tDone this ping.");
                try{
                    Thread.sleep(T_ping_ms);
                }catch(Exception e){e.printStackTrace();}
            }
            timestamp_pairs[i] = pings;
            //MyLog.logWithDump(timestamp_pairs, "of timestamp_pairs");
        }
        logger.info("End pinging.\n\n");
        
        fe.put(PipDefs.FE_TIMESTAMP_PAIRS, timestamp_pairs);
        fe.put(PipDefs.FE_LOGIN_NAME, "login_name_vale");
        fe.put(PipDefs.FE_IP_SRC, "fill it"/*MiscIP.getPublicIPAddress()*/);

        if (dumpFilename!=null){
            String report = gson.toJson(fe);
            Misc.appendToFile(dumpFilename, report + "\n\n");
        }

        if (!sinks.isEmpty()){
            for(Pipelineable sink:sinks){
                sink.insertFlowElement(fe,PipDefs.SIGN_PINGGEN);
            }
        }else{
            System.err.println("There is no sink connected.");
        }
    }


    public static void main(String args[]) throws Exception{

    }

    public ArrayList<Object> sendCommand(String command, ArrayList<Object> args) throws UnsupportedCommandException {
        throw new UnsupportedCommandException("No command.");
    }
}
