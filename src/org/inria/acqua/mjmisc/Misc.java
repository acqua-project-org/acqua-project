package org.inria.acqua.mjmisc;

import java.net.UnknownHostException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.inria.acqua.mjmisc.exceptions.OSNotSupportedException;


/** 
 * This class provides several functionalities. 
 * @author mjost
 */
public class Misc {

	private static Logger logger = Logger.getLogger(Misc.class.getName()); 
    public static final int OS_UNINITIALIZED = -1;
    public static final int OS_UNKNOWN = 0;
    public static final int OS_UNIX = 1;
    public static final int OS_MAC = 2;
    public static final int OS_WINDOWS = 3;

    private static String hostname = null;
    private static Long timeDifference_hereminusthere = null;

    private Misc(){}

    /** Get elements of 'z' that are not present in 'b'. */
    public static AbstractCollection getDifference_ZsNotPresentInB(AbstractCollection z, AbstractCollection b){
        AbstractCollection ret = new ArrayList<Object>();
        for(Object o: z){
            if (!b.contains(o)){
                ret.add(o);
            }
        }
        return ret;
    }
    
    public static String printSpecified(Object o){
        String output = "";
        if (o instanceof ArrayList){
            ArrayList a = (ArrayList) o;
            for(Object i:a){
                output = output + i + " ";
            }
        }else if(o instanceof int[]){
            int[] a = (int[]) o;
            for (int i=0;i<a.length;i++){
                output = output + a[i] + " ";
            }
        }else if(o instanceof float[]){
            float[] a = (float[]) o;
            for (int i=0;i<a.length;i++){
                output = output + a[i] + " ";
            }
        }

        return output;
    }
    //public static int executeCall(String call) throws Exception {

    public static void main(String args[]) throws Exception{

        ArrayList<String> set1 = new ArrayList<String>();
        ArrayList<String> set2 = new ArrayList<String>();

        set1.add("A");
        set1.add("B");
        set1.add("C");

        //set2.add("A");
        set2.add("B");
        set2.add("C");

        AbstractCollection al = Misc.getDifference_ZsNotPresentInB(set1, set2);

        Misc.printCollection(al);


        System.exit(0);

        

        System.exit(0);

        ArrayList<String> landm = new ArrayList<String>();

        
        landm.add("C");
        landm.add("D");
        landm.add("A");
        landm.add("E");
        landm.add("A");

        if(Misc.areThereEqualElements(landm)!=null){
            logger.info("Hay iguales.");
        }else{
            logger.info("Todos diferentes.");
        }

        logger.info("BEFORE");
        for(String s: landm){
            logger.info(" " + s);
        }
        logger.info("");
        
        landm = Misc.removeRepeated(landm);

        logger.info("AFTER");
        for(String s: landm){
            logger.info(" " + s);
        }
        logger.info("");
    }

    public static ProcessOutput executeCallGetOutput(String command, String arguments) throws IOException{
        return Misc.executeCallGetOutput(command, arguments, false);
    }

    public static ProcessOutput executeCallGetOutput(String command, String arguments, boolean printStdout) throws IOException{
        int ret = -77;
        String line;
        Runtime rt = Runtime.getRuntime();
        Process pr = null;
        String output = "";

        pr = rt.exec(command + " " + arguments);

        BufferedReader input =
            new BufferedReader (new InputStreamReader(pr.getInputStream()));

        while ((line = input.readLine()) != null){
            output = output + line + "\n";
            if (printStdout == true){
                logger.info(line);
            }
        }

        try{
            pr.waitFor();
        }catch(Exception e){
            e.printStackTrace();
        }

        input.close();
        ret = pr.exitValue();

        pr.destroy();

        return new ProcessOutput(output,null,ret);
    }


    private static int OS = OS_UNINITIALIZED;

    private static String getOSString() throws OSNotSupportedException{
        String str;
        str = System.getProperty("os.name");
        if (str != null){return str.toLowerCase();}

        str = System.getProperty("OS");
        if (str != null){return str.toLowerCase();}

        Map<String, String> map = System.getenv();

        throw new OSNotSupportedException("Cannot get string that identifies the Operative System. Available: " + map.keySet());
    }
    public static int getOS() throws OSNotSupportedException{
        if (OS == OS_UNINITIALIZED){
            String str;
            str = getOSString();
            if (str.contains("win")){
                OS = OS_WINDOWS;
            }else if (str.contains("lin") || str.contains("uni")){
                OS = OS_UNIX;
            }else if (str.contains("mac")){
                OS = OS_MAC;
            }else{
                OS = OS_UNKNOWN;
            }
        }
        return OS;
    }


