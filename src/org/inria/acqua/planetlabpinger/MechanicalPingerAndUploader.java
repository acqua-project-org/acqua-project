
package org.inria.acqua.planetlabpinger;

import com.google.gson.Gson;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;
import org.inria.acqua.misc.FTPClientWrapper;
import org.inria.acqua.misc.Landmark;
import org.inria.acqua.misc.MiscIFE;
import org.inria.acqua.misc.Timestamp;
import org.inria.acqua.mjmisc.Misc;
import org.inria.acqua.mjmisc.MiscIP;
import org.inria.acqua.plugins.FlowElement;
import org.inria.acqua.plugins.PipDefs;
import org.inria.acqua.plugins.campaigngenerator.JsonDumpeableFlowElement;
import org.inria.acqua.plugins.campaigngenerator.MeasurementReceiver;
import org.inria.acqua.plugins.campaigngenerator.ParallelPinger;


public class MechanicalPingerAndUploader extends Thread implements MeasurementReceiver{
	private static Logger logger = Logger.getLogger(MechanicalPingerAndUploader.class.getName()); 
    private Subscription clientToPing;
    private String creationTimeFormatted_high;
    private String creationTimeFormatted_low;
    
    private int pingsPerCampaign;
    private int pingTimeoutSeconds;
    private int packetSize;
    private Timestamp[][] resultPings;
    private Gson gson = new Gson();
    private String localIP;
    private int inputID;

    private String ftpserver;
    private String ftpname;
    private String ftppass;

    private final static int FTP_TIMEOUT = 1000 * 30;

    public MechanicalPingerAndUploader(Subscription clientToPing, String ftpserver, String ftpname, String ftppass){
        this.clientToPing = clientToPing;
        this.ftpserver = ftpserver;
        this.ftpname = ftpname;
        this.ftppass = ftppass;

    }

