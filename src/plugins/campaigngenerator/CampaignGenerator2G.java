package plugins.campaigngenerator;


import mjmisc.log.MyLog;
import mjmisc.Misc;
import com.google.gson.Gson;
import exceptions.*;
import java.util.*;
import misc.*;
import plugins.*;


/**
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

public class CampaignGenerator2G implements Pipelineable, MeasurementReceiver{
    private ArrayList<Pipelineable> sinks;
    private Timestamp[][][] timestamp_pairs;
    private int collected;
    private ArrayList<Landmark> landmarks_list;
    private FlowElement fe;
    private String dumpFilename;
    private Gson gson;
    private int campaignID;

    public CampaignGenerator2G(){
        this(null);
    }

    public CampaignGenerator2G(String dumpFilename){
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
        campaignID = 0;
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

        this.campaignID++;
        
        this.fe = fe;
        landmarks_list = (ArrayList<Landmark>)fe.get(PipDefs.FE_LANDMARKS_LIST);
        final Integer count = (Integer)fe.get(PipDefs.FE_COUNT);
        final Integer timeout_ms = (Integer)fe.get(PipDefs.FE_TIMEOUT_MS);
        final Integer packet_size = (Integer)fe.get(PipDefs.FE_PACKET_SIZE);
        final Integer T_ping_ms = (Integer)fe.get(PipDefs.FE_T_PING_MS);
        timestamp_pairs = new Timestamp[landmarks_list.size()][][];
        collected = 0;
        MyLog.logP(this, "Pinging started (id "+campaignID+")\n");
        
        ArrayList<ParallelPinger> pendantPingers = new ArrayList<ParallelPinger>();
        for (int i=0; i<landmarks_list.size(); i++){
            
            Landmark landmark = landmarks_list.get(i);
            MyLog.appendPToLogFile(this, "Created ParallelPinger id %d landmark %s\n", i, landmark.getDescriptiveName());

            ParallelPinger pp =
                    new ParallelPinger(this, this.campaignID, i, landmark, timeout_ms/1000, packet_size, count, T_ping_ms);
            pendantPingers.add(pp);
            pp.start();
            /*
            Here the ParallelPinger pp1 will do:
                CampaignGenerator2G rr.insertMeasurement(meas_pp1);
            and this will put in
                timestamp_pairs
            the measurement of that particular ParallelPinger (once at a time).
             */
            Thread.sleep(20);
            
        }
        for (int i=0; i<landmarks_list.size(); i++){
            pendantPingers.get(i).join(timeout_ms*50*(count+1));
        }

        if (collected == landmarks_list.size()){
            MyLog.appendPToLogFile(this, "End pinging.\n\n");

            fe.put(PipDefs.FE_TIMESTAMP_PAIRS, timestamp_pairs);
            fe.put(PipDefs.FE_LOGIN_NAME, "login_name_value");
            fe.put(PipDefs.FE_IP_SRC, "fill it"/*MiscIP.getPublicIPAddress()*/);

            if (dumpFilename!=null){
                String jsonstr = gson.toJson(new JsonDumpeableFlowElement(fe));
                Misc.appendToFile(dumpFilename, jsonstr + "\n");
            }

            if (!sinks.isEmpty()){
                for(Pipelineable sink:sinks){
                    sink.insertFlowElement(fe,PipDefs.SIGN_PINGGEN);
                }
            }else{
                System.err.println("There is no sink connected.");
            }
        }else{
            throw new Exception("ERROR: Some pings failed to return. Too delayed system.");
        }
    }

    public synchronized void insertMeasurement(int campaignID, int id, Timestamp[][] pings) throws Exception{
        if (campaignID == this.campaignID){
            collected++;
            timestamp_pairs[id] = pings;
            MyLog.appendPToLogFile(this, "Collected ID " + campaignID + ":" + id + " so we have " + collected + " of " + landmarks_list.size() + "\n");
        }else{
            MyLog.appendPToLogFile(this, "Error... Discarding old ParallelPinger input " + id + "\n");
        }
    }

    public ArrayList<Object> sendCommand(String command, ArrayList<Object> args) throws UnsupportedCommandException {
        throw new UnsupportedCommandException("No command.");
    }    
}
