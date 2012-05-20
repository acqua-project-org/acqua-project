package org.inria.acqua.layers.painter;

import java.util.ArrayList;

/**
 * @author mjost
 */
public class PainterFactory {
    public static ArrayList<Painter> getNewPainters(int type) throws Exception{
        ArrayList<Painter> arr = new ArrayList<Painter>();
        if (type==CurveElement.TIME_1ARG){
            arr.add(new TimePainter(type));
        }else if (type==CurveElement.BASIC_1ARG ||
                type==CurveElement.BASIC_PLUS_BOX_2ARG ||
                type==CurveElement.BASIC_PLUS_BOX_PLUS_EXTREMES_4ARG
                ){
            arr.add(new CurvePainter(type));
        }else if (type==CurveElement.BASICx2_PLUS_BOX_3ARG){
            arr.add(new CurvePainter(type, CurvePainter.ROLE0));
            arr.add(new CurvePainter(type, CurvePainter.ROLE1));
        }else{
            
        }
        return arr;
    }
}
