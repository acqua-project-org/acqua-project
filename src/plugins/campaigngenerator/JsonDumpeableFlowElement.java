package plugins.campaigngenerator;

import java.util.ArrayList;
import misc.Landmark;
import misc.Timestamp;
import plugins.FlowElement;
import plugins.PipDefs;

public class JsonDumpeableFlowElement {
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
        landmarks_list = (Landmark[])(al_2_ar((ArrayList<Landmark>)fe.get(PipDefs.FE_LANDMARKS_LIST)));
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
        fe.put(PipDefs.FE_LANDMARKS_LIST,this.ar_2_al(landmarks_list));
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

    public Landmark[] al_2_ar(ArrayList<Landmark> arg){
        Landmark[] arr = (Landmark[])new Landmark[arg.size()];
        int i=0;
        for(Landmark t: arg){
            arr[i] = t;
            i++;
        }
        return (Landmark[])arr;
    }
    public ArrayList<Landmark> ar_2_al(Landmark[] arg){
        ArrayList<Landmark> ret = new ArrayList<Landmark>();

        for(int i=0;i<arg.length;i++){
            ret.add(arg[i]);
        }
        return ret;
    }
   
}
