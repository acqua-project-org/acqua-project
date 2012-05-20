
package org.inria.acqua.plugins.campaigngenerator;

import com.google.gson.Gson;
import it.sauronsoftware.ftp4j.FTPFile;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.inria.acqua.exceptions.*;
import org.inria.acqua.misc.FTPClientWrapper;
import org.inria.acqua.misc.Landmark;
import org.inria.acqua.misc.MiscIFE;
import org.inria.acqua.misc.Timestamp;
import org.inria.acqua.mjmisc.Misc;
import org.inria.acqua.plugins.FlowElement;
import org.inria.acqua.plugins.PipDefs;
import org.inria.acqua.plugins.Pipelineable;


public class InverseDumpReaderOnTheFly implements Pipelineable{
    
	private static Logger logger = Logger.getLogger(InverseDumpReaderOnTheFly.class.getName()); 
    private ArrayList<Pipelineable> sinks;
    //private ArrayList<File> coveredLandmarks;
    private String monitoredPoint;
    private String FTPServerName = null;
    private boolean ftpTrue_pathFalse = true;
    private String basePathFileReading = null;

    private int periodOfCampaign = -1;

    //private String FTPLoginName = "test";
    //private String FTPPass = "test";
    private String FTPLoginName = "acqua";
    private String FTPPass = "_FTP17820_Client";


    private FTPClientWrapper ourFTPclient;
    private Gson gson;
    private int minutesOfOffset;

    public static final int MODE_UNDEFINED = 0;
    public static final int MODE_READ_ALL_FROM = 1;
    public static final int MODE_READ_PERIODICALLY = 2;
    private int workingMode = MODE_UNDEFINED;
    private Calendar calendarFrom;
    private long cyclesCounter;

    

    public InverseDumpReaderOnTheFly(String monitoredPoint, int workingMode, int minutesOfOffset) throws Exception{
        this(monitoredPoint, workingMode, minutesOfOffset, null);
    }

    public InverseDumpReaderOnTheFly(String monitoredPoint, int workingMode, Calendar from) throws Exception{
        this(monitoredPoint, workingMode, 0, from);
    }


    public InverseDumpReaderOnTheFly(String monitoredPoint, int workingMode, int minutesOfOffset, Calendar from) throws Exception{
        //this.dumpFilename = dumpFilename;
        this.calendarFrom = from;
        this.sinks = new ArrayList<Pipelineable>();
        this.workingMode = workingMode;
        this.minutesOfOffset = minutesOfOffset;
        gson = new Gson();
        ArrayList<String> conffile =
                Misc.filterEmptyLines(Misc.getLines(Misc.readAllFile("planetlabanalyzer_config.txt")));
        for(String item: conffile){
            String[] raw_info = item.split(" ");
            if (raw_info[0].equals("FTP_SERVER")){
                this.FTPServerName = raw_info[1];
            }else if (raw_info[0].equals("USE_FTP_OR_PATH")){
                this.ftpTrue_pathFalse = (raw_info[1].equals("FTP")?true:false);
            }else if (raw_info[0].equals("PATH_WHERE_DIRECTORY_PINGER_IS")){
                this.basePathFileReading = raw_info[1];
                if (basePathFileReading.endsWith(File.pathSeparator) && basePathFileReading.length()>2){
                    basePathFileReading = basePathFileReading.substring(0, basePathFileReading.length()-1);
                }
            }else if (raw_info[0].equals("PERIOD_OF_CAMPAIGN_MS")){
                this.periodOfCampaign = Integer.parseInt(raw_info[1]);
            }else{
                throw new Exception("In PPM configuration file, the item '" + raw_info[0] + "' is unknown.");
            }
        }
        
        if (FTPServerName==null||basePathFileReading==null||periodOfCampaign==-1){
            throw new IllegalArgumentException("Some of the mandatory parameters in the configuration file are not present.");
        }

        if (ftpTrue_pathFalse==true){
            /* Connecting to the FTP server... */
            ourFTPclient = new FTPClientWrapper(FTPServerName, FTPLoginName, FTPPass);
        }
        
        this.monitoredPoint = monitoredPoint;
        this.cyclesCounter = 0;
    }

