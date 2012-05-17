package layers.painter;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.List;
import java.util.Locale;
import layers.Curve;

public class CurvePainter implements Painter{
    private static final int ADD = 0;
    private static final int COMPARE = 1;
    private static final int SUBSTRACT = 2;

    public static final int ROLE0 = 0;
    public static final int ROLE1 = 1;

    private int numberOfSlots = 40;

    private static final float HEIGHT_RATIO_FACTOR = (float)0.2;
    private float BOX_WIDTH = 0.4f;

    private List<Float> signalBase;
    private List<Float> signalBoxes;
    private List<Float> signalMinimum;
    private List<Float> signalMaximum;
    
    /** Determine whether certain parts will be printed or not. */
    private boolean printBoxes = false;
    private boolean printMinimumMaximum = false;

    private int referenceY;
    private int sampleHeight;
    
    private float sampleWidth;
    private Float baseReferenceToPaint = null;
    private Float forcedMinimum = null;
    private Float forcedMaximum = null;
    
    private Font fontReferences = new Font("Arial", Font.PLAIN, 12);
    
    private Color colBaseReference = new Color(220,220,220);
    private Color colLimits = new Color(220,220,220);
    private Color colSignal = new Color(40,40,200);
    private Color colName = new Color(40,40,40);
    private Color colExtremes = new Color(200,40,40);
    private Color colBoxes = new Color(40,200,40);
    
    private int type;
    private int role;
    private String[] toolTipTextCaptions = {};

    public CurvePainter(int type, int role) throws Exception{
        switch(type){
            case CurveElement.BASIC_1ARG:
                break;
            case CurveElement.BASIC_PLUS_BOX_2ARG:
                colSignal = Color.red;
                break;
            case CurveElement.BASIC_PLUS_BOX_PLUS_EXTREMES_4ARG:
                break;
            case CurveElement.BASICx2_PLUS_BOX_3ARG:
                if (role==ROLE0){
                    colSignal = Color.green;
                }else{
                    colSignal = Color.red;
                }
                break;
            default:
                throw new Exception("Type not supported (" + type + ").");
        }
        
        this.type = type;
        this.role = role;
    }

    public CurvePainter(int type) throws Exception{
        this(type, ROLE0);
    }

    
    /**
     * The following method is used when one wants to print the simplest curve.
     * It is a curve, its limits, its comment/caption/description.
     */
    private void changeSignalBasic(List signal){
        printBoxes = false;
        printMinimumMaximum = false;
        this.signalBase = signal;
    }

    /** Simplest curve + boxes. Boxes values are from the base to the peak. */
    private void changeSignalWithBoxes(List base, List boxesVal){
        printBoxes = true;
        printMinimumMaximum = false;
        this.signalBase = base;
        this.signalBoxes = boxesVal;        
    }

    /** Signal statistics + maximun/minimun. */
    private void changeSignalComplete(List base, List boxes,
            List min, List max){
        printBoxes = true;
        printMinimumMaximum = true;

        this.signalBase = base;
        this.signalBoxes = boxes;
        this.signalMinimum = min;
        this.signalMaximum = max;
    }
    
    /**
     * This establish references that will be used
     * while plotting.
     */
    private void establishReferences(Curve curve, int number_of_elements){
        referenceY = curve.getHeight()-10;    /* Y base value. */
        sampleHeight = curve.getHeight()-20; /* Size of one sample in Y. */
        sampleWidth =                       /* Size of one sample in X. */
            (float)(curve.getWidth())/(number_of_elements-1);
    }


