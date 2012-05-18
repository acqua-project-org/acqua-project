package org.inria.acqua.plugins.anomalydetector;

import java.util.HashMap;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.inria.acqua.misc.*;


public class MapperTStudentInverse {

	private static Logger logger = Logger.getLogger(MapperTStudentInverse.class.getName()); 
    private static HashMap<String, Float> values = null;
    private static Float probCache = null;
    private static Integer dofCache = null;
    private static Float zCache = null;

    public static Float getZFromTStudent(Float prob_bw_0mina_0plua, Integer dof) throws Exception {
        checkIfInitialized();

        if (prob_bw_0mina_0plua.equals(probCache) && dof.equals(dofCache)){
            return zCache;
        }
        
        String key = String.format(Locale.ENGLISH, "prob%1.4f df%d", new Float(prob_bw_0mina_0plua), dof);
        Float fl = values.get(key);
        if (fl == null){
            logger.warn("ERROR: while trying to get z-t-student with key='" + key + "'.");
            throw new IllegalArgumentException("Invalid arguments ('" + key + "').");
        }else{
            probCache = prob_bw_0mina_0plua;
            dofCache = dof;
            zCache = fl;
            return fl;
        }
        
    }

    public static void main(String args[]) throws Exception {
        Float sl;
        Integer df;

        sl = 0.95f; df = 10; System.out.printf("Got sl=%f df=%d z=%f\n",sl,df,MapperTStudentInverse.getZFromTStudent(sl, df));
        sl = 0.95f; df = 10; System.out.printf("Got sl=%f df=%d z=%f\n",sl,df,MapperTStudentInverse.getZFromTStudent(sl, df));
        sl = 0.99f; df = 11; System.out.printf("Got sl=%f df=%d z=%f\n",sl,df,MapperTStudentInverse.getZFromTStudent(sl, df));
        sl = 0.98f; df = 10; System.out.printf("Got sl=%f df=%d z=%f\n",sl,df,MapperTStudentInverse.getZFromTStudent(sl, df));
        sl = 0.95f; df = 10; System.out.printf("Got sl=%f df=%d z=%f\n",sl,df,MapperTStudentInverse.getZFromTStudent(sl, df));
        sl = 0.95f; df = 10; System.out.printf("Got sl=%f df=%d z=%f\n",sl,df,MapperTStudentInverse.getZFromTStudent(sl, df));
        sl = 0.99f; df = 10; System.out.printf("Got sl=%f df=%d z=%f\n",sl,df,MapperTStudentInverse.getZFromTStudent(sl, df));
        sl = 0.999f; df = 11; System.out.printf("Got sl=%f df=%d z=%f\n",sl,df,MapperTStudentInverse.getZFromTStudent(sl, df));
        
    }

    private static void checkIfInitialized(){

/* Entries generated with MATLAB. */
/*** INIT MATLAB CODE ***/
/*


fid = fopen('mapper.txt','w');
sign = [0.8, 0.9, 0.96, 0.98, 0.99, 0.994, 0.996, 0.998, 0.999, 0.9995, 0.9999];
for df = 1 : 240
    for s = 1:length(sign)
        fprintf(fid, '            values.put("prob%2.4f df%d", new Float(%2.9ff));\n', sign(s),df ,tinv(1-((1-sign(s))/2), df));
    end
end
fclose(fid);

 */
/*** END  MATLAB CODE ***/
        if (values==null) {

            values = new HashMap<String,Float>();

                        values.put("prob0.8000 df1", new Float(3.077683537f));
            values.put("prob0.9800 df238", new Float(2.342117730f));
            values.put("prob0.9900 df238", new Float(2.596643941f));
            values.put("prob0.9940 df238", new Float(2.772668255f));
            values.put("prob0.9960 df238", new Float(2.906484397f));
            values.put("prob0.9980 df238", new Float(3.124826897f));
            values.put("prob0.9990 df238", new Float(3.331873277f));
            values.put("prob0.9995 df238", new Float(3.529312077f));
            values.put("prob0.9999 df238", new Float(3.957545098f));
            values.put("prob0.8000 df239", new Float(1.285103775f));
            values.put("prob0.9000 df239", new Float(1.651254165f));
            values.put("prob0.9600 df239", new Float(2.065018308f));
            values.put("prob0.9800 df239", new Float(2.342051323f));
            values.put("prob0.9900 df239", new Float(2.596556193f));
            values.put("prob0.9940 df239", new Float(2.772563254f));
            values.put("prob0.9960 df239", new Float(2.906364822f));
            values.put("prob0.9980 df239", new Float(3.124680679f));
            values.put("prob0.9990 df239", new Float(3.331698324f));
            values.put("prob0.9995 df239", new Float(3.529106386f));
            values.put("prob0.9999 df239", new Float(3.957260718f));
            values.put("prob0.8000 df240", new Float(1.285088932f));
            values.put("prob0.9000 df240", new Float(1.651227393f));
            values.put("prob0.9600 df240", new Float(2.064971102f));
            values.put("prob0.9800 df240", new Float(2.341985472f));
            values.put("prob0.9900 df240", new Float(2.596469182f));
            values.put("prob0.9940 df240", new Float(2.772459135f));
            values.put("prob0.9960 df240", new Float(2.906246252f));
            values.put("prob0.9980 df240", new Float(3.124535691f));
            values.put("prob0.9990 df240", new Float(3.331524844f));
            values.put("prob0.9995 df240", new Float(3.528902430f));
            values.put("prob0.9999 df240", new Float(3.956978744f));

            
        }
    }
}
