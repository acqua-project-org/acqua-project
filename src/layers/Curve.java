package layers;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import layers.painter.Painter;

/**
 * This class is graphical and just
 * represents data that is given.
 */
public class Curve extends JPanel implements MouseMotionListener{
    private Color colBackground = Color.white;
    private Color colBorder = Color.black;
    private Color colName = Color.black;
    private Font fontName = new Font("Arial",Font.PLAIN, 14);

    private String caption = "";/* Brief description of the curve. */
    private ArrayList<Painter> painters;

    public Curve(String caption){
        super();
        this.caption = caption;
        Border a = new LineBorder(colBorder,1,true);
        this.setBorder(a);
        this.setBackground(colBackground);
        this.addMouseMotionListener(this);
        this.painters = new ArrayList<Painter>();
    }

    public void addPainters(ArrayList<Painter> e){
        painters.addAll(e);
        this.refreshCurve();
    }

    public void clearPainters(){
        painters.clear();
    }

    public void setCaption(String caption){
        this.caption = caption;
    }

    public String getCaption(){
        return caption;
    }

    public void refreshCurve(){
        repaint();
    }
    
    @Override

    public void paintComponent(Graphics g){


        try{
            super.paintComponent(g);
            for(Painter p: painters){
                p.paintThis(this, g);
            }


            Graphics2D g2d = (Graphics2D)g;

            /* Name of the curve. */
            g.setFont(fontName);
            g.setColor(colName);
            Rectangle2D rec = fontName.getStringBounds(caption, g2d.getFontRenderContext());
            int x = (int)rec.getMaxX();
            g.drawString("" + caption, (this.getWidth() - x)/2,10+  fontName.getSize()/2);

        }catch(Exception e){
           System.err.println("Curve got: " + e.getMessage());
        }
        
    }

    public void mouseDragged(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void mouseMoved(MouseEvent e) {
        String str = "";
        for (Painter p:painters){
            str += p.getToolTipText(this, e.getX(), e.getY()) + " ";
        }
        this.setToolTipText(str);
    }
}
