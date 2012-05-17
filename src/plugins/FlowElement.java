package plugins;

import java.util.HashMap;
import mjmisc.log.MyLog;

public class FlowElement{
    private HashMap data;

    public FlowElement(){
        data = new HashMap();
    }

    public synchronized Object get(String key){
        Object obj = data.get(key);
        if (obj == null){
            MyLog.log(data, "ERROR: Could not get object with key '" + key + "' in this FlowElement.");
        }
        return obj;
    }

    public synchronized void put(String key, Object value){
        data.put(key, value);
    }

}
