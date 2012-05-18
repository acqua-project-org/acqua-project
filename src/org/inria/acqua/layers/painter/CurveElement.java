package org.inria.acqua.layers.painter;

import java.util.ArrayList;
import java.util.List;

public class CurveElement {
    public static final int UNINITIALIZED                       = 0; /* 0 Base. */
    public static final int BASIC_1ARG                          = 1; /* 0 Base. */
    public static final int TIME_1ARG                           = 2; /* 0 Time. */
    public static final int BASIC_PLUS_BOX_2ARG                 = 3; /* 0 Base. 1 STD. */
    public static final int BASIC_PLUS_BOX_PLUS_EXTREMES_4ARG   = 4; /* 0 Base. 1 STD. 2 MIN. 3 MAX. */
    public static final int BASICx2_PLUS_BOX_3ARG               = 5; /* 0 Base. 1 STD. 3 2ndBase. */
    
    private ArrayList<ArrayList<Object>> curveValues;
    private String title;
    private int type;
    private String name;

    private Float baseReferenceToPaint = null;
    private Float forcedMinimum = null;
    private Float forcedMaximum = null;
    private String[] toolTipTextCaptions;


    public CurveElement(String name, int type, String title, String [] tttc) throws Exception{
        this.type = type;
        this.name = name;
        this.title = title;
        int lines;

        lines = getNumberOfArrays(type);

        curveValues = new ArrayList<ArrayList<Object>>(lines);
        for (int i=0;i<lines;i++){
            curveValues.add(i, new ArrayList());
        }
        toolTipTextCaptions = tttc;
    }


    private int getNumberOfArrays(int type) throws Exception{
        int lines;
        switch(type){
            case BASIC_1ARG:                         lines = 1; break;
            case TIME_1ARG:                          lines = 1; break;
            case BASIC_PLUS_BOX_2ARG:                lines = 2; break;
            case BASIC_PLUS_BOX_PLUS_EXTREMES_4ARG:  lines = 4; break;
            case BASICx2_PLUS_BOX_3ARG:              lines = 3; break;
            default:
                throw new Exception("Type " + type + " not supported.");
        }
        return lines;
    }
    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }
    
    public void setForcedMaximum(Float forcedMaximum) {
        this.forcedMaximum = forcedMaximum;
    }

    public void setForcedMinimum(Float forcedMinimum) {
        this.forcedMinimum = forcedMinimum;
    }

    public void setBaseReferenceToPaint(Float baseReferenceToPaint) {
        this.baseReferenceToPaint = baseReferenceToPaint;
    }

    public Float getBaseReferenceToPaint(){
        return baseReferenceToPaint;
    }

    public Float getForcedMinimum(){
        return forcedMinimum;
    }

    public Float getForcedMaximum(){
        return forcedMaximum;
    }

    public String getTitle(){
        return title;
    }

    public synchronized void addElement(int index, Object element){
        ArrayList arrr = curveValues.get(index);
        arrr.add(element);
    }

    private synchronized int checkConsistenceBetweenAllArrays() throws Exception{
        int arrays = getNumberOfArrays(type);
        int size=-1;
        for(int i=0; i<arrays; i++){
            if (i==0){size = this.curveValues.get(i).size();}
            if (size!=this.curveValues.get(i).size()){
                throw new Exception("There is no consistence in the CurveElement's arrays.");
            }
        }
        return size;
    }

    /*
    public synchronized void moveRange(int paramA, int paramB) throws Exception{
        int elements = checkConsistenceBetweenAllArrays();

        if (elements<=MINIMUM_ZOOM_ELEMENTS){
            rangex1 = 0;
            rangex2 = elements - 1;

        }else if (elements>MINIMUM_ZOOM_ELEMENTS){
            int current_zoom = rangex2 - rangex1;
            
            rangex1+=paramA;
            rangex2+=paramB;
        
            if(rangex1>rangex2){
                int aux;
                aux = rangex1;
                rangex1 = rangex2;
                rangex2 = aux;
            }
        
            if (rangex1<0){
                rangex1 = 0;
                rangex2 = current_zoom;
            }

            if(rangex2>elements-1){
                rangex2 = elements - 1;
                rangex1 = rangex2 - current_zoom;
            }

            if(rangex1==rangex2){
                throw new Exception("Cannot have such zoom.");
            }
        }
    }


    public synchronized int getNumberOfSlots() throws Exception{
        int elements = checkConsistenceBetweenAllArrays();
        if (elements<=MINIMUM_ZOOM_ELEMENTS){
            return MINIMUM_ZOOM_ELEMENTS;
        }else{
            return rangex2-rangex1;
        }
    }
    */
    public synchronized int getSize() throws Exception{
        return checkConsistenceBetweenAllArrays();
    }

    public synchronized List getSubList(int index, int rangex1, int rangex2){
        List arrr = curveValues.get(index).subList(rangex1,rangex2);
        return arrr;   
    }

    public String[] getToolTipTextCaptions(){
        return toolTipTextCaptions;
    }
}