    public void setParameters(int pingsPerCampaign, int pingTimeoutSeconds, int packetSize, Calendar d, String localIP, int inputID){
        creationTimeFormatted_high = MiscIFE.getTimeFormattedHigh(d);
        creationTimeFormatted_low = MiscIFE.getTimeFormattedLow(d);
        
        this.pingsPerCampaign = pingsPerCampaign;
        this.pingTimeoutSeconds = pingTimeoutSeconds;
        this.packetSize = packetSize;
        this.localIP = localIP;
        this.inputID = inputID;

        this.setName("MechUp-" + clientToPing.getName() + "-" + creationTimeFormatted_low);

    }
    @Override
    public void run(){
        String finalpath = "nothing";
        String ID = this.creationTimeFormatted_low + this.clientToPing.getName();
        logger.info("["+ID+"]Pinging started: to '" + clientToPing.getName() + "' at '" + creationTimeFormatted_low + "'...");
        try{
            /*******************/
            /* Do the pings... */
            /*******************/

            ParallelPinger pp = new ParallelPinger(this, 1, 1, new Landmark(clientToPing.getPublicIP()),
                    pingTimeoutSeconds, packetSize, pingsPerCampaign, 20);
            pp.run(); /* Now our buffer 'resultPings' is filled. */
            //logger.info("["+ID+"]Finished pinging...");
            /***********************************/
            /* Put in result the JSON element. */
            /***********************************/
            pp = null;
            
            System.gc();

            FlowElement fe = new FlowElement();
            
            /* Now we fill the Flow Element 'fe'. */
            {
                Timestamp[][][] timestamp_pairs = new Timestamp[1][][];
                timestamp_pairs[0] = resultPings;

                /* Basic ones. */
                fe.put(PipDefs.FE_VERSION,"1.0a");
                fe.put(PipDefs.FE_INPUT_ID, inputID);
                fe.put(PipDefs.FE_TIMEOUT_MS, this.pingTimeoutSeconds*1000);
                fe.put(PipDefs.FE_PACKET_SIZE, this.packetSize);
                fe.put(PipDefs.FE_COUNT, this.pingsPerCampaign);
                fe.put(PipDefs.FE_T_PING_MS, new Integer(50));
                //fe.put(PipDefs.FE_SIGNIFICANCE_LEVEL, new Float((float)cp.getSignificanceLevel()/1000.0f));
                fe.put(PipDefs.FE_T_CAMP_MS, new Integer(-1));

                /* Specific ones. */
                ArrayList<Landmark> listlist = new ArrayList<Landmark>();
                try {
                    listlist.add(new Landmark(clientToPing.getPublicIP(), clientToPing.getName()));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                fe.put(PipDefs.FE_LANDMARKS_LIST, listlist);
                fe.put(PipDefs.FE_TIMESTAMP_PAIRS, timestamp_pairs);
                fe.put(PipDefs.FE_LOGIN_NAME, clientToPing.getName());
                fe.put(PipDefs.FE_IP_SRC, localIP);
            }
            /* 'fe' completely filled. */

            String result = gson.toJson(new JsonDumpeableFlowElement(fe));

            /*************************/
            /* Upload the results... */
            /*************************/
            String hostname = "noname";
            try{
                hostname = Misc.getHostname();
            }catch(Exception e){
                try{
                hostname = MiscIP.getPublicIPAddress();
                }catch(Exception ex){
                    logger.info("["+ID+"]Couldn't find out any hostname. Using 'noname'.");
                    e.printStackTrace();
                }
            }

            finalpath = "/pinger/pings/" + creationTimeFormatted_high + "/" +
                    creationTimeFormatted_low + "/" + clientToPing.getName() + "/" + hostname + ".txt";

            int mili = (new Random()).nextInt(30*1000);
            logger.info("["+ID+"]Done pinging. Uploading in " + mili +" milliseconds...");
            Thread.sleep(mili);
            
            logger.info("["+ID+"]Woken up.");
            boolean general_success = false;
            int attemps = 6;
            for(int i=0;i<attemps;i++){
                boolean success = false;
                try{
                    FTPClientWrapper ftpclient = new FTPClientWrapper(ftpserver, ftpname, ftppass);
                    logger.info("["+ID+"]Uploading...");
                    ftpclient.uploadFromStringWithTimeout(finalpath,  result, FTP_TIMEOUT);
                    success = true;
                    ftpclient = null;
                }catch(ConnectException ce){
                    logger.info("["+ID+"]Non reachability to FTP server...");
                }catch(Exception e){
                    FTPClientWrapper ftpclient = new FTPClientWrapper(ftpserver, ftpname, ftppass);
                    try{
                        logger.info("["+ID+"]Mini-non-folder-failure. Creating folder and uploading...");
                        ftpclient.forceCreationOfPathWithTimeout(finalpath, FTP_TIMEOUT);
                        ftpclient.uploadFromStringWithTimeout(finalpath,  result, FTP_TIMEOUT);
                        success = true;
                    }catch(Exception ee){
                        logger.info("["+ID+"]Attemp failure: '" + ee.getMessage() + "'.");
                    }
                }
                if (success == true){
                    general_success = true;
                    break;
                }else{
                    logger.info("["+ID+"]Failed the upload " + (i+1) + "/" + attemps + ". Retrying in a moment...");
                    Thread.sleep(60 * 1000);
                }
            }
            if (general_success == false){
                throw new Exception("["+ID+"]Failed all the attemps.");
            }

            logger.info("["+ID+"]Results uploaded to '" + finalpath + "'.");
        }catch(Throwable e){
            logger.info("["+ID+"]Couldn't upload results of '" + finalpath + "' ('"+e.getMessage()+"').");
            e.printStackTrace();
        }
        System.gc();
    }

    public void insertMeasurement(int campaignID, int id, Timestamp[][] pings) throws Exception {
        this.resultPings = pings;
    }
}
