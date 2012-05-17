package plugins.anomalydetector;

import java.util.Locale;
import misc.ErrorFunction;


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
            //System.out.printf"i=%3d x1=%5.6f x2=%5.6f x3=%5.6f\n",i, x1,x2,x3);
        }
        return x2;
    }

    public static void main(String args[]){
        float p, x, u, s;

//
//        x = 0; u = 0; s = 1; p = getCDF(x,u,s);
//        System.out.printf "x=%f u=%f s=%f -> p=%s\n", x, u, s, p);
//
//        x = 1; u = 0; s = 1; p = getCDF(x,u,s);
//        System.out.printf "x=%f u=%f s=%f -> p=%s\n", x, u, s, p);
//
//        x = 2; u = 0; s = 1; p = getCDF(x,u,s);
//        System.out.printf "x=%f u=%f s=%f -> p=%s\n", x, u, s, p);
//
//        x = 3; u = 0; s = 1; p = getCDF(x,u,s);
//        System.out.printf "x=%f u=%f s=%f -> p=%s\n", x, u, s, p);
//
//        x = 4; u = 0; s = 1; p = getCDF(x,u,s);
//        System.out.printf "x=%f u=%f s=%f -> p=%s\n", x, u, s, p);
//
//        System.out.println();
//
//        p = 0.49999997f ; u = 0; s = 1; x = getInvCDF(p,u,s);
//        System.out.printf "p=%f u=%f s=%f -> x=%s\n", p, u, s, x);
//
//        p = 0.8413447f ; u = 0; s = 1; x = getInvCDF(p,u,s);
//        System.out.printf "p=%f u=%f s=%f -> x=%s\n", p, u, s, x);
//
//        p = 0.97724986f ; u = 0; s = 1; x = getInvCDF(p,u,s);
//        System.out.printf "p=%f u=%f s=%f -> x=%s\n", p, u, s, x);
//
//        p = 0.9986501f ; u = 0; s = 1; x = getInvCDF(p,u,s);
//        System.out.printf "p=%f u=%f s=%f -> x=%s\n", p, u, s, x);
//
//        p = 0.99996835f ; u = 0; s = 1; x = getInvCDF(p,u,s);
//        System.out.printf "p=%f u=%f s=%f -> x=%s\n", p, u, s, x);
//

        //950 de prob
        //avg 96.666718
        //std 6.216038
        p = 0.95f ; u = 96.666718f; s = 6.216038f; x = getInvCDF(p,u,s);
        System.out.printf(Locale.ENGLISH,"p=%f u=%f s=%f -> x=%s\n", p, u, s, x);




    }

}
