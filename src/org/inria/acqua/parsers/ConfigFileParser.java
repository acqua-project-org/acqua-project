package org.inria.acqua.parsers;

import java.util.ArrayList;
import java.util.Iterator;

import org.inria.acqua.misc.Landmark;
import org.inria.acqua.mjmisc.Misc;


/**
 * Parsers' "template".
 * It is supposed to be inherited by any kind of configuration-file parser.
 * @author mjost
 */
public abstract class ConfigFileParser {
    protected String rawFileContent;
    public static final String LANDMARKS_SEPARATOR = "\n";
    
    /** Open the file. 
     * @throws Exception */
    public static ConfigFileParser getConfigFileParser(String filename) throws Exception{
    	if (filename.endsWith(".xml")){
    		return new ConfigXMLParser(filename);
    	} else if (filename.endsWith(".json")){
    		return ConfigJSONParser.getConfigJSONParser(filename);
    	}
    	throw new Exception("Wrong configuration file format: " + filename);
    }
    
    /** Open a file and give its content in a String. */
    public String readFile(String filename) throws Exception{
        rawFileContent = Misc.readAllFile(filename);
        return rawFileContent;
    }

    /** Write a file with the current raw content. */
    public void writeFile(String filename) throws Exception{
        Misc.writeAllFile(filename, rawFileContent);
    }

    public abstract void setNumberOfLoops(long numberOfLoops) ;
    public abstract long getNumberOfLoops() ;
    public abstract void setSessionFileName(String sessionFileName) ;
    public abstract String getSessionFileName() ;
    public abstract void setIFEExecutionPeriod(int x);
    public abstract int getIFEExecutionPeriod();
    public abstract void setAnomalyDetectionParameter1(double x);
    public abstract double getAnomalyDetectionParameter1();
    public abstract void setAnomalyDetectionParameter2(double x);
    public abstract double getAnomalyDetectionParameter2();
    public abstract void setAnomalyDetectionParameter3(double x);
    public abstract double getAnomalyDetectionParameter3();
    public abstract void setAnomalyDetectionParameter4(double x);
    public abstract double getAnomalyDetectionParameter4();
    public abstract void setAnomalyDetectionParameter5(double x);
    public abstract double getAnomalyDetectionParameter5();
    public abstract void setTimeoutSeconds(int x);
    public abstract int getTimeoutSeconds();
    public abstract void setNumberOfPings(int x);
    public abstract int getNumberOfPings();
    public abstract void setLandmarks(ArrayList<Landmark> x);
    public abstract ArrayList<String> getStrLandmarks() throws Exception;
    public abstract void setStrLandmarks (ArrayList<String> landmarks) throws Exception;
    public abstract void writeFile() throws Exception;

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

}
