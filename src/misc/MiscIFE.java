/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package misc;

import java.util.Calendar;
import java.util.Locale;

/**
 *
 * @author Mauricio
 */
public class MiscIFE {

    public static String getTimeFormattedHigh(Calendar d){
        int year = d.get(Calendar.YEAR);
        int month = d.get(Calendar.MONTH) + 1;
        int day = d.get(Calendar.DAY_OF_MONTH);
        return String.format(Locale.ENGLISH, "%02d", year) + "_" + String.format(Locale.ENGLISH, "%02d", month) + "_" + String.format(Locale.ENGLISH, "%02d", day);
    }
    
    public static String getTimeFormattedLow(Calendar d){
        int hours = d.get(Calendar.HOUR_OF_DAY);
        int minutes = d.get(Calendar.MINUTE);
        return String.format(Locale.ENGLISH, "%02d", hours) + "_" + String.format(Locale.ENGLISH, "%02d", minutes);
    }
}
