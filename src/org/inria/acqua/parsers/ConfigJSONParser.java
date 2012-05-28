package org.inria.acqua.parsers;

import java.lang.reflect.Method;
import java.util.ArrayList;
import org.apache.log4j.Logger;
import org.inria.acqua.misc.Landmark;
import org.inria.acqua.mjmisc.Misc;
import com.google.gson.Gson;

/**
 * Parser of Configuration files (JSON format).
 * As all of them, this parser uses regular expressions to get the fields of such file.
 * It also can write this file.
 * @author mjost
 */
public class ConfigJSONParser extends ConfigFileParser{
	private static Logger logger = Logger.getLogger(ConfigJSONParser.class.getName()); 
    public static final String LANDMARKS_SEPARATOR = "\n";
    public static final String DEFAULT_CONFIG_FILE_NAME = "config.json";
    public static final String EXPERIMENTS_CONFIG_FILE_NAME = "config-exp.json";
    
    private transient String filename;
	private long number_of_loops;
	private String session_filename;
	private int ife_execution_period;
	private double xp_anom_detection_param1;
	private double xp_anom_detection_param2;
	private double xp_anom_detection_param3;
	private double xp_anom_detection_param4;
	private double xp_anom_detection_param5;
	private int number_of_pings;
	private int timeout_seconds;
	private Landmark[] landmarks;

    public static ConfigJSONParser getConfigJSONParser(String file) throws Exception{
        String content = Misc.readAllFile(file);
    	logger.info("Creating json configuration object with the following json: " + 
	        content);
    	Gson gson = new Gson();
    	ConfigJSONParser obj = gson.fromJson(content, ConfigJSONParser.class);
    	obj.filename = file;
    	logger.info("Created json configuration object with the following configuration: " + 
	    	obj.toString());
    	return obj;
    }

    public static ConfigJSONParser getEmptyConfigJSONParser(String configfile) throws Exception{
    	ConfigJSONParser obj = new ConfigJSONParser();
    	obj.filename = configfile;
    	ArrayList<Landmark> landm = new ArrayList<Landmark>();
    	landm.add(new Landmark("192.168.0.1"));
    	landm.add(new Landmark("192.168.0.2"));
    	obj.landmarks = Landmark.al_2_ar(landm);
    	return obj;
    }

    public void setNumberOfLoops(long numberOfLoops) {
    	number_of_loops = numberOfLoops;
    }

    public long getNumberOfLoops() {
    	return number_of_loops;
    }

    public void setSessionFileName(String sessionFileName) {
    	session_filename = sessionFileName;
    }

    public String getSessionFileName() {
    	return session_filename;
    }
    
    public void setIFEExecutionPeriod(int x){
    	ife_execution_period = x;
    }

    public int getIFEExecutionPeriod(){
    	return ife_execution_period;
    }

    public void setAnomalyDetectionParameter1(double x){
        xp_anom_detection_param1 = x;
    }

    public double getAnomalyDetectionParameter1(){
    	return xp_anom_detection_param1;
    }

    public void setAnomalyDetectionParameter2(double x){
        xp_anom_detection_param2 = x;
    }

    public double getAnomalyDetectionParameter2(){
    	return xp_anom_detection_param2;
    }


    public void setAnomalyDetectionParameter3(double x){
        xp_anom_detection_param3 = x;
    }

    public double getAnomalyDetectionParameter3(){
    	return xp_anom_detection_param3;
    }


    public void setAnomalyDetectionParameter4(double x){
        xp_anom_detection_param4 = x;
    }

    public double getAnomalyDetectionParameter4(){
    	return xp_anom_detection_param4;
    }

    public void setAnomalyDetectionParameter5(double x){
        xp_anom_detection_param5 = x;
    }

    public double getAnomalyDetectionParameter5(){
    	return xp_anom_detection_param5;
    }

    public void setTimeoutSeconds(int x){
        timeout_seconds = x;
    }

    public int getTimeoutSeconds(){
    	return timeout_seconds;
    }

    public void setNumberOfPings(int x){
    	number_of_pings = x;
    }

    public int getNumberOfPings(){
    	return number_of_pings;
    }

    /** Set the landmarks for the IFE tool. */
    public void setLandmarks(ArrayList<Landmark> x){
    	landmarks = Landmark.al_2_ar(x);
    }

    /** Set the landmarks for the IFE tool. 
     * @throws Exception */
    public void setStrLandmarks(ArrayList<String> landm) throws Exception{
    	ArrayList<Landmark> landmarks = new ArrayList<Landmark>();
    	for (String ones: landm){
    		Landmark onel = new Landmark(ones);
    		landmarks.add(onel);
    	}
    	this.landmarks = Landmark.al_2_ar(landmarks);
    }

    /** Return the landmarks for the IFE tool. */
    public ArrayList<String> getStrLandmarks() throws Exception{
    	ArrayList<String> ret = new ArrayList<String>();
    	for (Landmark one: landmarks){
    		ret.add(one.toString());
    	}
    	return ret;
    }

	@Override
	public void writeFile() throws Exception {
    	Gson gson = new Gson();
    	String json = gson.toJson(this);
    	Misc.writeAllFile(filename, json);
	}

	public String toString(){
		String ret = "";
		Method[] methods = this.getClass().getMethods();
		for (Method m: methods){
			if (m.getName().startsWith("get") && m.getParameterTypes().length==0){
				try {
					ret = ret + m.getName() + "() -> '" + m.invoke(this, null).toString() + "'\n";
				} catch (Exception e) {
					ret = ret + m.getName() + "() -> '" + "<error>" + "'\n";
				}
			}
		}
		return ret;
		
	}
	public static void main(String args[]) throws Exception{
		ConfigJSONParser parser = ConfigJSONParser.getEmptyConfigJSONParser("conf/config.json");
		parser.writeFile();
	}

}
