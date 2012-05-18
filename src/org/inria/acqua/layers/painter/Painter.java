package org.inria.acqua.layers.painter;

import java.awt.Graphics;

import org.inria.acqua.layers.Curve;

public interface Painter {
    public void changeSignal(CurveElement ce, int x1, int x2, int zoom) throws Exception;
    public void paintThis(Curve curve, Graphics g);
    public String getToolTipText(Curve curve, int x, int y);
}
