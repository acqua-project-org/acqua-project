
package org.inria.acqua.plugins.campaigngenerator;

import java.io.File;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.inria.acqua.exceptions.*;
import org.inria.acqua.misc.Landmark;
import org.inria.acqua.misc.Timestamp;
import org.inria.acqua.mjmisc.Misc;
import org.inria.acqua.parsers.ConfigParser;
import org.inria.acqua.plugins.FlowElement;
import org.inria.acqua.plugins.PipDefs;
import org.inria.acqua.plugins.Pipelineable;
import org.inria.acqua.plugins.anomalydetector.AnomalyDetectorBernoulli;

/**
 * Performs Inverse IFE by reading the pings that Pingers put in the filesystem. 
 * Then it performs IFE considering only the monitored point (i.e. only one of the clients
 * that the pingers in Planetlab were pinging). 
 * @author mjost
 */
public class InverseDumpReader implements Pipelineable{
    
	private static Logger logger = Logger.getLogger(InverseDumpReader.class.getName()); 
    private ArrayList<Pipelineable> sinks;
    private ArrayList<FEReader> fereaders;
    private ArrayList<File> coveredLandmarks;
    private Landmark monitoredPoint;
    private ConfigParser cp;

    public InverseDumpReader(String path, Landmark monitoredPoint, ConfigParser cp) throws Exception{
        //this.dumpFilename = dumpFilename;
        this.sinks = new ArrayList<Pipelineable>();
        this.cp = cp;
        coveredLandmarks = Misc.getListOfFiles(null, path); /* Get all the files from the directory. */
        fereaders = new ArrayList<FEReader>();
        for(File landmFile: coveredLandmarks){ /* We go inside all of the landmarks. */
            /* We create a set of readers */
            try{ /* Try to use the valid files. If not valid, just discard it. */
                logger.info("Reading file: '" + landmFile + "'.");
                FEReader fr = new FEReader(landmFile);
                fereaders.add(fr);
            }catch(Exception e){
                logger.info(e.getMessage());
            }
        }
        logger.info(fereaders.size() + " valid files were loaded.");
        this.monitoredPoint = monitoredPoint;
    }

    public void addAsSink(Pipelineable p) {
        sinks.add(p);
    }

    public ArrayList<Landmark> getLandmarkList(){
        ArrayList<Landmark> ll = new ArrayList<Landmark>();
        for (FEReader fr: fereaders){
            ll.add(fr.getSourceLandmark());
        }
        return ll;
    }

    public void insertFlowElement(FlowElement fe, String signature) throws Exception {
        if (!PipDefs.SIGN_INPUT.equals(signature)){
            throw new PipelineException("Expected INPUT FlowElement but received " + signature + ".");
        }

        /*
         * Identificar un monitored point P al cual todos los landmarks L1...LN han pingueado.
         * Tomar todos esos dump de L1...LN y buscar en ellos los pings G a ese monitored point P.
         * Tomar esos pings G (hechos hacia P) y crear un nuevo FE.
         * Meterlo de una. 
         */

        /*
         * Fields obtained from the dump file after conversion.
        fe.put(PipDefs.FE_T_PING_MS,T_ping_ms);                                 =
        fe.put(PipDefs.FE_TIMEOUT_MS,timeout_ms); should not change             =
        fe.put(PipDefs.FE_IP_SRC, ip_src);                                      *
        fe.put(PipDefs.FE_SIGNIFICANCE_LEVEL, 3dsf);                            = (not hardcoded)
        fe.put(PipDefs.FE_COUNT,count);                                         =
        fe.put(PipDefs.FE_TIMESTAMP_PAIRS,timestamp_pairs);                     *
        fe.put(PipDefs.FE_INPUT_ID, input_id);                                  =
        fe.put(PipDefs.FE_T_CAMP_MS,T_camp_ms);                                 =
        fe.put(PipDefs.FE_LOGIN_NAME,login_name);                               =
        fe.put(PipDefs.FE_PACKET_SIZE,packet_size);                             =
        fe.put(PipDefs.FE_LANDMARKS_LIST,this.ar_2_al(landmarks_list));         * list of covered landmarks

         */

        ArrayList<Landmark> landm_list = new ArrayList<Landmark>();
        Timestamp[][][] timestamp_pairs = new Timestamp[coveredLandmarks.size()][][];

        FlowElement out = null;
        int counter = 0;
        for (FEReader fr: fereaders){ /* We take one FE from landmark at a time. */
            FlowElement fem = fr.readNextFE();
            
            int indexMP = AnomalyDetectorBernoulli.getLandmarkIndex(fem, monitoredPoint);
            Timestamp[][][] tmsp_src = (Timestamp[][][]) fem.get(PipDefs.FE_TIMESTAMP_PAIRS);
            
            timestamp_pairs[counter] = tmsp_src[indexMP];
            landm_list.add(fr.getSourceLandmark());
            counter++;

            if (out==null){
                out = fem;
                out.put(PipDefs.FE_TIMESTAMP_PAIRS, timestamp_pairs);
            }
        }

        out.put(PipDefs.FE_LANDMARKS_LIST,landm_list);

        for (Pipelineable p: sinks){
            p.insertFlowElement(out, PipDefs.SIGN_PINGGEN);
        }
    }

    public ArrayList<Object> sendCommand(String command, ArrayList<Object> args) throws UnsupportedCommandException {
        throw new UnsupportedCommandException("No command.");
    }

}


