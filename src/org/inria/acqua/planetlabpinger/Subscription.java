package org.inria.acqua.planetlabpinger;

import java.util.ArrayList;

import org.inria.acqua.mjmisc.Misc;
import org.inria.acqua.mjmisc.MiscIP;

/**
 * Represents a client that wants to be pinged to have an Inverse IFE service. 
 * @author mjost
 */
public class Subscription {
    private String name; // Login name of the client/user. 
    private String ip;   // IP address of that client/user. 
    
    public Subscription(String name, String ip) throws Exception{
        this.name = name;
        this.ip = MiscIP.checkIP(ip);
    }

    public String getName(){
        return name;
    }
    
    public String toString(){
        return name + "-" + ip;
    }

    public String getPublicIP(){
        return ip;
    }

    public static ArrayList<Subscription> parseMany(ArrayList<String> lines){
        ArrayList<Subscription> ret = new ArrayList<Subscription>();
        
        for(String s:lines){
            if (s.startsWith("*")){
                continue;
            }else{
                String[] sections = s.split(" ");
                try{
                    String name = sections[0];
                    String publicip = sections[1];
                    ret.add(new Subscription(name,MiscIP.solveName(publicip)));
                }catch(Exception e){
                }
            }
        }
        return ret;
    }
}
