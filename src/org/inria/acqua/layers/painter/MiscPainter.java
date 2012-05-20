package org.inria.acqua.layers.painter;

/**
 * @author mjost
 */
public class MiscPainter {
    public static final int SLOTS_REFERENCE = 100;
    /** Maps '0.4' between 0 and 10 as: 6 (the previous one minus the maximun). */
    public static float mapIntoInverse(float min, float max, float inBetween){
        return max - ((max-min) * inBetween);
    }

}
