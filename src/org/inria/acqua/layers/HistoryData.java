package org.inria.acqua.layers;

import java.io.Serializable;
import java.util.HashMap;

import org.inria.acqua.layers.painter.CurveElement;

/** This class stores all data read in a session. */
public class HistoryData implements Serializable{
    private HashMap<String, CurveElement> curveElements;
    
    public HistoryData(
             HashMap<String, CurveElement> ce
    ) throws Exception{
        curveElements = ce;
        
    }

    public HashMap<String, CurveElement> getCurveElements() {
        return curveElements;
    }

    public int getSize() throws Exception{
        int size = -1;

        for(CurveElement ce:curveElements.values()){
            if (size == -1){
                size = ce.getSize();
            }
            if (size!=ce.getSize()){
                throw new Exception("Different sizes in the CurveElements ("+size+"!="+ce.getSize()+").");
            }
        }
        if (size>0){
            return size;
        }else{
            throw new Exception("Invalid size ("+size+").");
        }
    }
}