    public void paintThis(Curve curve, Graphics g) {
        try{

            /* If we have curve, use its length, if we have time, use its length. */

            int number_of_elements = (signalBase!=null?signalBase.size():0);
            int number_of_slots = numberOfSlots;
            
            //g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            establishReferences(curve, number_of_slots); /* Like Yreference, etc. */

            /* Y arrow reference. */
            g.setColor(Color.black);
            this.drawLine(g, 1, 1, 0, 1, 1);
            this.drawLine(g, 1, (float)0.9, 1, (float)0.9, 1);
            this.drawLine(g, 1, (float)1.1, 1, (float)0.9, 1);


            g.setColor(colLimits);
            this.drawLine(g,
                        0, number_of_slots-1,
                        1, 1 ,1);
            this.drawLine(g,
                        0, number_of_slots-1,
                        0.75f, 0.75f ,1);
            this.drawLine(g,
                        0, number_of_slots-1,
                        0.5f, 0.5f ,1);
            this.drawLine(g,
                        0, number_of_slots-1,
                        0.25f, 0.25f ,1);
            this.drawLine(g,
                        0, number_of_slots-1,
                        0, 0 ,1);
            

            Float min = 0.0f;
            Float max = 0.0f;
            if (number_of_elements>0){
                
                Float[] minmax = calculateMaximumMinimum();
                min = minmax[0];
                max = minmax[1];

                /* Paint the signal lines. */
                for (int j=0; j<number_of_elements;j++){

                    int sloti = number_of_slots - number_of_elements + j;
                    /* Reference signal. Usually it will be requested to paint the '0' line. */
                    if (baseReferenceToPaint!=null){ /* If there is a reference ("paint the 0 as reference"). */
                        g.setColor(colBaseReference);
                        this.drawLine(g,
                                sloti, sloti+1,
                                mapIntoAbsolute(min, max, baseReferenceToPaint), mapIntoAbsolute(min, max, baseReferenceToPaint),
                                1);
                    }

                    /* Vertical lines of minimun/maximun per sample. */
                    if (printMinimumMaximum){
                        float ysup = this.mapIntoAbsolute(min, max, signalMaximum.get(j));
                        float yinf = this.mapIntoAbsolute(min, max, signalMinimum.get(j));
                        g.setColor(colExtremes);
                        this.drawLine(g, sloti, sloti, ysup, yinf, 1);

                        this.drawLine(g, sloti-BOX_WIDTH/2, sloti+BOX_WIDTH/2, ysup, ysup, 1);
                        this.drawLine(g, sloti-BOX_WIDTH/2, sloti+BOX_WIDTH/2, yinf, yinf, 1);
                    }

                    /* Boxes of Standard Deviation per sample. */
                    if ((printBoxes)){

                        g.setColor(colBoxes);
                        drawRectangle(g, sloti, mapIntoAbsolute(min, max, signalBase.get(j)), BOX_WIDTH, 2*signalBoxes.get(j) / (max-min),1);
                    }

                    /* Signal. */
                    g.setColor(colSignal);

                    if (j!=number_of_elements-1){
                        this.drawLine(g,
                                sloti, sloti+1,
                                mapIntoAbsolute(min, max, signalBase.get(j)), mapIntoAbsolute(min, max, signalBase.get(j+1)),
                                2);
                    }else{
                        this.drawLine(g,
                                sloti, sloti,
                                -1+mapIntoAbsolute(min, max, signalBase.get(j)), mapIntoAbsolute(min, max, signalBase.get(j))+1,
                                2);
                    }
                }
            }
            String format = "%1.2f";
            g.setFont(fontReferences);
            g.setColor(colName.brighter().brighter());

            /* Upperbound of the curve. */
            g.drawString(String.format(Locale.ENGLISH,format, max), 2 * (int)sampleWidth,
                    referenceY - sampleHeight + fontReferences.getSize()/2);

            /* Upper-middle part of the curve. */
            g.drawString(String.format(Locale.ENGLISH, format, (((max-min)/4.0f)*3.0f+min)), 2 * (int)sampleWidth,
                    referenceY - (int)(((float)sampleHeight/4.0f) * 3.0f) + fontReferences.getSize()/2);

            /* Middle part of the curve. */
            g.drawString(String.format(Locale.ENGLISH, format, ((max-min)/2+min)), 2 * (int)sampleWidth,
                    referenceY - (int)(sampleHeight/2) + fontReferences.getSize()/2);

            /* Lower-middle part of the curve. */
            g.drawString(String.format(Locale.ENGLISH, format, ((max-min)/4.0f+min)), 2 * (int)sampleWidth,
                    referenceY - (int)((float)sampleHeight/4) + fontReferences.getSize()/2);

            /* Lowerbound of the curve. */
            g.drawString(String.format(Locale.ENGLISH, format, (min)), 2 * (int)sampleWidth,
                    referenceY + (int)fontReferences.getSize()/2);


        }catch(Exception e){
           System.err.println("CurvePainter exception: " + e.getMessage());
           e.printStackTrace();
        }
    }


