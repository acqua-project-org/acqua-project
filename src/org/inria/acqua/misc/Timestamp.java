package org.inria.acqua.misc;

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import org.inria.acqua.exceptions.IllegalComparisonException;
import org.inria.acqua.mjmisc.MiscDate;

public class Timestamp {
    public static final String TIMEOUT = "timeout"; /* Used to generate new Timestamp with timeout. */

    private double seconds;
    private boolean timeout;

    public Timestamp(){}
    
    public static double getDifferenceInMS(Timestamp ts_after, Timestamp ts_before)
            throws IllegalComparisonException {
        if(ts_after.timeout || ts_before.timeout){
            throw new IllegalComparisonException("Comparing timed out timestamps.");
        }else{
            return (double)((ts_after.seconds - ts_before.seconds)*1000);
        }
    }
    
    public Timestamp(String t) throws ParseException{ /* A seconds.fraction is coming. */
        if (TIMEOUT.equals(t)){
            seconds = 0.0f;
            timeout = true;
        }else{
            seconds = Double.parseDouble(t);
            timeout = false;
        }
    }


    public Timestamp(Date t){
        if (t==null){
            throw new IllegalArgumentException("ERROR: Cannot use null date.");
        }
        seconds = (double)((double)t.getTime()/1000.0f);
        timeout = false;
    }


    @Override
    public String toString(){ /* Format is 'ss.ff' where ss is seconds from epoch, and ff fractions. */
        if (timeout){
            return TIMEOUT;
        }else{
            return String.format(Locale.ENGLISH, "%1.3f", (double)seconds);
        }
    }

    public String toHumanReadableString(){
        if (timeout){
            return TIMEOUT;
        }else{
            return MiscDate.generateHumanReadableString(new Date((long)(seconds*1000)));
        }
    }

}
