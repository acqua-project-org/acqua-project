package org.inria.acqua.parsers;


import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.log4j.Logger;
import org.inria.acqua.misc.Landmark;
import org.inria.acqua.mjmisc.parser.XMLParser;
import org.w3c.dom.Node;

/**
 * Parser of Configuration files.
 * As all of them, this parser uses regular expressions to get the fields of such file.
 * It also can write this file.
 * @author mjost
 */
public class ConfigParser extends Parser{
	private static Logger logger = Logger.getLogger(ConfigParser.class.getName()); 
    public static final String LANDMARKS_SEPARATOR = "\n";
    /** Attributes that store the value of each field of the configuration file. */
    public static ConfigParser instance = null;
    public static final String DEFAULT_CONFIG_FILE_NAME = "config.xml";
    public static final String EXPERIMENTS_CONFIG_FILE_NAME = "config-exp.xml";

    public static final String DEFAULT_SESSION_FILE_NAME = "session.dat";
    
    private XMLParser xmlParser = null;

    private static String xp_base = "/ACQUA_CONFIG_FILE/";
    private static String xp_ife_execution_period = xp_base + "ife_execution_period/@value";
    private static String xp_ife_path_and_name  = xp_base + "ife_path_and_name";
    private static String xp_landmarks = xp_base + "landmarks";
    private static String xp_number_of_pings = xp_base + "number_of_pings/@value";
    private static String xp_number_of_saved_entries_ife = xp_base + "number_of_saved_entries_ife/@value";
    private static String xp_number_of_saved_entries_gui = xp_base + "number_of_saved_entries_gui/@value";
    //private static String xp_significance_level = xp_base + "significance_level/@value";
    private static String xp_anom_detection_param1 = xp_base + "anomaly_detection_parameter1/@value";
    private static String xp_anom_detection_param2= xp_base + "anomaly_detection_parameter2/@value";
    private static String xp_anom_detection_param3= xp_base + "anomaly_detection_parameter3/@value";
    private static String xp_anom_detection_param4 = xp_base + "anomaly_detection_parameter4/@value";
    private static String xp_anom_detection_param5 = xp_base + "anomaly_detection_parameter5/@value";
    private static String xp_timeout = xp_base + "timeout/@value";
    private static String xp_session_filename = xp_base + "session_filename";
    private static String xp_number_of_loops = xp_base + "number_of_loops/@value";

    public ConfigParser(String file) throws Exception{
    	try{
	        xmlParser = new XMLParser(file);
    	}catch(Exception e){
    		throw new Exception("Error parsing the configuration file '" + file + "'. " + e.getMessage());
    	}
    }

    public void setNumberOfLoops(long numberOfLoops) {
        try{
            xmlParser.queryOneAnswer(xp_number_of_loops).setNodeValue(String.valueOf(numberOfLoops));
        }catch(Exception e){e.printStackTrace();}
    }

    public long getNumberOfLoops() {
        long res = 10;
        try{
            res = Long.parseLong(xmlParser.queryOneAnswer(xp_number_of_loops).getNodeValue());
        }catch(Exception e){e.printStackTrace();}
        return res;
    }

    public void setSessionFileName(String sessionFileName) {
        try{
            xmlParser.queryOneAnswer(xp_session_filename).setTextContent(sessionFileName);
        }catch(Exception e){e.printStackTrace();}
    }

    public String getSessionFileName() {
        String res = "session.dat";
        try{
            res = xmlParser.queryOneAnswer(xp_session_filename).getTextContent().trim();
        }catch(Exception e){e.printStackTrace();}
        return res;
    }
    

    
    public void setIFEExecutionPeriod(int x){
        try{
            xmlParser.queryOneAnswer(xp_ife_execution_period).setNodeValue(String.valueOf(x));
        }catch(Exception e){e.printStackTrace();}
    }

    public int getIFEExecutionPeriod(){
        int res = 30000;
        try{
            res = Integer.parseInt(xmlParser.queryOneAnswer(xp_ife_execution_period).getNodeValue());
        }catch(Exception e){e.printStackTrace();}
        return res;
    }

    public void setIFEPathAndName(String x){
        x = (new File(x)).getPath();
        try{
            xmlParser.queryOneAnswer(xp_ife_path_and_name).setTextContent(x);
        }catch(Exception e){e.printStackTrace();}
    }

    public String getIFEPathAndName(){
        String res = "";
        try{
            res = xmlParser.queryOneAnswer(xp_ife_path_and_name).getTextContent().trim();
        }catch(Exception e){e.printStackTrace();}

        if (res.trim().length()==0){
            logger.info("Using default IFE tool (acqua in '.').");
            return null; 
        }else{
            return (new File(res)).getPath();
        }
    }

    public String getIFEPath(){
        String pathandname = this.getIFEPathAndName();
        return getOnlyPath(pathandname);
        
    }

