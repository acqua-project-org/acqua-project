
package plugins.campaigngenerator.pingabstraction;

import mjmisc.exceptions.OSNotSupportedException;
import exceptions.ParsingException;
import java.io.IOException;
import java.net.InetAddress;

import plugins.campaigngenerator.pingabstraction.commandline.PingResultPackage;

public class Pinger {
    public static float ping(String landmark, int timeout_sec, int packet_size) throws IllegalArgumentException, OSNotSupportedException, ParsingException, IOException{
        return pingWithCommandLine(landmark, timeout_sec, packet_size);
        //return pingWithInetAddress(landmark, timeout_sec);
        //return pingWithLibPCap(landmark, timeout_sec);
    }

    private static void validateLandmark(String landmark) throws IllegalArgumentException{

    }

    private static float pingWithCommandLine(String landmark, int timeout_sec,
            int packet_size) throws IllegalArgumentException, OSNotSupportedException, ParsingException, IOException{
        validateLandmark(landmark);
        PingResultPackage pr = null;
        
        pr = new PingResultPackage(landmark, timeout_sec, packet_size);
        
        return pr.getTimeMs();
    }


    /**
     * This ping implementations is actually used for test reachability.
     * This is really bad to get measurements since its implementation is plataform-dependant
     * and TCP connection time is used in Windows.
     */
    private static float pingWithInetAddress(String landmark, int timeout_sec){
        long init = System.nanoTime();
        try {
            InetAddress.getByName(landmark).isReachable(1000 * timeout_sec);
        } catch (Exception ex) { ex.printStackTrace();}
        long end = System.nanoTime();
        return (float)((float)((float)end-(float)init)/(1000*1000));
    }
      
}
