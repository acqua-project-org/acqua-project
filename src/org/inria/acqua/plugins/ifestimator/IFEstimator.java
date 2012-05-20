
package org.inria.acqua.plugins.ifestimator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.inria.acqua.exceptions.NotEnoughDataException;
import org.inria.acqua.exceptions.PipelineException;
import org.inria.acqua.exceptions.UnsupportedCommandException;
import org.inria.acqua.misc.Landmark;
import org.inria.acqua.misc.Timestamp;
import org.inria.acqua.plugins.FlowElement;
import org.inria.acqua.plugins.PipDefs;
import org.inria.acqua.plugins.Pipelineable;
import org.inria.acqua.plugins.anomalydetector.AnomalyDetector;
import org.inria.acqua.plugins.anomalydetector.GeneralAnomalyDetector;
import org.inria.acqua.plugins.anomalydetector.NormalDistrib;


/*
 * Implementar un emulador de red.
 * La idea es tener una consola con los landmarks y con sus RTT como slides.
 * Cuando muevo uno tendría que ver un cambio o una anomalía verdad?
 *
 * Después MÁS ADELANTE uno podría armar una red con Matlab que escriba archivos
 * que son leidos por un PingerMockupeado que obtiene las RRT de landmarks que le hace falta.
 * La red tiene links 1.2.3.4.5.6.7.8. Los paths son fijos. Uno cambia tiempo
 * de acceso en un link. Los caminos que cruzan ese link son afectados (el tiempo de un camino
 * no es más que 2 * suma de los tiempos de los links que incluye). Teniendo 5 medidores y 10 landmarks
 * uno tendría que ver que cuando un link muy cercano a un cliente es afectado,
 * el cliente debería mostrar un IFE más grande que los demás.
 * 
 *
 * Justificación? Fácil, queremos ver cómo capturaríamos la información.
 * Queremos un entorno COMPLETAMENTE CONTROLADO.
 */
/**
 * Element of the pipeline that takes anomalies of many landmarks and estimates the Impact Factor. 
 * @author mjost
 */
public class IFEstimator implements Pipelineable{
	private static Logger logger = Logger.getLogger(IFEstimator.class.getName()); 
    private ArrayList<Pipelineable> sinks;
    
    public IFEstimator(){
        sinks = new ArrayList<Pipelineable>();
    }

    public void addAsSink(Pipelineable p) {
        sinks.add(p);
    }


    public void insertFlowElement(FlowElement fe, String signature) throws Exception {
        if (PipDefs.SIGN_ANDETGEN.equals(signature)==true){
            logger.info("Inserting a FlowElement...\n");

            /* We have all the flowElements to proceed. */
            FlowElement output = new FlowElement();
            calculateImpactFactorAndPutInFlowElement(fe, output);

            for(Pipelineable p: sinks){
                p.insertFlowElement(output, PipDefs.SIGN_IFECALC);
            }
        }else{
            throw new PipelineException("Signature '"+signature+"' not supported.");
        }
    }

