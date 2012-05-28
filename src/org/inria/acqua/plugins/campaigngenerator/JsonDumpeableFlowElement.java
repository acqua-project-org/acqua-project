package org.inria.acqua.plugins.campaigngenerator;

import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.inria.acqua.misc.Landmark;
import org.inria.acqua.misc.Timestamp;
import org.inria.acqua.plugins.FlowElement;
import org.inria.acqua.plugins.PipDefs;

import com.google.gson.Gson;


/**
 * Class that is used as a template for the json formatting of the FlowElement objects. 
 * @author mjost
 */
public class JsonDumpeableFlowElement {
	
	private static Logger logger = Logger.getLogger(JsonDumpeableFlowElement.class.getName()); 
    private Landmark[] landmarks_list;
    private Integer count;
    private Integer timeout_ms;
    private Integer packet_size;
    private Integer T_ping_ms;
    private Timestamp[][][] timestamp_pairs;
    private String login_name;
    private String ip_src;
    private Integer T_camp_ms;
    private Integer input_id;
    
    public JsonDumpeableFlowElement(){}
    
    public JsonDumpeableFlowElement(FlowElement fe){
        landmarks_list = (Landmark[])(Landmark.al_2_ar((ArrayList<Landmark>)fe.get(PipDefs.FE_LANDMARKS_LIST)));
        count = (Integer)fe.get(PipDefs.FE_COUNT);
        timeout_ms = (Integer)fe.get(PipDefs.FE_TIMEOUT_MS);
        packet_size = (Integer)fe.get(PipDefs.FE_PACKET_SIZE);
        T_ping_ms = (Integer)fe.get(PipDefs.FE_T_PING_MS);
        timestamp_pairs = (Timestamp[][][]) fe.get(PipDefs.FE_TIMESTAMP_PAIRS);
        login_name = (String)fe.get(PipDefs.FE_LOGIN_NAME);
        ip_src = (String)fe.get(PipDefs.FE_IP_SRC);
        T_camp_ms = (Integer)fe.get(PipDefs.FE_T_CAMP_MS);
        input_id = (Integer)fe.get(PipDefs.FE_INPUT_ID);
        if (input_id==null){
            input_id = 0; /* jjj remove in the future. */
        }
    }

    public FlowElement dumpToFlowElement(){
        FlowElement fe = new FlowElement();
        fe.put(PipDefs.FE_LANDMARKS_LIST,Landmark.ar_2_al(landmarks_list));
        fe.put(PipDefs.FE_COUNT,count);
        fe.put(PipDefs.FE_TIMEOUT_MS,timeout_ms);
        fe.put(PipDefs.FE_PACKET_SIZE,packet_size);
        fe.put(PipDefs.FE_T_PING_MS,T_ping_ms);
        fe.put(PipDefs.FE_TIMESTAMP_PAIRS,timestamp_pairs);
        fe.put(PipDefs.FE_LOGIN_NAME,login_name);
        fe.put(PipDefs.FE_IP_SRC, ip_src);
        fe.put(PipDefs.FE_T_CAMP_MS,T_camp_ms);
        fe.put(PipDefs.FE_INPUT_ID, input_id);

        return fe;
    }

   
    public static void main(String args[]) throws Exception{
    	
    	logger.info("Preparing...");
        Scanner stdin = new Scanner (System.in);
        String str; 
        while(stdin.hasNextLine()){
        	logger.info("Obtained: " + stdin.nextLine());
        }
        
    	JsonDumpeableFlowElement a = new JsonDumpeableFlowElement();
    	Landmark[] land = {new Landmark("127.0.0.2"), new Landmark("127.0.0.3")};
        a.landmarks_list = land;
        a.count = 2;
        a.timeout_ms = 3000;
        a.packet_size = 56;
        a.T_ping_ms = 1000;
    	Timestamp[] timestamp1 = {new Timestamp(new Date()), new Timestamp(new Date())};
    	Timestamp[] timestamp2 = {new Timestamp(new Date()), new Timestamp(new Date())};
    	Timestamp[] timestamp3 = {new Timestamp(new Date()), new Timestamp(new Date())};
    	Timestamp[] timestamp4 = {new Timestamp(new Date()), new Timestamp(new Date())};
    	Timestamp[][] timestamp1a = {timestamp1, timestamp2};
    	Timestamp[][] timestamp2a = {timestamp3, timestamp4};
    	Timestamp[][][] timestamp1b = {timestamp1a, timestamp2a};
        a.timestamp_pairs = timestamp1b;
        a.login_name = "mjost";
        a.ip_src = "127.0.0.1";
        a.T_camp_ms = 60000;
        a.input_id = 123456;
        Gson gson = new Gson();
        logger.info("JSON: " + gson.toJson(a));
       // {"landmarks_list":[{"alias":"\u003cno alias\u003e","type":1,"ip":[127,0,0,2]},{"alias":"\u003cno alias\u003e","type":1,"ip":[127,0,0,3]}],"count":2,"timeout_ms":3000,"packet_size":56,"T_ping_ms":1000,"timestamp_pairs":[[[{"seconds":1.337357252124E9,"timeout":false},{"seconds":1.337357252125E9,"timeout":false}],[{"seconds":1.337357252125E9,"timeout":false},{"seconds":1.337357252125E9,"timeout":false}]],[[{"seconds":1.337357252125E9,"timeout":false},{"seconds":1.337357252125E9,"timeout":false}],[{"seconds":1.337357252125E9,"timeout":false},{"seconds":1.337357252125E9,"timeout":false}]]],"login_name":"mjost","ip_src":"127.0.0.1","T_camp_ms":60000,"input_id":123456} 
    }
}
