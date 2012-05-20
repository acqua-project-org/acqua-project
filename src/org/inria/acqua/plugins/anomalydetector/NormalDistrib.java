package org.inria.acqua.plugins.anomalydetector;

import java.util.Locale;

import org.inria.acqua.misc.ErrorFunction;

/**
 * @author mjost
 */
public class NormalDistrib {

    public static float getPDF(float x, float u, float s){
        return (float) ((1 / (Math.sqrt(2.0f * Math.PI * s * s))) * Math.exp(-1.0f * (x - u) * (x - u) / (2.0f * s * s)));
    }

    public static float getCDF(float x, float u, float s){
        return (float) (0.5f + 0.5f * ErrorFunction.erf((x-u)/Math.sqrt(2.0f*s*s)));
    }

    public static float getInvCDF(float p, float u, float s){
        float x1 = 0, x2 = 5000, x3 = 10000;
        float p2 = 0.0f;
        
        for (int i=0; i<55; i++){
            p2 = getCDF(x2,u,s);
            if (p2 < p){    /* p1        p2   p     p3 */
                x1 = x2;
                x2 = x1 + ((x3 - x1)/2.0f);
            }else{          /* p1    p   p2        p3 */
                x3 = x2;
                x2 = x1 + ((x3 - x2)/2.0f);
            }
        }
        return x2;
    }

}
