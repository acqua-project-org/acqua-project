
package org.inria.acqua.layers;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.inria.acqua.layers.painter.CurveElement;
import org.inria.acqua.layers.painter.Painter;
import org.inria.acqua.layers.painter.PainterFactory;


/**
 * This class is in charge of giving to the curve the right information
 * according to the signature that it has. The signature tells what should be printed
 * in the curve (time, curve, curve+statistics, etc.).
 * This is the nexum between the External Module and one Curve.
 * @author mjost
 */
public class ChannelController implements Serializable{

    transient private Curve curve;
    private String signature = null;
    private int type = CurveElement.UNINITIALIZED;
    private ArrayList<Painter> painters;
    private String title;

    public ChannelController(String name, Curve curve){
        this.curve = curve;
        this.setSignature(name);
    }

    public String getTitle() {
        return title;
    }

    public void setCurve(Curve c){
        curve = c;
    }

    public String getSignature(){
        return signature;
    }


    public void setSignature(String signatur){
        signatur = signatur.trim();
        if (!signatur.equals(signature)){
            signature = signatur;
        }
    }


    public void refreshCurve(HistoryData history, Integer paramA, Integer paramB) throws Exception{
        
        HashMap<String, CurveElement> allce = history.getCurveElements();
        CurveElement ce = allce.get(signature);

        if (type!= ce.getType()){
            this.title = ce.getTitle();
            this.curve.setCaption(title);
            type = ce.getType();
            painters = PainterFactory.getNewPainters(type);
            this.curve.clearPainters();
            this.curve.addPainters(painters);
        }


        int x1, x2, slots;
        int size = ce.getSize();
        if (paramA >= ce.getSize() || paramB >= ce.getSize()){
            x1 = 0;
            x2 = size-1;
            slots = 50;
        }else{
            x1 = size - paramB;
            x2 = size - paramA;
            slots = paramB - paramA;
        }

        for(Painter p:painters){
            p.changeSignal(ce, x1, x2, slots);
        }
        curve.refreshCurve();

    }

}