    private void calculateImpactFactorAndPutInFlowElement(FlowElement input, FlowElement output) throws Exception{
        /* Here we need to calculate amount of anomalies and shifts. */
        int ife = 0;

        ArrayList<Float> rttArray = new ArrayList<Float>();
        ArrayList<Float> shiftArray = new ArrayList<Float>();

        int unreachables = 0;

        HashMap <Landmark, Float> rttLandmark = new HashMap<Landmark, Float>();
        ArrayList<Landmark> currentLandmarksList = (ArrayList<Landmark>)input.get(PipDefs.FE_LANDMARKS_LIST);
        for (Landmark lm: currentLandmarksList){

            //print(lm.getDescriptiveName() + " evaluated...");

            FlowElement curr = (FlowElement) input.get(PipDefs.SIGN_ANDETLAN + lm.toString());
            if (curr==null){
                throw new Exception("Landmark '" + lm + "' didn't deliver the FlowElement.");
            }

            
            Integer anomaly = (Integer)curr.get(PipDefs.FE_THIS_LANDMARK_ANOMALY);
            Float rtt = (Float)curr.get(PipDefs.FE_THIS_LANDMARK_RTT);
            Float shift = (Float)curr.get(PipDefs.FE_THIS_LANDMARK_SHIFT);

            //print("\t [flow from]"+ lm.toString() + " rtt " + rtt + " anomaly " + anomaly + " shift " + shift);
            
            rttLandmark.put(lm, rtt);
            if (includeAnomalyTypeInIFE(anomaly)){
                ife += 1;
            }

            if (includeAnomalyTypeInShift(anomaly)){
                shiftArray.add(shift);
            }

            if (includeAnomalyTypeInRTT(anomaly)){
                rttArray.add(rtt);
            }
            
            if(includeAnomalyTypeInUnreachable(anomaly)){
                unreachables += 1;
            }
        }

        int amount_of_landmarks = currentLandmarksList.size();

        Float finalIFE = new Float((float)ife/(float)amount_of_landmarks);

        /* Calculating average RTT (not mathematically used, it is only representative to the user). */
        Float finalRTTAvg = 0.0f;
        Float finalRTTStd = 0.0f;
        try{
            ArrayList<Float> rtt = getAvgStdMaxMin(rttArray);
            finalRTTAvg = rtt.get(0);
            finalRTTStd = rtt.get(1);
            //print("\t " + " rtt avg " + finalRTTAvg);
        }catch(NotEnoughDataException e){
            /* It happens when all RTT were invalid. */
            //System.err.println("There are no points to calculate STD RTT. Putting default values.");
        }


        /* Calculating average Shift (not mathematically used, it is only representative to the user). */
        Float finalShiftAvg = 0.0f;
        Float finalShiftStd = 0.0f;
        Float finalShiftMin = 0.0f;
        Float finalShiftMax = 0.0f;
        try{
            ArrayList<Float> shift = getAvgStdMaxMin(shiftArray);
            finalShiftAvg = shift.get(0);
            finalShiftStd = shift.get(1);
            finalShiftMin = shift.get(2);
            finalShiftMax = shift.get(3);
        }catch(NotEnoughDataException e){
            /* It happens when all RTT were invalid. */
            //System.err.println("There are no points to calculate Shift. Putting default values.");
        }

        Float finalUnreachable = 0.0f;
        if(unreachables!=0){
            finalUnreachable = new Float((float)unreachables/(float)amount_of_landmarks);
        }

        Float significanceLevel = (Float)input.get(PipDefs.FE_CALCULATED_SIGNIFICANCE_LEVEL);
        
        output.put(PipDefs.FE_IFE_THIS_CAMPAIGN, finalIFE);
        output.put(PipDefs.FE_AVG_RTT_THIS_CAMPAIGN, finalRTTAvg);
        output.put(PipDefs.FE_STD_RTT_THIS_CAMPAIGN, finalRTTStd);

        output.put(PipDefs.FE_AVG_SHIFT_THIS_CAMPAIGN, finalShiftAvg);
        output.put(PipDefs.FE_STD_SHIFT_THIS_CAMPAIGN, finalShiftStd);
        output.put(PipDefs.FE_MAX_SHIFT_THIS_CAMPAIGN, finalShiftMax);
        output.put(PipDefs.FE_MIN_SHIFT_THIS_CAMPAIGN, finalShiftMin);

        output.put(PipDefs.FE_LANDMARK_RTT_LIST, rttLandmark);

        output.put(PipDefs.FE_AVG_UNREACHABLES, finalUnreachable);
        output.put(PipDefs.FE_CAMPAIGN_TIMESTAMP, (Timestamp)input.get(PipDefs.FE_TIMESTAMP));
        Float ife_ci = calculateIFEConfidenceInterval(significanceLevel, finalIFE, amount_of_landmarks);
        output.put(PipDefs.FE_IFE_CONFIDENCE_INTERVAL_THIS_CAMPAIGN, ife_ci);
    }


    private ArrayList<Float> getAvgStdMaxMin(ArrayList<Float> array) throws IllegalArgumentException, NotEnoughDataException{
        float max = -Float.MAX_VALUE;
        float min = Float.MAX_VALUE;
        float tot = 0.0f;
        float avg;
        float std;
        if (array==null){
            throw new IllegalArgumentException("Array cannot be null.");
        }
        if (array.size()<=0){
            throw new NotEnoughDataException("Array is empty.");
        }
        int counter = 0;
        for(Float f:array){ /* Average, maximum and minimum. */
            max = (f>max)?f:max;
            min = (f<min)?f:min;
            tot += f;
            counter++;
        }
        avg = (float)tot/(float)counter;
        tot = 0;
        for(Float f:array){ /* Standard deviation. */
            tot += Math.pow(f-avg,2.0f);
        }
        std = (float) Math.sqrt(tot/counter);
        ArrayList<Float> results = new ArrayList<Float>(4);
        results.add(0, avg);
        results.add(1, std);
        results.add(2, min);
        results.add(3, max);
        return results;
    }


    private boolean includeAnomalyTypeInIFE(int anomaly){
        return ((anomaly==GeneralAnomalyDetector.ST_ABNORMAL_SHIFT) ||
                (anomaly==GeneralAnomalyDetector.ST_ABNORMAL_UNREACHABLE));
        
    }


    private boolean includeAnomalyTypeInShift(int anomaly){
        return (anomaly==GeneralAnomalyDetector.ST_ABNORMAL_SHIFT);
    }


    private boolean includeAnomalyTypeInRTT(int anomaly){
        return ((anomaly==GeneralAnomalyDetector.ST_ABNORMAL_SHIFT) ||
                (anomaly==GeneralAnomalyDetector.ST_NORMAL));
    }


    private boolean includeAnomalyTypeInUnreachable(int anomaly){
        return (anomaly==GeneralAnomalyDetector.ST_ABNORMAL_UNREACHABLE);
    }


    float calculateIFEConfidenceInterval(float sign, float temperature, int count){
        float alp = (1.0f - sign)/2;
        float z = NormalDistrib.getInvCDF(1-alp, 0, 1);
        float ife = temperature;
        float temperature_confiint = (float)(z * Math.sqrt((float)ife * (1.0f-(float)ife) / count));
        return temperature_confiint;
    }


    public ArrayList<Object> sendCommand(String command, ArrayList<Object> args) throws UnsupportedCommandException {
        throw new UnsupportedCommandException("No command.");
    }

    
}
