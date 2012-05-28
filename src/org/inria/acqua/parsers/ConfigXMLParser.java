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
public class ConfigXMLParser extends ConfigFileParser{
	private static Logger logger = Logger.getLogger(ConfigXMLParser.class.getName()); 
    /** Attributes that store the value of each field of the configuration file. */
    public static ConfigXMLParser instance = null;

    public static final String DEFAULT_SESSION_FILE_NAME = "session.dat";
    
    private XMLParser xmlParser = null;

    private static String xp_base = "/ACQUA_CONFIG_FILE/";
    private static String xp_ife_execution_period = xp_base + "ife_execution_period/@value";
    private static String xp_landmarks = xp_base + "landmarks";
    private static String xp_number_of_pings = xp_base + "number_of_pings/@value";
    private static String xp_anom_detection_param1 = xp_base + "anomaly_detection_parameter1/@value";
    private static String xp_anom_detection_param2= xp_base + "anomaly_detection_parameter2/@value";
    private static String xp_anom_detection_param3= xp_base + "anomaly_detection_parameter3/@value";
    private static String xp_anom_detection_param4 = xp_base + "anomaly_detection_parameter4/@value";
    private static String xp_anom_detection_param5 = xp_base + "anomaly_detection_parameter5/@value";
    private static String xp_timeout = xp_base + "timeout/@value";
    private static String xp_session_filename = xp_base + "session_filename";
    private static String xp_number_of_loops = xp_base + "number_of_loops/@value";

    public ConfigXMLParser(String file) throws Exception{
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
    public void setStrLandmarks(ArrayList<String> x){
        String landm = this.getStrLandmarksSerialized(x);
        try{
            xmlParser.queryOneAnswer(xp_landmarks).setTextContent(landm);
        }catch(Exception e){e.printStackTrace();}
    }


    /** Return the landmarks for the IFE tool. */
    public ArrayList<String> getRawProvidedLandmarks() throws Exception{
        String res = null;
        Node node = xmlParser.queryOneAnswer(xp_landmarks);
        res = node.getTextContent().trim();
        return getStrLandmarksFromText(res);
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
    public String getStrLandmarksSerialized(ArrayList<String> landmarks){
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
    public ArrayList<String> getStrLandmarksFromText(String landmarksTemp){
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

    /** Return the landmarks for the IFE tool. */
    public ArrayList<String> getStrLandmarks() throws Exception{
        String res = null;
        Node node = xmlParser.queryOneAnswer(xp_landmarks);
        res = node.getTextContent().trim();
        return getStrLandmarksFromText(res);
    }


    /** Write the file with the given changes in the nodes. */
    public void writeFile(){
        xmlParser.writeXML();
    }
}
