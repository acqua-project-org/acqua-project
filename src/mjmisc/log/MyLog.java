
package mjmisc.log;

//import com.google.gson.Gson;
import java.io.FileOutputStream;
//import java.io.PrintWriter;
//import java.io.StringWriter;
//import java.util.Calendar;
//import java.util.Date;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import mjmisc.Misc;

public class MyLog {
    public static boolean DEBUG_MODE = true;

    private static final String MYLOG_NAME = "mylog";
    private static final String MYLOG_EXTENSION = "logger";
    //private static Gson gson = new Gson();

    public static synchronized void startLog(){
        if (DEBUG_MODE){
            try {
                Misc.deleteFilesFrom(MYLOG_EXTENSION, ".");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

//    private static synchronized String getDump(Object obj){
//        String dump = gson.toJson(obj);
//        return "Object \"" + obj.getClass().getSimpleName() + ":" + obj.toString() + "\nReported: >" +
//                "<\nDump:\n" + dump + "\n";
//    }


    private static synchronized String getReport(Object obj, String log, Object... args){
        Date time = Calendar.getInstance().getTime();
        String times = "[" + time.getDate() + "/" + (time.getMonth()+1) + " " + time.getHours() + ":" + time.getMinutes() + ":" + time.getSeconds() + "." + time.getTime()%1000 + "]";
        return  String.format(Locale.ENGLISH, times+log, args);
    }

    public static synchronized void log(Object obj, String log, Object... args){log(MYLOG_NAME, obj, log, args);}
    public static synchronized void logP(Object obj, String log, Object... args){log(getName(obj), obj, log, args);}
    private static synchronized void log(String filename, Object obj, String log, Object... args){
        if (DEBUG_MODE){
            String report = MyLog.getReport(obj, log, args);
            appendToLogFile(report + "\n", filename);
        }
    }

    private static String getName(Object obj){
        return obj.getClass().getSimpleName();
    }
//    public static synchronized void logWithDump(Object obj, String log){logWithDump(MYLOG_NAME, obj, log);}
//    public static synchronized void logPWithDump(Object obj, String log){logWithDump(getName(obj), obj, log);}
//    private static synchronized void logWithDump(String filename, Object obj, String log){
//        if (DEBUG_MODE){
//            String report = MyLog.getDumpedReport(obj, log);
//            appendToLogFile(report + "\n", filename);
//        }
//    }


//    public static synchronized void logWithBacktrace(Object obj, String log){logWithBacktrace(MYLOG_NAME, obj, log);}
//    public static synchronized void logPWithBacktrace(Object obj, String log){logWithBacktrace(getName(obj), obj, log);}
//    private static synchronized void logWithBacktrace(String filename, Object obj, String log){
//        if (DEBUG_MODE){
//            try{
//                throw new Exception("Exception");
//            }catch(Exception e){
//                String str = MyLog.getStackTraceAsString(e);
//                MyLog.log(filename, obj, "Log with backtrace: >" + log + "<.\nBacktrace:\n" + str + "\n\n" );
//            }
//        }
//    }

//    public static synchronized void logWithBacktraceAndDump(Object obj, String log){logWithBacktraceAndDump(MYLOG_NAME, obj, log);}
//    public static synchronized void logPWithBacktraceAndDump(Object obj, String log){logWithBacktraceAndDump(getName(obj), obj, log);}
//    private static synchronized void logWithBacktraceAndDump(String filename, Object obj, String log){
//        if (DEBUG_MODE){
//            try{
//                throw new Exception("Exception");
//            }catch(Exception e){
//                String report = MyLog.getDumpedReport(obj, log);
//                report += MyLog.getStackTraceAsString(e);
//                appendToLogFile(report, filename);
//            }
//        }
//    }

    public static synchronized void appendPToLogFile(Object obj, String report, Object... args){
        if (DEBUG_MODE){
            try {
                FileOutputStream appendedFile =
                        new FileOutputStream(getName(obj) + "." + MYLOG_EXTENSION, true);
                String str = String.format(Locale.ENGLISH, report, args);
                appendedFile.write(str.getBytes());
                appendedFile.close();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static synchronized void appendToLogFile(String report, String logfilename, Object... args){
        if (DEBUG_MODE){
            try {
                String str = String.format(Locale.ENGLISH, report, args);
                Misc.appendToFile(logfilename + "." + MYLOG_EXTENSION, str);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static synchronized void appendToTheFile(String logfilename, String report, Object... args){
        try {
            String str = String.format(Locale.ENGLISH, report, args);
            Misc.appendToFile(logfilename, str);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

//
//    private static synchronized String getStackTraceAsString(Exception exception)
//    {
//        StringWriter sw = new StringWriter();
//        PrintWriter pw = new PrintWriter(sw);
//        pw.print(" [ ");
//        pw.print(exception.getClass().getName());
//        pw.print(" ] ");
//        pw.print(exception.getMessage());
//        exception.printStackTrace(pw);
//        return sw.toString() + "\n";
//    }

}
