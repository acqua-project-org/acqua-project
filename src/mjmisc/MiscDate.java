package mjmisc;

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

    public static void main(String args[]) throws ParseException{
        Date now = Calendar.getInstance().getTime();

        String str = MiscDate.generateHumanReadableString(now);
        System.out.println("Generated string ini: " + str + " is " + now.getTime());
        Date later = MiscDate.parseHumanReadableString(str);
        str = MiscDate.generateHumanReadableString(later);
        System.out.println("Generated string lat: " + str);
    }

}