    private Float[] calculateMaximumMinimum(){

        Float max = null; /* This is used to scale the curves in Y (avoid saturating curve). */
        if (forcedMaximum==null){
            Float max2 = null; /* This is used to scale the curves in Y (avoid saturating curve). */
            if (printMinimumMaximum){
                max = calculateMax(signalBase, signalMaximum, COMPARE);
            }else if (this.printBoxes){
                max = calculateMax(signalBase, signalBoxes, ADD);
                max2 = calculateMax(signalBase, signalMaximum, ADD);
                max = Math.max(max, max2);
            }else{
                max = calculateMax(signalBase, null, ADD);
            }

        }else{
            max = forcedMaximum;
        }

        Float min = null; /* This is used to scale the curves in Y (avoid saturating curve). */
        if (forcedMinimum==null){
            Float min2 = null; /* This is used to scale the curves in Y (avoid saturating curve). */

            if (printMinimumMaximum){
                min = calculateMin(signalBase, signalMinimum, COMPARE);
            }else if (this.printBoxes){
                min = calculateMin(signalBase, signalBoxes, SUBSTRACT);
                min2 = calculateMin(signalBase, signalMinimum, ADD);
                min = Math.min(min, min2);
            }else{
                min = calculateMin(signalBase, null, SUBSTRACT);
            }

        }else{
            min = forcedMinimum;
        }

        if (forcedMinimum==null && forcedMaximum==null){ /* Nothing forced. */
            Float range = Math.max(0.001f, max - min);
            min = min - range * (HEIGHT_RATIO_FACTOR);
            max = max + range * (HEIGHT_RATIO_FACTOR);
        }else if(forcedMinimum!=null && forcedMaximum==null){ /* Minimum forced. */
            min = forcedMinimum;
            Float range = Math.max(0.001f, max - min);
            max = max + range * (HEIGHT_RATIO_FACTOR);
        }else if(forcedMinimum==null && forcedMaximum!=null){ /* Maximum forced. */
            max = forcedMaximum;
            Float range = Math.max(0.001f, max - min);
            min = min - range * (HEIGHT_RATIO_FACTOR);
        }else{ /* Forced both. */

        }

        Float[] ret = {min, max};
        return ret;
    }


    /** Draw a line using abstract references. */
    private void drawLine(Graphics g, float x1, float x2, float y1, float y2, int thickness){
        int referenceYSup = referenceY - sampleHeight;
        int x1r, y1r, x2r, y2r;
        x1r = (int)(x1 * sampleWidth);
        y1r = (int)mapIntoInverse(referenceYSup, referenceY, y1);
        x2r = (int)(x2* sampleWidth);
        y2r = (int)mapIntoInverse(referenceYSup, referenceY, y2);
        for (int i=0;i<thickness;i++){
            g.drawLine(x1r+i,y1r,x2r+i,y2r);
            g.drawLine(x1r-i,y1r,x2r-i,y2r);
            g.drawLine(x1r,y1r+i,x2r,y2r+i);
            g.drawLine(x1r,y1r-i,x2r,y2r-i);
        }
            /* Reference vertical line. */
    }


    /** Draw a rectangle using abstract references. */
    public void drawRectangle(Graphics g, float x, float y, float width, float height, int thickness){
        int xr = (int)mapInto(0, sampleWidth, x - width/2);
        int yr = (int)mapIntoInverse(referenceY - sampleHeight, referenceY, y + height/2);
        int wir = (int)mapInto(0,sampleWidth,width);
        int her = (int)mapInto(0,sampleHeight,height);
        for (int i=0; i<thickness; i++){
            g.drawRect(xr+i, yr, wir, her);
            g.drawRect(xr-i, yr, wir, her);
            g.drawRect(xr, yr+i, wir, her);
            g.drawRect(xr, yr-i, wir, her);
        }
    }


    /** Maps '5' between 0 and 10 as: 0.5. */
    private float mapIntoAbsolute(float min, float max, float inBetween){
        float range = max - min;
        float inRange = inBetween - min;
        return inRange / range;
    }

    /** Maps '0.4' between 0 and 10 as: 4. */
    private float mapInto(float min, float max, float inBetween){
        return min + ((max-min) * inBetween);
    }

    /** Maps '0.4' between 0 and 10 as: 6 (the previous one minus the maximun). */
    private float mapIntoInverse(float min, float max, float inBetween){
        return max - ((max-min) * inBetween);
    }

    /** Calculates the maximum value in several ways to use when plotting. */
    private Float calculateMax(List<Float> value1, List<Float> value2, int mode){
        int i;
        float max = Float.MIN_VALUE;

        for (i=0; i<value1.size(); i++){
            if (mode == ADD){
                if (value2!=null){
                    if (i==0 || (value1.get(i)+value2.get(i) > max)){
                        max = value1.get(i) + value2.get(i);
                    }
                }else{
                    if (i==0 || (value1.get(i) > max)){
                        max = value1.get(i);
                    }
                }
            }else if (mode == COMPARE){
                if (value2!=null){
                    if (i==0 || (value1.get(i)>max) || (value2.get(i) > max)){
                        max = Math.max(value1.get(i), value2.get(i));
                    }
                }else{
                    if (i==0 || (value1.get(i) > max)){
                        max = value1.get(i);
                    }
                }
            }
        }

        return (float)(max);
    }