    public static String getOnlyPath(String file){
        if (file!=null){
            File aux = new File(file);
            File aux2 = new File(aux.getAbsolutePath());
            return aux2.getParent();
        }else{
            logger.info("Returning path by default ('.').");
            return ".";
        }
    }



    public void setAnomalyDetectionParameter1(double x){
        try{xmlParser.queryOneAnswer(xp_anom_detection_param1).setNodeValue(String.valueOf(x));}catch(Exception e){e.printStackTrace();}
    }

    public double getAnomalyDetectionParameter1(){
        double res = 0; try{res = Double.parseDouble(xmlParser.queryOneAnswer(xp_anom_detection_param1).getNodeValue());}catch(Exception e){e.printStackTrace(); System.exit(1);}
        return res;
    }

    public void setAnomalyDetectionParameter2(double x){
        try{xmlParser.queryOneAnswer(xp_anom_detection_param2).setNodeValue(String.valueOf(x));}catch(Exception e){e.printStackTrace();}
    }

    public double getAnomalyDetectionParameter2(){
        double res = 0; try{res = Double.parseDouble(xmlParser.queryOneAnswer(xp_anom_detection_param2).getNodeValue());}catch(Exception e){e.printStackTrace(); System.exit(1);}
        return res;
    }



    public void setAnomalyDetectionParameter3(double x){
        try{xmlParser.queryOneAnswer(xp_anom_detection_param3).setNodeValue(String.valueOf(x));}catch(Exception e){e.printStackTrace();}
    }

    public double getAnomalyDetectionParameter3(){
        double res = 0; try{res = Double.parseDouble(xmlParser.queryOneAnswer(xp_anom_detection_param3).getNodeValue());}catch(Exception e){e.printStackTrace(); System.exit(1);}
        return res;
    }



    public void setAnomalyDetectionParameter4(double x){
        try{xmlParser.queryOneAnswer(xp_anom_detection_param4).setNodeValue(String.valueOf(x));}catch(Exception e){e.printStackTrace();}
    }

    public double getAnomalyDetectionParameter4(){
        double res = 0; try{res = Double.parseDouble(xmlParser.queryOneAnswer(xp_anom_detection_param4).getNodeValue());}catch(Exception e){e.printStackTrace(); System.exit(1);}
        return res;
    }



    public void setAnomalyDetectionParameter5(double x){
        try{xmlParser.queryOneAnswer(xp_anom_detection_param5).setNodeValue(String.valueOf(x));}catch(Exception e){e.printStackTrace();}
    }

    public double getAnomalyDetectionParameter5(){
        double res = 0; try{res = Double.parseDouble(xmlParser.queryOneAnswer(xp_anom_detection_param5).getNodeValue());}catch(Exception e){e.printStackTrace(); System.exit(1);}
        return res;
    }





    public void setNumberOfSavedEntriesGUI(int x){
        try{
            xmlParser.queryOneAnswer(xp_number_of_saved_entries_gui).setNodeValue(String.valueOf(x));
        }catch(Exception e){e.printStackTrace();}
    }

    public int getNumberOfSavedEntriesGUI(){
        int res = 50;
        try{
            res = Integer.parseInt(xmlParser.queryOneAnswer(xp_number_of_saved_entries_gui).getNodeValue());
        }catch(Exception e){e.printStackTrace();}
        return res;
    }

    public void setNumberOfSavedEntriesIFE(int x){
        try{
            xmlParser.queryOneAnswer(xp_number_of_saved_entries_ife).setNodeValue(String.valueOf(x));
        }catch(Exception e){e.printStackTrace();}
    }
    public int getNumberOfSavedEntriesIFE(){
        int res = 3;
        try{
            res = Integer.parseInt(xmlParser.queryOneAnswer(xp_number_of_saved_entries_ife).getNodeValue());
        }catch(Exception e){e.printStackTrace();}
        return res;
    }



    public void setTimeoutSeconds(int x){
        try{
            xmlParser.queryOneAnswer(xp_timeout).setNodeValue(String.valueOf(x));
        }catch(Exception e){e.printStackTrace();}
    }

    public int getTimeoutSeconds(){
        int res = 2;
        try{
            res = Integer.parseInt(xmlParser.queryOneAnswer(xp_timeout).getNodeValue());
        }catch(Exception e){e.printStackTrace();}
        return res;
    }



    public void setNumberOfPings(int x){
        try{
            xmlParser.queryOneAnswer(xp_number_of_pings).setNodeValue(String.valueOf(x));
        }catch(Exception e){e.printStackTrace();}
    }

    public int getNumberOfPings(){
        int res = 4;
        try{
            res = Integer.parseInt(xmlParser.queryOneAnswer(xp_number_of_pings).getNodeValue());
        }catch(Exception e){e.printStackTrace();}
        return res;
    }

