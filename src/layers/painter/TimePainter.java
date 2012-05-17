
package layers.painter;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.List;
import layers.Curve;
import misc.Timestamp;

public class TimePainter implements Painter{
    private int numberOfSlots = 40;
    private List<Timestamp> signalTime;
    private int referenceY;
    private int sampleHeight;
    private float sampleWidth;
    private Color colDate = new Color(40,40,40);
    private Color colAxes = new Color(210,210,210);
    private Font fontDate = new Font("Arial", Font.PLAIN, 11);

    private int type;
    private String[] toolTipTextCaptions = {};

    public TimePainter(int type) throws Exception{
        if (type!=CurveElement.TIME_1ARG){
            throw new Exception("Not supported type " + type +".");
        }
        this.type = type;
    }
    private void changeSignalTime(List dates){
        this.signalTime = dates;
    }


    /** Draw a line using abstract references. */
    public void drawLine(Graphics g, float x1, float x2, float y1, float y2, int thickness){
        int referenceYSup = referenceY - sampleHeight;
        int x1r, y1r, x2r, y2r;
        x1r = (int)(x1 * sampleWidth);
        y1r = (int)MiscPainter.mapIntoInverse(referenceYSup, referenceY, y1);
        x2r = (int)(x2* sampleWidth);
        y2r = (int)MiscPainter.mapIntoInverse(referenceYSup, referenceY, y2);
        for (int i=0;i<thickness;i++){
            g.drawLine(x1r+i,y1r,x2r+i,y2r);
            g.drawLine(x1r-i,y1r,x2r-i,y2r);
            g.drawLine(x1r,y1r+i,x2r,y2r+i);
            g.drawLine(x1r,y1r-i,x2r,y2r-i);
        }
            /* Reference vertical line. */
    }

    
    
    private void establishReferences(Curve curve, int number_of_elements){
        referenceY = curve.getHeight()-10;    /* Y base value. */
        sampleHeight = curve.getHeight()-20; /* Size of one sample in Y. */
        sampleWidth =                       /* Size of one sample in X. */
            (float)(curve.getWidth())/(number_of_elements-1);
    }

    public void paintThis(Curve curve, Graphics g) {
        /* If we have curve, use its length, if we have time, use its length. */
        int number_of_elements = (signalTime.size());
        int number_of_slots = numberOfSlots;
        establishReferences(curve, number_of_slots); /* Like Yreference, etc. */



        /* Paint the signal lines. */

        for (int j = 0; j<number_of_elements-1;j++){
            int sloti = number_of_slots - number_of_elements + j;
            int inter_time = Math.max((int)((float)number_of_slots/10),1);
            if (j%inter_time==0){
                g.setFont(fontDate);
                g.setColor(colDate);
                if (signalTime.get(j)!=null){
                    drawDiagonalString(g, signalTime.get(j).toHumanReadableString(),
                        sloti, referenceY-sampleHeight + 5);
                }
                g.setColor(colAxes);
                this.drawLine(g, sloti, sloti, (float)0.6, 1,1);
            }
        }
    }

    public void drawDiagonalString(Graphics g, String str, float x, float y){
        int i;
        for (i=0;i<str.length();i++){
            int aux = (int)((g.getFont().getSize()) * 0.8);
            g.drawString(str.charAt(i) + "", (int)(x * sampleWidth + (int)(i*aux/1.2)), (int)(y + (int)(i*aux/3.00)));
        }
    }

    public String getToolTipText(Curve curve, int x, int y){
        String ret = "<no details here>";
        String strAux = "";

        try{
            int sampleSlot = (int)(((float)x+((float)sampleWidth/2))/sampleWidth);
            int number_of_values = this.signalTime.size();

            int index = sampleSlot - (numberOfSlots - number_of_values);
            /* Signal. */
            strAux = strAux + "" + this.toolTipTextCaptions[0] + signalTime.get(index).toHumanReadableString() + "   ";

            ret = strAux;
        }catch(Exception e){
            //System.out.println("No information here: " + e.getMessage());
        }
        return ret;
    }

    public void changeSignal(CurveElement ce, int x1, int x2, int slots) throws Exception {
        if (ce.getType()!=type){
            throw new Exception("Trying to change type from " + type + " to " + ce.getType() + ".");
        }
        this.numberOfSlots = slots;
        this.changeSignalTime(ce.getSubList(0, x1, x2));
        this.toolTipTextCaptions = ce.getToolTipTextCaptions();
    }
}
