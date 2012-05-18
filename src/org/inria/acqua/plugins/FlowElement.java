package org.inria.acqua.plugins;

import java.util.HashMap;

import org.apache.log4j.Logger;


public class FlowElement{
	private static Logger logger = Logger.getLogger(FlowElement.class.getName()); 
    private HashMap data;

    public FlowElement(){
        data = new HashMap();
    }

    public synchronized Object get(String key){
        Object obj = data.get(key);
        if (obj == null){
            logger.warn("ERROR: Could not get object with key '" + key + "' in this FlowElement.");
        }
        return obj;
    }

    public synchronized void put(String key, Object value){
        data.put(key, value);
    }

}