    /** Set the landmarks for the IFE tool. */
    public void setLandmarks(ArrayList<Landmark> x){
        String landm = this.getLandmarksSerialized(x);
        try{
            xmlParser.queryOneAnswer(xp_landmarks).setTextContent(landm);
        }catch(Exception e){e.printStackTrace();}
    }

    /** Set the landmarks for the IFE tool. */
    public void setRawProvidedLandmarks(ArrayList<String> x){
        String landm = this.getRawProvidedLandmarksSerialized(x);
        try{
            xmlParser.queryOneAnswer(xp_landmarks).setTextContent(landm);
        }catch(Exception e){e.printStackTrace();}
    }


    /** Return the landmarks for the IFE tool. */
    public ArrayList<Landmark> getLan1dmarks() throws Exception{
        String res = null;
        Node node = xmlParser.queryOneAnswer(xp_landmarks);
        res = node.getTextContent().trim();        
        return getLan1dmarksFromText(res);
    }

    /** Return the landmarks for the IFE tool. */
    public ArrayList<String> getRawProvidedLandmarks() throws Exception{
        String res = null;
        Node node = xmlParser.queryOneAnswer(xp_landmarks);
        res = node.getTextContent().trim();
        return getRawProvidedLandmarksFromText(res);
    }

    /** Put a collection of landmarks in a serialized-space-separated string. */
    public String getLandmarksSerialized(ArrayList<Landmark> landmarks){
        String ret = "";
        Iterator<Landmark> i = landmarks.iterator();
        while(i.hasNext()){
            ret = ret + i.next() + LANDMARKS_SEPARATOR;
        }
        ret = ret.substring(0, ret.length()-1);
        return ret; 
    }

    /** Put a collection of landmarks in a serialized-space-separated string. */
    public String getRawProvidedLandmarksSerialized(ArrayList<String> landmarks){
        String ret = "";
        Iterator<String> i = landmarks.iterator();
        boolean first = true;
        while(i.hasNext()){
            if (first){
                ret = ret + i.next();
                first = false;
            }else{
                ret = ret + LANDMARKS_SEPARATOR + i.next();
            }
        }
        
        return ret;
    }



    /** Put a collection of landmarks in a serialized-comma-separated string. */
    public String getLandmarksSerializedWithComma(ArrayList<String> landmarks){
        String ret = "";
        for(String i: landmarks){
            ret = ret + i + ",";
        }
        ret = ret.substring(0, ret.length()-1);
        return ret;
    }

    /** Gets landmarks from raw text to put them in a collection. */
    public ArrayList<Landmark> getLan1dmarksFromText(String landmarksTemp) throws Exception{
        int indice=0;
        String oneM;
        landmarksTemp = landmarksTemp.trim();
        ArrayList<Landmark> lm = new ArrayList<Landmark>();
        while (indice!=-1){
            indice = landmarksTemp.indexOf(LANDMARKS_SEPARATOR);
            if (indice!=-1){
                oneM = landmarksTemp.substring(0,indice).trim();
                landmarksTemp = landmarksTemp.substring(indice).trim();
            }else{
                oneM = landmarksTemp.trim();
            }
            lm.add(new Landmark(oneM));
        }
        return lm;

    }

    /** Gets landmarks from raw text to put them in a collection. */
    public ArrayList<String> getRawProvidedLandmarksFromText(String landmarksTemp){
        int indice=0, in1, in2, in3;
        String oneM;
        landmarksTemp = landmarksTemp.trim();
        ArrayList<String> lm = new ArrayList<String>();
        if (landmarksTemp.length()!=0){
            while (indice!=-1){
                in1 = landmarksTemp.indexOf(LANDMARKS_SEPARATOR); in1=(in1==-1?Integer.MAX_VALUE:in1);
                in2 = landmarksTemp.indexOf(" "); in2=(in2==-1?Integer.MAX_VALUE:in2);
                in3 = landmarksTemp.indexOf("\r"); in3=(in3==-1?Integer.MAX_VALUE:in3);

                if (in1==Integer.MAX_VALUE && in2==Integer.MAX_VALUE && in3==Integer.MAX_VALUE){
                    indice = -1;
                }else{
                    indice = Math.min(Math.min(in1,in2),in3);
                }

                if (indice!=-1){
                    oneM = landmarksTemp.substring(0,indice).trim();
                    landmarksTemp = landmarksTemp.substring(indice).trim();
                }else{
                    oneM = landmarksTemp.trim();
                }
                lm.add(oneM);
            }
        }
        return lm;

    }


    /** Write the file with the given changes in the nodes. */
    public void writeFile(){
        xmlParser.writeXML();
    }

    /** Testing. */
    public static void main(String[] args) throws Exception{
        File a = new File("config.xml");
        String r = a.getAbsolutePath();
        File b = new File(r);

        String s = b.getCanonicalPath();
        String t = b.getPath();
        String u = b.getName();
        String rl = b.getParent();

        ConfigParser cp = new ConfigParser("config.xml");
        //cp.writeFile();
        
    }
}