    /** Calculates the minimum value in several ways to use when plotting. */
    private Float calculateMin(List<Float> value1, List<Float> value2, int mode){
        int i;
        float min = Float.MAX_VALUE;

        for (i=0; i<value1.size(); i++){
            if (mode == ADD){
                if (value2!=null){
                    /* The second value is suppossed to be absolute. */
                    if (i==0 || (value1.get(i) + value2.get(i) < min)){
                        min = value1.get(i) + value2.get(i);
                    }
                }else{
                    if (i==0 || (value1.get(i) < min)){
                        min = value1.get(i);
                    }
                }
            }else if (mode == SUBSTRACT){
                if (value2!=null){
                    /* The second value is suppossed to be absolute. */
                    if (i==0 || (value1.get(i) - value2.get(i) < min)){
                        min = value1.get(i) - value2.get(i);
                    }
                }else{
                    if (i==0 || (value1.get(i) < min)){
                        min = value1.get(i);
                    }
                }
            }else if (mode == COMPARE){
                if (value2!=null){
                    if (i==0 || (value1.get(i)<min) || (value2.get(i) < min)){
                        min = Math.min(value1.get(i), value2.get(i));
                    }
                }else{
                    if (i==0 || (value1.get(i) < min)){
                        min = value1.get(i);
                    }
                }
            }
        }

        return (float)(min);

    }


    public String getToolTipText(Curve curve, int x, int y) {

        String ret = "<no details here>";
        String strAux = "";
        

        try{
            int sampleSlot = (int)(((float)x+((float)sampleWidth/2))/sampleWidth);
            int number_of_values = this.signalBase.size();

            int index = sampleSlot - (numberOfSlots - number_of_values);
            /* Signal. */

            if (type== CurveElement.BASICx2_PLUS_BOX_3ARG){
                if (role==ROLE0){
                    strAux = strAux + "" + this.toolTipTextCaptions[0] + signalBase.get(index) + "   ";
                }else{
                    strAux = strAux + "" + this.toolTipTextCaptions[2] + signalBase.get(index) + "   ";
                }
            }else{
                strAux = strAux + "" + this.toolTipTextCaptions[0] + signalBase.get(index) + "   ";
            }



            

            /* Boxes of Standard Deviation. */
            if ((printBoxes)){
                strAux = strAux + this.toolTipTextCaptions[1] + signalBoxes.get(index) + "   ";
            }

            /* Maximum and minimum. */
            if (printMinimumMaximum){
                strAux = strAux + this.toolTipTextCaptions[2] + signalMinimum.get(index) + " " + this.toolTipTextCaptions[3] + signalMaximum.get(index) + " ";
            }
            ret = strAux;
        }catch(Exception e){
            //System.out.println("No information here: " + e.getMessage());
        }
        return ret;
    }


    public void changeSignal(CurveElement ce, int x1, int x2, int slots) throws Exception{
        if (ce.getType()!=type){
            throw new Exception("Trying to change type from " + type + " to " + ce.getType() + ".");
        }

        this.numberOfSlots = slots;
        
        switch(ce.getType()){
            case CurveElement.BASIC_1ARG:
                this.changeSignalBasic(
                        ce.getSubList(0, x1, x2)
                        );
                break;
            case CurveElement.BASIC_PLUS_BOX_2ARG:
                this.changeSignalWithBoxes(
                        ce.getSubList(0, x1, x2),
                        ce.getSubList(1, x1, x2)
                        );
                break;
            case CurveElement.BASIC_PLUS_BOX_PLUS_EXTREMES_4ARG:
                this.changeSignalComplete(
                        ce.getSubList(0, x1, x2),
                        ce.getSubList(1, x1, x2),
                        ce.getSubList(2, x1, x2),
                        ce.getSubList(3, x1, x2)
                        );
                break;
            case CurveElement.BASICx2_PLUS_BOX_3ARG:
                if (role==ROLE0){
                    this.changeSignalWithBoxes(
                        ce.getSubList(0, x1, x2),
                        ce.getSubList(1, x1, x2)
                        );
                }else{
                    this.changeSignalBasic(
                        ce.getSubList(2, x1, x2)
                        );
                }

                break;
            default:
                throw new Exception("CurveElement type not supported (" + ce.getType() + ").");
        }
        this.baseReferenceToPaint = ce.getBaseReferenceToPaint();
        this.forcedMinimum = ce.getForcedMinimum();
        this.forcedMaximum = ce.getForcedMaximum();
        this.toolTipTextCaptions = ce.getToolTipTextCaptions();
    }



}