    public static <T> T areThereEqualElements(ArrayList<T> elem){
        elem.trimToSize();
        for (int i=elem.size()-1;i>=0;i--){
            int count = 0;
            T repeated = null;
            for (int j=0;j<i;j++){
                T a = elem.get(j);
                T b = elem.get(i);
                if (a.equals(b)){
                    repeated = a;
                    count++;
                }
            }
            if (count>0){
                return repeated;
            }
        }
        
        return null;
    }


    public static <T> ArrayList<T> removeRepeated(ArrayList<T> set){
        set.trimToSize();
        
        ArrayList<T> ret = new ArrayList<T>();

        boolean repeated;

        //for (int main_index=set.size()-1;main_index>=0;main_index--){
        for (int main_index=0;main_index<set.size();main_index++){
            repeated = false;
            T elem1 = set.get(main_index);
            //for (int j=0;j<main_index;j++){
            for (int j=main_index-1;j>=0;j--){
                T elem2 = set.get(j);
                
                if (elem2.equals(elem1)){
                    repeated = true;
                }
            }
            if (repeated==false){
                ret.add(elem1);
            }
        }

        return ret;
    }


    public static ProcessOutput executeCall(String workingdirectory, String command, String arguments) throws Exception{
        int ret = -77;
        String line;
        final StringBuilder output_stdout = new StringBuilder("");
        final StringBuilder output_stderr = new StringBuilder("");
        Runtime rt = Runtime.getRuntime();
        Process pr = null;
        File myWorking = new File(workingdirectory);
        ProcessOutput out = null;
        
        try{
            pr = rt.exec(new File(myWorking, command).getAbsolutePath() + " " + arguments, null, myWorking);
            final BufferedReader input =
                new BufferedReader (new InputStreamReader(pr.getInputStream()));

            final BufferedReader err =
                new BufferedReader (new InputStreamReader(pr.getErrorStream()));

//            output = output + "Output (stdout):\n";
//            while ((line = input.readLine()) != null){
//                logger.info(line);
//                output = output + line + "\n";
//            }
//            output = output + "\nError Output (stderr):\n";


//            final Process prf = pr;



            Thread stdout_thread = new Thread(
                new Runnable() {
                    public void run() {
                        String line;
                        try {
                            while ((line = input.readLine()) != null) {
                                logger.info(line);
                                output_stdout.append(line + "\n");
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            );
            stdout_thread.start(); 

            
            Thread stderr_thread = new Thread(
                new Runnable() {
                    public void run() {
                        String line;
                        try {
                            while ((line = err.readLine()) != null) {
                                logger.info(line);
                                output_stderr.append(line + "\n");
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            );
            stderr_thread.start();


            try{
                stdout_thread.join(); /* jjj */
                stderr_thread.join(); /* jjj */
            }catch(Exception e){
                e.printStackTrace();
            }


            pr.waitFor();

            input.close();

            ret = pr.exitValue();

            pr.destroy();


            out = new ProcessOutput(output_stdout.toString(),
                    output_stderr.toString(), ret);

                    
            if (ret!=0){
                throw new Exception("Process not executed correctly (return value='" + ret + "').");
            }

            String str = command + " " + arguments + "' in '" + workingdirectory +
                    "'.\nOutput: \n" + out;

            logger.info("IFE excecution details: \n" + str );

        }catch(Exception e){
            String str = "ERROR: While executing '" +
                    command + " " + arguments + "' in '" + workingdirectory + 
                    "'.\nDetails: \n" + out +
                    "\nException Java: " + e.getMessage();

            throw new Exception(str);
        }
        return out;


        /*
        int ret=-1;

        Process p = Runtime.getRuntime().exec(call);

        String line;
        BufferedReader input =
            new BufferedReader (new InputStreamReader(p.getInputStream()));

        while ((line = input.readLine()) != null){
            logger.info(line);
        }

        p.waitFor();

        input.close();
        ret = p.exitValue();

        p.destroy();
        return ret;
         *
         */
    }


    /*************************/
    /* Auxiliar Computations */
    /*************************/


    public static float computeAverage(float[] vector){
        float acumulator = 0.0f;
        for (int i=0; i<vector.length; i++){
            acumulator += vector[i];
        }
        return acumulator/vector.length;
    }

    public static float computeStd(float[] vector){
        float avg = computeAverage(vector);

        float acumulator = 0.0f;
        for (int i=0; i<vector.length; i++){
            acumulator += (vector[i]-avg)*(vector[i]-avg);
        }

        return (float) Math.sqrt(acumulator/vector.length);
    }



    public static Float computeAverage(ArrayList<Float> vector){
        float acumulator = 0.0f;
        for (Float f: vector){
            acumulator += f;
        }
        return acumulator/vector.size();
    }

    public static Float computeStd(ArrayList<Float> vector){
        float avg = computeAverage(vector);
        float acumulator = 0.0f;
        for (Float f:vector){
            acumulator += (f-avg)*(f-avg);
        }
        return new Float(Math.sqrt(acumulator/vector.size()));
    }





    public static float[] putFromRight_float1(float[] array, float value){
        for(int i=0;i<array.length-1;i++){
            array[i] = array[i+1];
        }
        array[array.length-1] = value;
        return array;
    }

    public static int[] putFromRight_int1(int[] array, int value){
        for(int i=0;i<array.length-1;i++){
            array[i] = array[i+1];
        }
        array[array.length-1] = value;
        return array;
    }

    /*
    public static Timestamp[] putFromRightTimestamp1(Timestamp[] array, Timestamp average){
        for(int i=0;i<array.length-1;i++){
            array[i] = (array[i+1]!=null?array[i+1]:null);
        }
        array[array.length-1] = average;
        return array;
    }
    */




    public static float[] getSubArrayFloat(float[] source, int from, int to){
        if ((from != -1) && (to != -1)){

            from = (int)((float)(((float)from)/100) * (float)(source.length-1));
            to = (int)((float)(((float)to)/100) * (float)(source.length-1));

            int len = to - from + 1;
            int i;
            float[] ret = new float[len];
            for (i=0; i<len; i++){
                ret[i] = source[i+from];
            }
            return ret;
        }else{
            return source;
        }
    }

    /*
    public static Timestamp[] getSubArrayTimestamp(Timestamp[] source, int from, int to){
        if ((from != -1) && (to != -1)){

            from = (int)((float)(((float)from)/100) * (float)(source.length-1));
            to = (int)((float)(((float)to)/100) * (float)(source.length-1));

            int len = to - from + 1;
            int i;
            Timestamp[] ret = new Timestamp[len];
            for (i=0; i<len; i++){
                ret[i] = source[i+from];
            }
            return ret;
        }else{
            return source;
        }
    }
     * */
     

    public static void deleteFile(String filename) throws Exception{
        File file = new File(filename);
        if(file.delete()){
            logger.info("File '" + filename + "' deleted.");
        }else{
            logger.warn("Error deleting file '" + filename + "'...");
        }

    }



    public static void deleteFilesFrom(String extension, String tool_path) throws Exception{
        ArrayList<File> files = Misc.getListOfFiles(extension, tool_path);
        for (File f: files){
            if(f.delete()){
                logger.info("File '" + f.getPath() + "' deleted.");
            }else{
                logger.warn("Error deleting file '" + f.getPath() + "'...");
            }
        }
    }

    public static ArrayList<File> getListOfFiles(String extension, String tool_path) throws Exception{
        final String exten = extension;

        if (tool_path==null){
            tool_path = ".";
        }

        File dir = new File(tool_path);

        FilenameFilter fnf;
        if (extension!=null){
            fnf = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return (name.endsWith("." + exten));
                }
            };
        }else{
            fnf = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return true;
                }
            };
        }

        ArrayList<File> output = new ArrayList<File>();

        File[] list_of_log = dir.listFiles(fnf);
        if (list_of_log == null){
            throw new IOException("Problem while listing  '" + tool_path + "'.");
        }
        for (int i=0; i<list_of_log.length; i++)
        {
            output.add(list_of_log[i]);
        }

        return output;
    }

public static ArrayList<File> getListOfFilesEndingWith(String ending, String tool_path) throws Exception{
        final String ending_final = ending;

        if (tool_path==null){
            tool_path = ".";
        }

        File dir = new File(tool_path);

        FilenameFilter fnf;
        if (ending!=null){
            fnf = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return (name.endsWith(ending_final));
                }
            };
        }else{
            fnf = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return true;
                }
            };
        }

        ArrayList<File> output = new ArrayList<File>();
        try{
            File[] list_of_log = dir.listFiles(fnf);

            for (int i=0; i<list_of_log.length; i++)
            {
                output.add(list_of_log[i]);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return output;
    }


    public static String readAllFile(String filename) throws Exception{
        String str =  null;
        try {
            FileInputStream i = new FileInputStream(filename);
            byte buff[] = new byte[i.available()];
            i.read(buff);
            i.close();
            str = new String(buff);
        }catch (Exception e){
            throw new Exception("Error opening file: " + filename);
        }
        return str;
    }

    public static void writeAllFile(String filename, String content) throws Exception{
        try {
            FileOutputStream i = new FileOutputStream(filename);
            i.write(content.getBytes());
            i.close();
        }catch (Exception e){
            throw new Exception("Error writing file: " + filename);
        }
    }

    public static void appendToFile(String filename, String content) throws Exception{
        try {
            FileOutputStream appendedFile =
                    new FileOutputStream(filename, true);
            appendedFile.write(content.getBytes());
            appendedFile.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    public static ArrayList<String> getLines(String input){
        String str;
        ArrayList<String> ret = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(new StringReader(input));
        try{
            while ((str = reader.readLine()) != null){
                if (str.length() > 0){
                    ret.add(str);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return ret;
    }



    public static ArrayList<String> filterEmptyLines(ArrayList<String> inp){
        ArrayList<String> ret = new ArrayList<String>();
        for(String l:inp){
            if (l.trim().length()>0){
                ret.add(l);
            }
        }

        return ret;
    }

    public static String getLineThatContains(ArrayList<String> set, String string){
        for (String line: set){
            if (line.contains(string)){
                return line;
            }
        }
        return null;
    }

    public synchronized static String getHostname() throws UnknownHostException{
        if (hostname==null){
            java.net.InetAddress localMachine = java.net.InetAddress.getLocalHost();
            hostname = localMachine.getHostName();
        }
        return hostname;
    }

    public static void printCollection(AbstractCollection set){
        System.out.println(Misc.collectionToString(set));
    }

    public static String collectionToString(AbstractCollection set){
        String str = "";
        int size = set.size();
        int i = 0;
        for(Object o:set){
            i++;
            str = str + "'" + o.toString() + (i==size?"'.":"', ");
        }
        return str;
    }

    public static Calendar getUniversalTimeWithInternet() throws Exception{
        try{
            String str2 = MiscIP.getHTML("http://www.census.gov/main/www/popclock.html");
            //08:01 UTC (EST+5) Aug 05, 2011
            Pattern p2 = Pattern.compile("(\\d{2}):(\\d{2}) UTC \\(EST([+-])(\\d)\\) (\\w{3}) (\\d{2}), (\\d{4})");
            Matcher m2 = p2.matcher(str2);

            if (m2.find()){
                //int hoursShift = Integer.valueOf(m2.group(4));
                String datestr = m2.group(1) + "." + m2.group(2) + "." + "00" + " " + m2.group(6) + "/" + m2.group(5) + "/" + m2.group(7) + " +0000";// + " " +  m2.group(3) + String.format("%02d", hoursShift) + "00";

                SimpleDateFormat formatter = new SimpleDateFormat("HH.mm.ss dd/MMM/yyyy Z", Locale.ENGLISH);
                Date d = (Date)formatter.parse(datestr);
                Calendar c = new GregorianCalendar();
                c.setTimeZone(new SimpleTimeZone(0, "Base"));
                c.setTimeInMillis(d.getTime());
                return c;
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        throw new Exception("Unable to get date from Internet...");   
    }

    public static Calendar getUniversalTime() throws Exception{
        return getUniversalTimeMinus(0);
    }

    public static Calendar getUniversalTimeMinus(int minus_minutes) throws Exception{
        if (timeDifference_hereminusthere == null){

            Calendar here_init = new GregorianCalendar(new SimpleTimeZone(0, "UTC")); /* Zone HERE. */
            Date d = new Date();
            Calendar page_init = Misc.getUniversalTimeWithInternet(); /* Zone UTC. */
            timeDifference_hereminusthere = here_init.getTimeInMillis() - page_init.getTimeInMillis();
            logger.info("[Misc] Obtained Universal time (page): '" + calendarToString(page_init) + "' ("+ page_init.getTimeInMillis()+").");
            logger.info("[Misc] Obtained local time:            '" + calendarToString(here_init) + "' ("+here_init.getTimeInMillis()+").");
            logger.info("[Misc] Difference (here-page): '" + timeDifference_hereminusthere + "'.");
        }

        /* Note that when changing TimeInMillis & TimeZone, the order does matter. */
        Calendar now = new GregorianCalendar(); /* Zone HERE. */
        Calendar ret = new GregorianCalendar(); /* Zone HERE. */
        ret.setTimeInMillis(now.getTimeInMillis() - timeDifference_hereminusthere - minus_minutes * 60 * 1000);
        
        ret.setTimeZone(new SimpleTimeZone(0, "Base"));
        return ret;
    }

    public static String calendarToString(Calendar c){
        return

                String.format("%02d", c.get(Calendar.YEAR)) + "/" +
                String.format("%02d", c.get(Calendar.MONTH) + 1) + "/" +
                String.format("%02d", c.get(Calendar.DAY_OF_MONTH)) + " " +
                String.format("%02d", c.get(Calendar.HOUR_OF_DAY)) + ":" +
                String.format("%02d", c.get(Calendar.MINUTE)) + ":" +
                String.format("%02d", c.get(Calendar.SECOND)) + " " +
                "(" + c.get(Calendar.ZONE_OFFSET) + ")";
    }
}