    public void addAsSink(Pipelineable p) {
        sinks.add(p);
    }

    

        /*
└── 2011_07_04
    ├── 11_12
    │   ├── erriapo
    │   │   └── maca.txt
    │   └── sandun
    │       └── maca.txt
    ├── 11_13
    │   ├── erriapo
    │   │   └── maca.txt
    │   └── sandun
    │       └── maca.txt
    ├── 11_14

     */

    public void insertFlowElement(FlowElement fe, String signature) throws Exception {
        if (!PipDefs.SIGN_INPUT.equals(signature)){
            throw new PipelineException("Expected INPUT FlowElement but received " + signature + ".");
        }

        


        /*
         * Determinar el time en el que estamos ahora.
         * Ir al directorio corresp. (usando time & Monitored Point, por ejemplo 2011_07_04/11_12/sandun).
         * In this directory you have all the PLlandmarks that pinged the Monitored Point. We use them directly one by one.
         *      Per each file (format JSON) we take the info and we start joining it to give what GeneralAnomalyDetector expects.
         *      * Check that GeneralAnomalyDetector handles the fact that now the Landmarks are dynamic (they change over time).
         */

        /* Two different modes:
         *      Read from X as much as possible
         *      Read periodically some minutes in retard.

         */
        
        Calendar d = null;

        if (workingMode == MODE_READ_ALL_FROM){
            d = (Calendar)calendarFrom.clone();
            d.setTimeInMillis(d.getTimeInMillis() + cyclesCounter * 1000 * 60);
            logger.info("Reading " + Misc.calendarToString(d));
            cyclesCounter++;
            Calendar now = Misc.getUniversalTimeMinus(minutesOfOffset);
            if (d.getTimeInMillis() + 1000 * 60 * 5 > now.getTimeInMillis()){
                throw new org.inria.acqua.exceptions.NoMoreWorkException("Done.");
            }
        }else if(workingMode == MODE_READ_PERIODICALLY){
            d = Misc.getUniversalTimeMinus(minutesOfOffset);
        }else{
            logger.info("Not valid parameter for InverseDumpReaderOnTheFly.");
            System.exit(0);
        }


        String nowpath = null;

        {
            String creationTimeFormatted_high = MiscIFE.getTimeFormattedHigh(d);
            String creationTimeFormatted_low =  MiscIFE.getTimeFormattedLow(d);
            nowpath = "/pinger/pings/" + creationTimeFormatted_high + "/" + creationTimeFormatted_low + "/" + monitoredPoint;
            if (ftpTrue_pathFalse==true){
                /* If FTP, then correct. */
            }else{
                /* If filesystem, then we need to replace the separators. */
                nowpath = nowpath.replace("/", File.separator);
            }
        }

        ArrayList<Landmark> landm_list = new ArrayList<Landmark>();

        String[] fileslist = null;

        if (ftpTrue_pathFalse==true){
            /* Remote FTP reading. */
            boolean success = false;
            int listing_counter = 0;
            do{
                try{
                    fileslist = ftpFileListToStringList(ourFTPclient.list(nowpath));
                    success = true;
                }catch(Exception e){
                    logger.info("Failed while listing...");
                    Thread.sleep(3 * 1000);
                }
                listing_counter++;
                if (listing_counter>20){
                    throw new NoMoreWorkException("20 attemps... It seems that '" + nowpath + "' is not existent. We stop here.");
                }
            }while (success == false);
        }else{
            /* Local filesystem reading. */
            String directorystr = basePathFileReading + nowpath;
            try{
                fileslist = fileListToStringList(Misc.getListOfFiles(null, directorystr));
            }catch(Exception e){
                throw new NoMoreWorkException("No more work to do (assuming '" + directorystr + "' is not there).");
            }
            if (fileslist.length < 10){
                throw new Exception("Stopping because '" + directorystr + "' has just a few landmarks to process ("+fileslist.length+").");
            }
        }


        HashMap<Landmark, FlowElement> flowelements = new HashMap<Landmark,FlowElement>();
        logger.info("Reading path '" + nowpath + "'...");
        for(String f:fileslist){
            //logger.info("\t Reading '" + nowpath + File.separator + f + "'...");
            FlowElement fe_real = null;
            try{
                String content = null;

                if (ftpTrue_pathFalse==true){
                    /* Through FTP. */
                    boolean success_now = false;
                    do{
                        try{
                            content = ourFTPclient.downloadAsString(nowpath + "/" + f); /* Might fail downloading... */
                            success_now = true;
                        }catch(Exception e){
                            logger.info("Failed while getting file '"+nowpath+"/"+f+"':" + e.getMessage());
                            Thread.sleep(3 * 1000);
                        }
                    }while (success_now == false);
                }else{
                    /* Through local filesystem. */
                    String fil = basePathFileReading + File.separator + nowpath + File.separator + f;
                    content = Misc.readAllFile(fil); /* Might fail downloading... */
                }


                
                JsonDumpeableFlowElement fe_json = gson.fromJson(content, JsonDumpeableFlowElement.class); /* Might fail converting.. */
                fe_real = fe_json.dumpToFlowElement();
                String str = (String)fe_real.get(PipDefs.FE_IP_SRC);
                String name = f.substring(1,f.length()-4);
                Landmark landmark = new Landmark(str, name);
                landm_list.add(landmark);
                flowelements.put(landmark, fe_real);
            }catch(Exception r){
                logger.warn("\t FAILED for '" + nowpath + "'...");
                r.printStackTrace();
            }
        }
        logger.info("Done for (" + fileslist.length + ").");

        /* Who cares about WHAT is the set of landmarks exactly? Do we mind their names? */
        /* Yes, we care because we put the last sample of planetlab1.xx with the anomalyDetector of planetlab1.xx */

        Timestamp[][][] timestamp_pairs = new Timestamp[landm_list.size()][][];
        int counter = 0;
        FlowElement out = null;
        for (Landmark ll: landm_list){ /* We take one FE from landmark at a time. */
            FlowElement fem = flowelements.get(ll);
            
            //int indexMP = AnomalyDetectorBernoulli.getLandmarkIndex(fem, monitoredPoint);
            
            if (out==null){ /* We initialize the initial FlowElement during only the first loop. */
                out = fem;
            }
            
            Timestamp[][][] tmsp_src = (Timestamp[][][]) fem.get(PipDefs.FE_TIMESTAMP_PAIRS);
            timestamp_pairs[counter] = tmsp_src[0]; /* Each FE coming has only one landmark, landmark 0. */
            counter++;
        }

        out.put(PipDefs.FE_TIMESTAMP_PAIRS, timestamp_pairs);
        out.put(PipDefs.FE_LANDMARKS_LIST,landm_list);

        //fe.put(PipDefs.FE_LANDMARKS_LIST, idrf.getLandmarkList()); /* This will be done on the fly. */
        
        for (Pipelineable p: sinks){
            p.insertFlowElement(out, PipDefs.SIGN_PINGGEN);
        }
    }

    public ArrayList<Object> sendCommand(String command, ArrayList<Object> args) throws UnsupportedCommandException {
        throw new UnsupportedCommandException("No command.");
    }

    private String[] ftpFileListToStringList(FTPFile[] set){
        String[] ret = new String[set.length];
        int i=0;
        for(FTPFile f:set){
            ret[i] = f.getName();
            i++;
        }
        return ret;
    }

    private String[] fileListToStringList(ArrayList<File> set){
        String[] ret = new String[set.size()];
        int i=0;
        for(File f:set){
            ret[i] = f.getName();
            i++;
        }
        return ret;
    }


}


