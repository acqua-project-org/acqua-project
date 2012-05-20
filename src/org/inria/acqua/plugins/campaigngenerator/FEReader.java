
package org.inria.acqua.plugins.campaigngenerator;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.inria.acqua.exceptions.NoMoreWorkException;
import org.inria.acqua.misc.Landmark;
import org.inria.acqua.mjmisc.MiscIP;
import org.inria.acqua.plugins.FlowElement;


/**
 * @author mjost
 */
public class FEReader {
	private static Logger logger = Logger.getLogger(FEReader.class.getName()); 
    private Gson gson;
    private FileReader freader;
    private BufferedReader breader;
    private Landmark landmark;


    private static Pattern pattern1 = Pattern.compile(".*?([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3}).*?\\.txt");
    private static Pattern pattern2 = Pattern.compile("(.*?)" + "-output.*?txt");

    /* A valid file must have .txt extension and contain the IP address embedded in the filename. */
    public FEReader(File file) throws Exception{
        gson = new Gson();
        freader = new FileReader(file);
        breader = new BufferedReader(freader);
        
        Matcher m1 = pattern1.matcher(file.getName());
        String strland = null;
        if (m1.find()){
           strland = m1.group(1) + "." + m1.group(2) + "." + m1.group(3) + "." + m1.group(4);
        }else{
            Matcher m2 = pattern2.matcher(file.getName());
            if (m2.find()){
                strland = m2.group(1);
            }else{
                throw new Exception("The file's name '" + file.getName() +
                        "' does not match with any valid file name for reverse IFE.");
            }
            
        }
        String ipsolved = MiscIP.solveName(strland);
        logger.info(strland + " " + ipsolved);
        landmark = new Landmark(ipsolved);
    }

    public Landmark getSourceLandmark(){
        return landmark;
    }

    public FlowElement readNextFE() throws Exception{
        if (breader.ready()){
            String str = breader.readLine();
            JsonDumpeableFlowElement fe2 = gson.fromJson(str, JsonDumpeableFlowElement.class);
            FlowElement fe3 = fe2.dumpToFlowElement();
            return fe3;
        }
        breader.close();
        freader.close();
        throw new NoMoreWorkException("Done.");

    }

}
