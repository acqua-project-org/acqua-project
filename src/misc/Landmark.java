package misc;

import java.util.Arrays;
import mjmisc.MiscIP;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import plugins.campaigngenerator.pingabstraction.Pinger;

public class Landmark {

    public static final int TYPE_UNKNOWN    = 0; /** Not known type of landmark object. */
    public static final int TYPE_RAW        = 1; /** The user gave a raw IP ('127.0.0.1'). */
    public static final int TYPE_RESOLVED   = 2; /** The user gave a name and it was solved by a DNS solver ('localhost'). */
    public static final int TYPE_KEYWORD_GW = 3; /** The user gave a keywork that was solved here. */

    public static final String LANDMARK_GATEWAY = "keyword-gateway";


    private static Pattern patternRegex = Pattern.compile("(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})");
    private String alias;
    private int type;
    private int ip[];

    public Landmark(){
        type = TYPE_UNKNOWN;
    }
    
    public Landmark(String landmstr, String alias) throws Exception{
        this.alias = alias;
        try{
            this.ip = parseIP(landmstr);
            type = TYPE_RAW;
        }catch(Exception e){
            if(LANDMARK_GATEWAY.equals(landmstr)){
                try{
                    this.ip = parseIP(MiscIP.getGateway1());
                    type = TYPE_KEYWORD_GW;
                }catch(Exception j){
                    try{
                        this.ip = parseIP(MiscIP.getGatewayNetstat());
                        type = TYPE_KEYWORD_GW;
                    }catch(Exception er){
                        er.printStackTrace();
                    }
                    
                }
            }else{
                throw new IllegalArgumentException("Invalid landmark syntax: '"+ landmstr +"'.");
            }
        }
    }

    public int getType(){
        return type;
    }

    private int[] parseIP(String ipadd) throws Exception{
        int[] ipa = new int[4];
        Matcher matcher = patternRegex.matcher(ipadd);
        if (matcher.find()){
            for (int i=0; i<4; i++){
                int integ = (int) Integer.valueOf(matcher.group(i+1));
                if (integ>=0 && integ<=255){
                    ipa[i] = (int)integ;
                }else{
                    throw new IllegalArgumentException("Invalid landmark value: '"+ ipadd +"'.");
                }
            }
            return ipa;
            
        }else{
            throw new Exception("Invalid landmark '" + ipadd + "'.");
        }
    }

    public Landmark(String landmstr) throws Exception{
        this(landmstr, "<no alias>");
    }


    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Arrays.hashCode(this.ip);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Landmark other = (Landmark) obj;
        if (!Arrays.equals(this.ip, other.ip)) {
            return false;
        }
        return true;
    }

    public String getDescriptiveName(){
        return this.toString() + " " + alias;
    }

    public String getAlias(){
        return alias;
    }
    
    @Override
    public String toString(){
        return (int)ip[0] + "." + (int)ip[1] + "." + (int)ip[2] + "." + (int)ip[3];
    }

    public static void main(String args[]) throws Exception{
        String landm;
        
        landm = "127.0.0.1";
        System.out.println("Landm: " + landm + "\nlater: " + new Landmark(landm)+"\n");

        landm = "199.0.0.1";
        System.out.println("Landm: " + landm + "\nlater: " + new Landmark(landm)+"\n");

        landm = "1.0.0.1";
        System.out.println("Landm: " + landm + "\nlater: " + new Landmark(landm)+"\n");

        landm = "256.0.0.1";
        System.out.println("Landm: " + landm + "\nlater: " + new Landmark(landm)+"\n");

        landm = "355.0.0.1";
        System.out.println("Landm: " + landm + "\nlater: " + new Landmark(landm)+"\n");
    }
}
