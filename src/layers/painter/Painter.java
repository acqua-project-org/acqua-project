package layers.painter;

import java.awt.Graphics;
import layers.Curve;

public interface Painter {
    public void changeSignal(CurveElement ce, int x1, int x2, int zoom) throws Exception;
    public void paintThis(Curve curve, Graphics g);
    public String getToolTipText(Curve curve, int x, int y);
}
