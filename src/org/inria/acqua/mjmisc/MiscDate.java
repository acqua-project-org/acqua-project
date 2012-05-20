package org.inria.acqua.mjmisc;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MiscDate {
    
    private static SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS 'GMT' Z");

    public static String generateHumanReadableString(Date date){
        return formatter.format(date);
    }

    public static Date parseHumanReadableString(String str) throws ParseException{
        return formatter.parse(str);
    }

}
