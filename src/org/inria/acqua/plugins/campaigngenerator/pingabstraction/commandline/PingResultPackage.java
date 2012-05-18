package org.inria.acqua.plugins.campaigngenerator.pingabstraction.commandline;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.inria.acqua.exceptions.ParsingException;
import org.inria.acqua.mjmisc.Misc;
import org.inria.acqua.mjmisc.ProcessOutput;
import org.inria.acqua.mjmisc.exceptions.OSNotSupportedException;

public class PingResultPackage {
    private static Pattern successPatternWindows = Pattern.compile( "[0-9.]{7,15}: bytes=\\d{1,3} \\w{3,8}[=<](\\d{1,6})m");
    private static Pattern allLostPatternWindows = Pattern.compile( ".*ping.*bytes.*100%", Pattern.DOTALL);
    private static Pattern successPatternLinux = Pattern.compile( "ttl=\\d{1,3} time=([0-9.]{1,12}) ms");
    private static Pattern allLostPatternLinux = Pattern.compile( ".*PING.*bytes.*100%", Pattern.DOTALL);
    private static Pattern unaccessibleMatcherWindows = Pattern.compile("[0-9.]{7,15}");
    private static Pattern unaccessibleMatcherLinux = Pattern.compile("[0-9.]{7,15}");
    private String ip;
    private float timeMs;

    public PingResultPackage(String landmark, int timeout_sec, int packet_size)
            throws ParsingException, IOException, OSNotSupportedException{
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

        m = successMatcher.matcher(output);
        if(m.find()){ /* Matcher ping successful. */
            timeMs = Float.parseFloat(m.group(1));
            return;
        }

        m = allLostMatcher.matcher(output);
        if(m.find()){ /* Matcher ping lost. */
            timeMs = -1.0f;
            return;
        }

        m = unaccessibleMatcher.matcher(output);
        if(m.find()){ /* Matcher ping lost. */
            timeMs = -1.0f;
            return;
        }

        throw new ParsingException("Error while parsing output '\n"+output+"\n' with matchers >'"+successMatcher+"' and '"+allLostMatcher+ "' and '" + unaccessibleMatcher +  "'< of command '" + command + " " + arguments + "'.");
    }

    
    public String getIp() {
        return ip;
    }

    public float getTimeMs() {
        return timeMs;
    }
}
