
package org.inria.acqua.misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import org.apache.log4j.Logger;

public class MiscMatlab {
	private static Logger logger = Logger.getLogger(MiscMatlab.class.getName()); 

    public static ArrayList<Double> cloneArray(ArrayList<Double> values){
        return (ArrayList<Double>)values.clone();
    }



    public static ArrayList<Double> removeNegatives(ArrayList<Double> values){
        ArrayList<Double> result = new ArrayList<Double>();
        for (int i=0;i<values.size(); i++){
            if (values.get(i)!=null && values.get(i)>=0.0){
                result.add(values.get(i));
            }
        }

        return result;
        
    }

    public static double getMedian(ArrayList<Double> values){
        //print("     values " + values.get(0) + " " + values.get(1) + " " + values.get(2) + " " + values.get(3) + " " + values.get(4));
        if (values.size() == 0){
            throw new IllegalArgumentException("The size of the array should be at least 1.");
        }
        Collections.sort(values);

        if (values.size() % 2 == 1){
            return values.get((values.size()+1)/2-1);
        }else{
            double lower = values.get(values.size()/2-1);
            double upper = values.get(values.size()/2);

            return (lower + upper) / 2.0;
        }
    }


    public static double binomialFunction(int ksuccess, int trials, double alphasuccess){
        double combi;
        combi = factorial(trials) / (factorial(ksuccess) * factorial(trials-ksuccess));
        return combi * (Math.pow(alphasuccess,ksuccess)) * (Math.pow((1-alphasuccess),(trials-ksuccess)));
    }

    public static long factorial(int n)
    {
        long ret = 1;
        for (int i = 1; i <= n; ++i) ret *= i;
        return ret;
    }

    public static <T> ArrayList<T> getThoseGreaterThan(ArrayList<T> values, double threshold){
        ArrayList<T> resul = new ArrayList<T>();
        for (int i=0;i<values.size();i++){
            Number num = ((Number)values.get(i));
            if (num!=null){
                if (num.doubleValue()>threshold){
                    resul.add((T)(Double)num.doubleValue());
                }
            }
        }
        return resul;
    }
    public static <T> ArrayList<T> getThoseLessThan(ArrayList values, double threshold){
        ArrayList<T> resul = new ArrayList<T>();
        for (int i=0;i<values.size();i++){
            Number num = ((Number)values.get(i));
            if (num!=null){
                if ((num.doubleValue())<threshold){
                    resul.add((T)(Double)num.doubleValue());
                }
            }
        }
        return resul;
    }

    public static double getSum(ArrayList<Double> values){
        double sum = 0;
        for (int i=0;i<values.size();i++){
            sum += values.get(i);
        }
        return sum;
    }

    public static double getMax(ArrayList<Double> values){
        double val = 0;
        for (int i=0;i<values.size();i++){
            if (i==0 || values.get(i) > val){
                val = values.get(i);
            }
        }
        return val;
    }

    public static double getMin(ArrayList<Double> values){
        double val = 0;
        for (int i=0;i<values.size();i++){
            if (i==0 || values.get(i) < val){
                val = values.get(i);
            }
        }
        return val;
    }

    public static double getMean(ArrayList<Double> values){
        double sum = 0;
        sum = getSum(values);
        return sum / values.size();
    }

    public static double getStd(ArrayList<Double> values){
        double mean = getMean(values);

        double sum = 0;
        for (int i=0;i<values.size();i++){
            sum += Math.pow((mean - values.get(i)),2.0);
        }
        return Math.sqrt(sum / values.size());
        
    }



    public static double getVariabilityIndex(ArrayList<Double> values, int ntaps){

        ArrayList<Double> arrr;
        double rr = 0;
        int count = 0;
        
        for (int i=ntaps-1; i<values.size(); i++){
            arrr = new ArrayList(values.subList(i-(ntaps-1),i+1));

            rr += MiscMatlab.getStd(arrr);
            count++;

        }

        return rr/count;
    }

    
    public static ArrayList<Double> medianFilter(ArrayList<Double> values, int ntaps){
        ArrayList<Double> result = new ArrayList<Double>(values.size());

        if (ntaps < 1){ntaps = 1;}
        
        if (values.size() < ntaps){
            /* No filtering here. */
            for (int i=0; i<values.size(); i++){
                //print( "\t\tindex: " + i);
                result.add(i, values.get(i));
            }
            return result;
        }

        /* The amount of ntaps is bigger than the size of the array. */
        for (int i=0; i<ntaps-1; i++){
            /* For the first ntaps-1 elements we cannot do more than give them rawly. */
            result.add(i, values.get(i));
        }

        ArrayList<Double> arrr;
        for (int i=ntaps-1; i<values.size(); i++){
            //print( "\t\tindex: " + i);
            int x1 = i - (ntaps-1);
            int x2 = i + 1;
            //print("[MedianFilter]x1=" + x1 + ", x2=" + x2);
            arrr = new ArrayList(values.subList(x1,x2));

            double rr = MiscMatlab.getMedian(arrr);
            //print("        " + rr);
            result.add(i, rr);
        }

        return result;
    }


    public static void main(String args[]){
        ArrayList<Double> aa = new ArrayList<Double>();
        //Random rnd = new Random();

        

        // [0 1 3 4 5 9 9 21 1 2 2 7 99 1] for test in Matlab.

        printArray(aa);
        
        ArrayList<Double> bb = medianFilter(aa, 0);
        printArray(bb);
        double res;
        res = getMedian(aa);
        print("median = " + res);
        res = getMean(aa);
        print("mean = " + res);
        res = getStd(aa);
        print("std = " + res);

        /*
        
        0.0 1.0 3.0 4.0 3.0 4.0 5.0 9.0 9.0 9.0 2.0 2.0 2.0 2.0
        median = 3.5
        mean = 11.714285714285714
        std = 25.709706551981707
        
        */

    }

    public static void printArray(ArrayList arr){
        logger.info(" " );
        for(int i=0;i<Math.min(arr.size(),30);i++){
            logger.info(String.format(Locale.ENGLISH, "\t%1.2f", arr.get(i)));
        }
        logger.info(" ");
    }

    public static synchronized void print(String r){
        logger.info(r);
    }

    
}


