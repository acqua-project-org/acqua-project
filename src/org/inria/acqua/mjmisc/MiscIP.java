
package org.inria.acqua.mjmisc;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.inria.acqua.mjmisc.exceptions.OSNotSupportedException;


public class MiscIP {
    private static Pattern IP_CHECKER_REGEX = Pattern.compile("(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})");
    public static final String IP_KEY = "pip";
    public static final String EMPTY = "empty";
    private static final String publicIPprovider = "http://automation.whatismyip.com/n09230945.asp";
    private static String gateway1;
    private static String gateway2;
    
    public static void sendHTML(String urlstr)
    {

        try
        {


            URL urlll = new URL(urlstr);
            //SSLContext sslcontext = SSLContext.getInstance("TLS");
            //sslcontext.init(null, new TrustManager[] { easyTrustManager }, null);
            //HttpsURLConnection.setDefaultSSLSocketFactory(sslcontext.getSocketFactory());
            HttpURLConnection shuc = (HttpURLConnection) urlll.openConnection();
            shuc.setDoOutput(true);
//            shuc.setDoInput(true);
//            shuc.setFollowRedirects(true);
//            System.out.println("Connection Code--------->" + shuc.getResponseCode());
//
//
//
//            BufferedReader in = new BufferedReader(new InputStreamReader(urlll.openStream()));
//            String str = in.readLine();
//            while(str != null)
//            {
//                System.out.println("DATA From Https Server------>" + str);
//                str = in.readLine();
//            }

            OutputStreamWriter out = new OutputStreamWriter(shuc.getOutputStream());
            out.write("auth_user=civ&auth_pass=civ&redirurl=&accept=Continue");
            out.flush();	out.close();
            System.out.println("Data Are Written Successfully.......");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    

        
    public static String getHTML(String urlstr) throws Exception{
        String ip = null;
        String output = "";

        URL url = new URL(urlstr);
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        String str = null;
        while ((str = in.readLine()) != null){
            output = output + str;
        }
        in.close();
        return output;
    }

    public static String getPublicIPAddress() throws Exception{
        String ip = null;
        try{
            ip = parseIP(getHTML(publicIPprovider));
        }catch(Exception e1){
            try{
                HashMap<String,Object> info = MiscIP.getIPInformation();
                String tt = (String)info.get(IP_KEY);
                if (tt.equals(EMPTY)){
                    throw new Exception();
                }
                ip = parseIP(tt);
            }catch(Exception e2){
                try{
                    HashMap<String,Object> info = MiscIP.getIPInformation2();
                    String tt = (String)info.get(IP_KEY);
                    if (tt.equals(EMPTY)){
                        throw new Exception();
                    }
                    ip = parseIP(tt);
                }catch(Exception e3){
                    throw new Exception("No way of getting the Public IP...");
                }
            }
        }
        return ip;
    }

    private static String parseIP(String str){
        String ip;
        Pattern strMatch = Pattern.compile( "([0-9.]{7,15})");
        Matcher m = strMatch.matcher(str);
        if(m.find()){
            ip = m.group(1);
            return ip;
        }else{
            return null;
        }
    }


    public static String getGatewayNetstat() throws Exception{
        
        /* get os-dependent comment for netstat/'route print'
         parse its output (check 0.0.0.0's gateway)
         * retrieve it
         * check also for linux.
         */
        
        int os = Misc.getOS();
        ProcessOutput output;
        Pattern pat;
        switch(os){
            case Misc.OS_WINDOWS:
                output = Misc.executeCallGetOutput("netstat", "-rn");
                pat = Pattern.compile("0.0.0.0\\s*?0.0.0.0\\s*?([0-9.]{7,15})\\s*?");
                break;
            case Misc.OS_UNIX:
                output = Misc.executeCallGetOutput("netstat", "-rn");
                pat = Pattern.compile("0.0.0.0\\s*?([0-9.]{7,15})\\s*?0.0.0.0\\s*?");
                break;
            default:
                throw new Exception("OS not supported.");
        }
        String outstr = output.getOutputSTDOUT();
        Matcher m = pat.matcher(outstr);
        if(m.find()){
            return m.group(1);
        }else{
            throw new Exception("Cannot obtain gateway from netstat output: \n" + outstr);
        }
    }




    public static String getGateway1() throws Exception{
        if (gateway1==null){
            gateway1 = getGateway(1);
        }
        return gateway1;
    }
    
    public static String getGateway2() throws Exception{
        if (gateway2==null){
            gateway2 = getGateway(2);
        }
        return gateway2;
    }

    public static String getGateway(int hop) throws Exception{

        /* get os-dependent comment for traceroute/tracert
         parse its output (check 1st hop)
         * retrieve it
         * check also for linux.
         */

        int os = Misc.getOS();
        ProcessOutput output;
        Pattern pat;
        switch(os){
            case Misc.OS_WINDOWS:
                output = Misc.executeCallGetOutput("tracert", "-d -h "+hop+" www.google.com");
                pat = Pattern.compile("[\\s]"+hop+"\\s+[<>0-9*.]+\\sms\\s+[<>0-9*.]+\\s+ms\\s+[<>0-9*.]+\\s+ms\\s+([0-9.]{7,15})");
                break;
            case Misc.OS_UNIX:
                output = Misc.executeCallGetOutput("traceroute", "-m "+hop+" -n www.google.com");
                pat = Pattern.compile("[\\s]"+hop+"\\s+([0-9.]{7,15})\\s+[<>0-9*.]+\\sms\\s+[<>0-9*.]+\\s+ms\\s+[<>0-9*.]+\\s+ms");
                break;
            default:
                throw new Exception("OS not supported.");
        }
        String outstr = output.getOutputSTDOUT();
        Matcher m = pat.matcher(outstr);
        if(m.find()){
            return m.group(1);
        }else{
            throw new Exception("Cannot obtain gateway from traceroute output: \n" + outstr);
        }
    }


    public static void main(String args[]) throws Exception{

        String filecontent = Misc.readAllFile("nodes.txt");
        ArrayList<String> names = Misc.getLines(filecontent);
        String result_both = "";
        String result_alone = "";
        for (String name:names){
            System.out.println("\t\tCurrent: " + name);
            try{
                float ping1 = MiscIP.ping(name, 3, 56);
                float ping2 = MiscIP.ping(name, 3, 56);
                if (ping1 < 0.0 && ping2 < 0.0 ){
                    throw new Exception();
                }

                /* Pingable. */
                
                String ip = MiscIP.solveName(name);
                result_both = result_both + (ip + "\t" + name + "\n");
                result_alone = result_alone + (ip + "\n");
            }catch(Exception e){
                System.out.println("'" + name + "' was not solvable or pingable.");
            }

        }
        Misc.deleteFile("nodes_nameresolved.txt");
        Misc.writeAllFile("nodes_nameresolved.txt", result_both);
        Misc.deleteFile("nodes_nameresolved_alone.txt");
        Misc.writeAllFile("nodes_nameresolved_alone.txt", result_alone);



    }

    /*
    public static void main(String args[]) throws Exception{
        //MiscIP.getPublicIPAddress();
        String str;
        str = "12.12.12.12";
        System.out.println(str);
        str = MiscIP.checkIP(str);
        System.out.println(str);

    }
*/
    public static void print(String str){
        System.out.println(str);
    }

    public static HashMap<String, Object> getIPInformation() throws Exception{
        HashMap<String, Object> ret = new HashMap<String, Object>();
        String urlProviderName = "http://www.ip2location.com/";
        String htmloutput = MiscIP.getHTML(urlProviderName);

        String debuginfo = "url:'" +urlProviderName + "';;html: '" + htmloutput + "'.";
        /* lat, lon, pip, isp, loc */
        Matcher m;

        m = Pattern.compile("([0-9.]{6,13})\\sLATITUDE,\\s([0-9.]{6,13})\\sLONGITUDE").matcher(htmloutput);
        if(m.find()){
            ret.put("lat", new Float(m.group(1)));
            ret.put("lon", new Float(m.group(2)));
        }else{
            ret.put("lat", EMPTY);
            ret.put("lon", EMPTY);
            String exx = "Could not parse latitude/longitude from " + debuginfo + ".";
            System.out.println(exx);
            //throw new Exception(str);

        }
        
        m = Pattern.compile("<span id=\"Livedemo1_lblIpAddress\" class=\"fontgraysmall\">([0-9.]{7,15})</span>").matcher(htmloutput);
        if(m.find()){
            //System.out.println("\tOne match: " + m.group(1));
            ret.put(IP_KEY, m.group(1));
        }else{
            ret.put(IP_KEY, EMPTY);
            String exx = "Could not parse public IP from " + debuginfo + ".";
            System.out.println(exx);
            //throw new Exception(exx);
        }
        
        m = Pattern.compile("<span id=\"Livedemo1_lblISP\" class=\"fontgraysmall\">(.*?)</span>").matcher(htmloutput);
        if(m.find()){
            //System.out.println("\tOne match: " + m.group(1));
            ret.put("isp", m.group(1));
        }else{
            ret.put("isp", EMPTY);
            String exx = "Could not parse ISP from " + debuginfo + ".";
            System.out.println(exx);
            //throw new Exception(exx);
        }

        m = Pattern.compile("<span id=\"Livedemo1_lblLocation\" class=\"fontgraysmall\">(.*?)</span>").matcher(htmloutput);
        if(m.find()){
            //System.out.println("\tOne match: " + m.group(1));
            ret.put("loc", m.group(1));
        }else{
            ret.put("loc", EMPTY);
            String exx = "Could not parse location from " + debuginfo + ".";
            System.out.println(exx);
            //throw new Exception(exx);
        }

        return ret;
    }

    public static HashMap<String, Object> getIPInformation2() throws Exception{
        HashMap<String, Object> ret = new HashMap<String, Object>();
        String urlName = "http://www.find-ip-address.org/";
        String str = MiscIP.getHTML(urlName);

        Matcher m;


        m = Pattern.compile("<br><b>My IP Address Latitude</b>: \\(([0-9.]+)\\)<br><b>My IP Address Longtitude</b>: \\(([0-9.]+)\\)<br>").matcher(str);
        if(m.find()){
            System.out.println("\tOne match: " + m.group(1));
            System.out.println("\tOne match: " + m.group(2));
            ret.put("lat", new Float(m.group(1)));
            ret.put("lon", new Float(m.group(2)));
        }else{
            ret.put("lat", EMPTY);
            ret.put("lon", EMPTY);
            String exx = "Could not parse latitude/longitude from '" + urlName + "'.";
            System.out.println(exx);
            //throw new Exception(exx);
        }

        m = Pattern.compile("<strong>My IP Address lookup</strong> for <b>([0-9.]{7,15})</b>").matcher(str);
        if(m.find()){
            System.out.println("\tOne match: " + m.group(1));
            ret.put(IP_KEY, m.group(1));
        }else{
            String exx = "Could not parse public IP from '" + urlName + "'.";
            ret.put(IP_KEY, EMPTY);
            System.out.println(exx);
            //throw new Exception(exx);
        }

        m = Pattern.compile("<br><strong>My ISP \\(Internet Service Provider\\)</strong>:&nbsp;<font color='#980000'> (\\w+)</font><br />").matcher(str);
        if(m.find()){
            System.out.println("\tOne match: " + m.group(1));
            ret.put("isp", m.group(1));
        }else{
            String exx = "Could not parse ISP from '" + urlName + "'.";
            ret.put("isp", EMPTY);
            System.out.println(exx);
            //throw new Exception(exx);
        }

        m = Pattern.compile("<br><b>My IP Address City</b>[:&nbsp;<>0-9a-zA-Z=#' ]+>(\\w+)</font>").matcher(str);
        if(m.find()){
            System.out.println("\tOne match: " + m.group(1));
            ret.put("loc", m.group(1));
        }else{
            String exx = "Could not parse location from '" + urlName + "'.";
            ret.put("loc", EMPTY);
            System.out.println(exx);
            //throw new Exception(exx);
        }

        m = Pattern.compile("<b>My IP Country Name</b>[:&nbsp;&nbsp;<>a-z ='#0-9]+> (\\w+)</font>").matcher(str);
        if(m.find()){
            System.out.println("\tOne match: " + m.group(1));
            ret.put("coun", m.group(1));
        }else{
            String exx = "Could not parse country from '" + urlName + "'.";
            ret.put("coun", EMPTY);
            System.out.println(exx);
            //throw new Exception(exx);
        }
        
        return ret;
    }

    public static String solveName(String name) throws UnknownHostException{
        String name_ip = InetAddress.getByName(name).toString();
        String ip = null;

        int index = name_ip.indexOf("/");
        if (index!=-1){
            ip = name_ip.substring(index+1);
        }
        
        return ip;

    }

    public static String checkIP(String ipadd) throws Exception{
        Matcher matcher = IP_CHECKER_REGEX.matcher(ipadd);
        String ret = "";
        int reti[] = new int[4];
        ipadd = ipadd.trim();
        if (ipadd.matches(("[^0-9.]"))){
            throw new Exception("Invalid IP '" + ipadd + "'. It contains invalid characters.");
        }
        if (matcher.find()){
            for (int i=0; i<4; i++){
                int integ = (int) Integer.valueOf(matcher.group(i+1));
                if (integ>=0 && integ<=255){
                    reti[i] = integ;
                }else{
                    throw new IllegalArgumentException("Invalid IP value: '"+ ipadd +"'.");
                }
            }
            return reti[0] + "." + reti[1] + "." + reti[2] + "." + reti[3];
        }else{
            throw new Exception("Invalid IP '" + ipadd + "'.");
        }
    }


    /*************/
    private static Pattern successPatternWindows = Pattern.compile( "[0-9.]{7,15}: bytes=\\d{1,3} \\w{3,8}[=<](\\d{1,6})m");
    private static Pattern allLostPatternWindows = Pattern.compile( ".*ping.*bytes.*100%", Pattern.DOTALL);
    private static Pattern successPatternLinux = Pattern.compile( "ttl=\\d{1,3} time=([0-9.]{1,12}) ms");
    private static Pattern allLostPatternLinux = Pattern.compile( ".*PING.*bytes.*100%", Pattern.DOTALL);
    private static Pattern unaccessibleMatcherWindows = Pattern.compile("[0-9.]{7,15}");
    private static Pattern unaccessibleMatcherLinux = Pattern.compile("[0-9.]{7,15}");
    

    public static float ping(String landmark, int timeout_sec, int packet_size)
            throws Exception, IOException, OSNotSupportedException{
        String output;
        int os = Misc.getOS();
        int count = 1;
        String arguments = null;
        String command = "ping";

        Pattern successMatcher = null;
        Pattern allLostMatcher = null;
        Pattern unaccessibleMatcher = null;
        switch(os){
            case Misc.OS_UNIX:
                arguments = "-w "+(timeout_sec*   1)+" -c "+count+" "+landmark+" -s " + packet_size;
                successMatcher = successPatternLinux;
                allLostMatcher = allLostPatternLinux;
                unaccessibleMatcher = unaccessibleMatcherLinux;
                break;
            case Misc.OS_WINDOWS:
                arguments = "-w "+(timeout_sec*1000)+" -n "+count+" "+landmark+" -l " + packet_size;
                successMatcher = successPatternWindows;
                allLostMatcher = allLostPatternWindows;
                unaccessibleMatcher = unaccessibleMatcherWindows;
                break;
            default:
                throw new OSNotSupportedException("OS not supported for parsing pinging.");
        }

        ProcessOutput out = Misc.executeCallGetOutput(command, arguments);

        /*
        if (out.getReturnValue()!=0){
            throw new Exception("Error of execution.\n" + out);
        }
        */


        //MyLog.logP(this, "Landmark: " + landmark + "\nCommand: " + command + " " + arguments + "\nOutput: \n" + out.getOutputSTDOUT());
        output = out.getOutputSTDOUT();
        Matcher m;

        if (output == null){
            throw new IOException("Ping failed. Not captured its output, returned: " + out.getReturnValue());
        }

        float timeMs;
        m = successMatcher.matcher(output);
        if(m.find()){ /* Matcher ping successful. */
            timeMs = Float.parseFloat(m.group(1));
            return timeMs;
        }

        m = allLostMatcher.matcher(output);
        if(m.find()){ /* Matcher ping lost. */
            timeMs = -1.0f;
            return timeMs;
        }

        m = unaccessibleMatcher.matcher(output);
        if(m.find()){ /* Matcher ping lost. */
            timeMs = -1.0f;
            return timeMs;
        }

        throw new Exception("Error while parsing output '\n"+output+"\n' with matchers >'"+successMatcher+"' and '"+allLostMatcher+ "' and '" + unaccessibleMatcher +  "'< of command '" + command + " " + arguments + "'.");
    }

    
    /*************/

}







