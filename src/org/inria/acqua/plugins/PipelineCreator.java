
package org.inria.acqua.plugins;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.util.SimpleTimeZone;

import org.apache.log4j.Logger;
import org.inria.acqua.layers.HistoryData;
import org.inria.acqua.layers.painter.CurveElement;
import org.inria.acqua.misc.Landmark;
import org.inria.acqua.mjmisc.Misc;
import org.inria.acqua.mjmisc.MiscDate;
import org.inria.acqua.mjmisc.MiscIP;
import org.inria.acqua.parsers.ConfigFileParser;
import org.inria.acqua.plugins.anomalydetector.AnomalyDetectorBernoulli;
import org.inria.acqua.plugins.anomalydetector.GeneralAnomalyDetector;
import org.inria.acqua.plugins.campaigngenerator.CampaignDumpReader;
import org.inria.acqua.plugins.campaigngenerator.CampaignGenerator2G;
import org.inria.acqua.plugins.campaigngenerator.InverseDumpReader;
import org.inria.acqua.plugins.campaigngenerator.InverseDumpReaderOnTheFly;
import org.inria.acqua.plugins.campaigngenerator.StdinCampaignReader;
import org.inria.acqua.plugins.campaigngenerator.pingabstraction.Pinger;
import org.inria.acqua.plugins.ifdumper.IFDumper;
import org.inria.acqua.plugins.ifestimator.IFEstimator;
import org.inria.acqua.plugins.ifestimator.IFEstimatorToCurveElement;


/** 
 * Creator of pipelines. 
 * Edit this class if you want acqua to support more Pipeline configurations. 
 * @author mjost
 */
public class PipelineCreator {
	private static Logger logger = Logger.getLogger(PipelineCreator.class.getName()); 
    private Pipeline pipeline;
    private HistoryData historyDataValue = null;
    private FlowElement inputFlowElement = null;
    

    private PipelineCreator(){}

    public Pipeline getPipeline(){
        return pipeline; 
    }
    
    public HistoryData getHistoryData(){
        return historyDataValue;
    }

    public FlowElement getPredefinedInputFlowElement(){
        return inputFlowElement;
    }

    
    
    
    /*********************/
    /**** APPLICATION ****/
    /*********************/

    public static PipelineCreator getGUIPipeline(ConfigFileParser cp) throws Exception{
    
        /* Create pipeline passing cp, and obtaining historyData also fe standard */

        ArrayList<String> landmarksStr = cp.getStrLandmarks();
        ArrayList<Landmark> landmarks = new ArrayList<Landmark>();

        for(String l:landmarksStr){
            landmarks.add(new Landmark(MiscIP.solveName(l), l));
        }
        landmarks.add(new Landmark(Landmark.LANDMARK_GATEWAY, "Gateway"));

        HashMap<String, CurveElement> curves =
                IFEstimatorToCurveElement.initializeCurveElements();

        HistoryData historyData = new HistoryData(curves);

        /* NEW */

        FlowElement fe = new FlowElement();

        fe.put(PipDefs.FE_VERSION,"1.0a");
        fe.put(PipDefs.FE_INPUT_ID,new Integer((new Random()).nextInt()));
        fe.put(PipDefs.FE_TIMEOUT_MS, cp.getTimeoutSeconds()*1000);
        fe.put(PipDefs.FE_PACKET_SIZE, new Integer(56));
        fe.put(PipDefs.FE_COUNT, cp.getNumberOfPings());
        fe.put(PipDefs.FE_T_PING_MS, new Integer(50));
        //fe.put(PipDefs.FE_SIGNIFICANCE_LEVEL, new Float((float)cp.getSignificanceLevel()/1000.0f));
        fe.put(PipDefs.FE_ANOMALY_DETECTOR_PARAM1, new Double((float)cp.getAnomalyDetectionParameter1()));
        fe.put(PipDefs.FE_ANOMALY_DETECTOR_PARAM2, new Double((float)cp.getAnomalyDetectionParameter2()));
        fe.put(PipDefs.FE_ANOMALY_DETECTOR_PARAM3, new Double((float)cp.getAnomalyDetectionParameter3()));
        fe.put(PipDefs.FE_ANOMALY_DETECTOR_PARAM4, new Double((float)cp.getAnomalyDetectionParameter4()));
        fe.put(PipDefs.FE_ANOMALY_DETECTOR_PARAM5, new Double((float)cp.getAnomalyDetectionParameter5()));

        fe.put(PipDefs.FE_T_CAMP_MS,cp.getIFEExecutionPeriod());


        fe.put(PipDefs.FE_LANDMARKS_LIST,landmarks);

        Pipeline pipeline = new Pipeline();
        
        Pipelineable initialPipelineElement = new CampaignGenerator2G("output.txt");
        pipeline.addAsFirst("generator", initialPipelineElement);

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put(AnomalyDetectorBernoulli.EPSILON_KEY, cp.getAnomalyDetectionParameter1());
        params.put(AnomalyDetectorBernoulli.LOWER_INDEX_KEY, cp.getAnomalyDetectionParameter2());
        params.put(AnomalyDetectorBernoulli.UPPER_INDEX_KEY, cp.getAnomalyDetectionParameter3());
        GeneralAnomalyDetector ad = new GeneralAnomalyDetector(params);
        pipeline.add("ad", ad);

        //GatewayChecker gc = new GatewayChecker(pipeline);
        //pipeline.add(gc);

        IFEstimator ife = new IFEstimator();
        pipeline.add("ife", ife);

        //IFController ic = new IFController(pipeline);
        //pipeline.add(ic);


        initialPipelineElement.addAsSink(ad);
        ad.addAsSink(ife);
        //ad.addAsSink(gc);

        //ife.addAsSink(this);
        //ife.addAsSink(ic);
        //pipeline.add(this);
        //IFDumper ifdumper = new IFDumper(prefix + "ifesummary.txt");
        //ife.addAsSink(ifdumper);

        PipelineCreator pc = new PipelineCreator();
        pc.historyDataValue = historyData;
        pc.pipeline = pipeline;
        pc.inputFlowElement = fe;
        return pc; 
    }


    public static PipelineCreator getMuteRunningPipeline(ConfigFileParser cp) throws Exception{

        /* Create pipeline passing cp, and obtaining historyData also fe standard */

        ArrayList<String> landmarksStr = cp.getStrLandmarks();
        ArrayList<Landmark> landmarks = new ArrayList<Landmark>();

        checkParameters(landmarksStr);
        
        for(String l:landmarksStr){
            landmarks.add(new Landmark(MiscIP.solveName(l), l));
        }
        landmarks.add(new Landmark(Landmark.LANDMARK_GATEWAY, "Gateway"));


        //HashMap<String, CurveElement> curves =
        //        IFEstimatorToCurveElement.initializeCurveElements();

        //HistoryData historyData = new HistoryData(curves);

        /* NEW */

        FlowElement fe = new FlowElement();

        fe.put(PipDefs.FE_VERSION,"1.0a");
        fe.put(PipDefs.FE_INPUT_ID,new Integer((new Random()).nextInt()));
        fe.put(PipDefs.FE_TIMEOUT_MS, cp.getTimeoutSeconds()*1000);
        fe.put(PipDefs.FE_PACKET_SIZE, new Integer(56));
        fe.put(PipDefs.FE_COUNT, cp.getNumberOfPings());
        fe.put(PipDefs.FE_T_PING_MS, new Integer(50));
        //fe.put(PipDefs.FE_SIGNIFICANCE_LEVEL, new Float((float)cp.getSignificanceLevel()/1000.0f));

        fe.put(PipDefs.FE_ANOMALY_DETECTOR_PARAM1, new Double((float)cp.getAnomalyDetectionParameter1()));
        fe.put(PipDefs.FE_ANOMALY_DETECTOR_PARAM2, new Double((float)cp.getAnomalyDetectionParameter2()));
        fe.put(PipDefs.FE_ANOMALY_DETECTOR_PARAM3, new Double((float)cp.getAnomalyDetectionParameter3()));
        fe.put(PipDefs.FE_ANOMALY_DETECTOR_PARAM4, new Double((float)cp.getAnomalyDetectionParameter4()));
        fe.put(PipDefs.FE_ANOMALY_DETECTOR_PARAM5, new Double((float)cp.getAnomalyDetectionParameter5()));

        fe.put(PipDefs.FE_T_CAMP_MS,cp.getIFEExecutionPeriod());


        fe.put(PipDefs.FE_LANDMARKS_LIST,landmarks);

        Pipeline pipeline = new Pipeline();


        //Pipelineable initialPipelineElement = new CampaignDumpReader(dir + prefix + "-output.txt");
        Pipelineable initialPipelineElement = new CampaignGenerator2G("output.txt", true);
        pipeline.addAsFirst("generator", initialPipelineElement);

        //HashMap<String, Object> params = new HashMap<String, Object>();
        //params.put(AnomalyDetectorBernoulli.EPSILON_KEY, default_epsilon);
        //GeneralAnomalyDetector ad = new GeneralAnomalyDetector(params);
        //pipeline.add("ad", ad);

        //GatewayChecker gc = new GatewayChecker(pipeline);
        //pipeline.add(gc);

        //IFEstimator ife = new IFEstimator();
        //pipeline.add("ife", ife);

        //IFController ic = new IFController(pipeline);
        //pipeline.add(ic);


        //initialPipelineElement.addAsSink(ad);
        //ad.addAsSink(ife);
        //ad.addAsSink(gc);

        //ife.addAsSink(this);
        //ife.addAsSink(ic);
        //pipeline.add(this);
        //IFDumper ifdumper = new IFDumper(prefix + "ifesummary.txt");
        //ife.addAsSink(ifdumper);

        PipelineCreator pc = new PipelineCreator();
        pc.historyDataValue = null; //historyData;
        pc.pipeline = pipeline;
        pc.inputFlowElement = fe;
        return pc;
    }


    /*********************/
    /***** PROCESSING ****/
    /*********************/

    public static PipelineCreator getPipelineProcessFileLight(
            ConfigFileParser cp, String directory, String prefix) throws Exception{

        /* Create pipeline passing cp, and obtaining historyData also fe standard */

        ArrayList<String> landmarksStr = cp.getStrLandmarks();
        ArrayList<Landmark> landmarks = new ArrayList<Landmark>();

        for(String l:landmarksStr){
            landmarks.add(new Landmark(MiscIP.solveName(l), l));
        }
        landmarks.add(new Landmark(Landmark.LANDMARK_GATEWAY, "Gateway"));

        //HashMap<String, CurveElement> curves =
        //        IFEstimatorToCurveElement.initializeCurveElements();

        //HistoryData historyData = new HistoryData(curves);

        /* NEW */

        FlowElement fe = new FlowElement();

        fe.put(PipDefs.FE_VERSION,"1.0a");
        fe.put(PipDefs.FE_INPUT_ID,new Integer((new Random()).nextInt()));
        fe.put(PipDefs.FE_TIMEOUT_MS, cp.getTimeoutSeconds()*1000);
        fe.put(PipDefs.FE_PACKET_SIZE, new Integer(56));
        fe.put(PipDefs.FE_COUNT, cp.getNumberOfPings());
        fe.put(PipDefs.FE_T_PING_MS, new Integer(50));
        //fe.put(PipDefs.FE_SIGNIFICANCE_LEVEL, new Float((float)cp.getSignificanceLevel()/1000.0f));
        fe.put(PipDefs.FE_ANOMALY_DETECTOR_PARAM1, new Double((float)cp.getAnomalyDetectionParameter1()));
        fe.put(PipDefs.FE_ANOMALY_DETECTOR_PARAM2, new Double((float)cp.getAnomalyDetectionParameter2()));
        fe.put(PipDefs.FE_ANOMALY_DETECTOR_PARAM3, new Double((float)cp.getAnomalyDetectionParameter3()));
        fe.put(PipDefs.FE_ANOMALY_DETECTOR_PARAM4, new Double((float)cp.getAnomalyDetectionParameter4()));
        fe.put(PipDefs.FE_ANOMALY_DETECTOR_PARAM5, new Double((float)cp.getAnomalyDetectionParameter5()));
        fe.put(PipDefs.FE_T_CAMP_MS,cp.getIFEExecutionPeriod());


        fe.put(PipDefs.FE_LANDMARKS_LIST,landmarks);

        Pipeline pipeline = new Pipeline();

        //pipeline.setHistoryData(historyData);

        Pipelineable initialPipelineElement = new CampaignDumpReader(directory + prefix + "-output.txt");
        //Pipelineable initialPipelineElement = new CampaignGenerator2G("output.txt");
        pipeline.addAsFirst("reader", initialPipelineElement);


        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put(AnomalyDetectorBernoulli.EPSILON_KEY, cp.getAnomalyDetectionParameter1());
        params.put(AnomalyDetectorBernoulli.LOWER_INDEX_KEY, cp.getAnomalyDetectionParameter2());
        params.put(AnomalyDetectorBernoulli.UPPER_INDEX_KEY, cp.getAnomalyDetectionParameter3());
        GeneralAnomalyDetector ad = new GeneralAnomalyDetector(params);
        pipeline.add("ad", ad);

        //GatewayChecker gc = new GatewayChecker(pipeline);
        //pipeline.add(gc);

        //IFEstimator ife = new IFEstimator();
        //pipeline.add("ife", ife);

        //IFController ic = new IFController(pipeline);
        //pipeline.add(ic);


        initialPipelineElement.addAsSink(ad);
        //ad.addAsSink(ife);
        //ad.addAsSink(gc);

        //ife.addAsSink(this);
        //ife.addAsSink(ic);
        //pipeline.add(this);
        //IFDumper ifdumper = new IFDumper(prefix + "ifesummary.txt");
        //ife.addAsSink(ifdumper);


        //pipeline.setPredefinedInputFlowElement(fe);

        PipelineCreator pc = new PipelineCreator();
        pc.historyDataValue = null; //historyData;
        pc.pipeline = pipeline;
        pc.inputFlowElement = fe;
        return pc;
    }

    /**********************************/
    /***** Pipeline for Grenouille ****/
    /**********************************/

    public static PipelineCreator getPipelineGrenouille(
            ConfigFileParser cp) throws Exception{

    	// Put in place the list of landmarks for which pings will be received. 
        ArrayList<Landmark> landmarks = new ArrayList<Landmark>();


        // Create the default FlowElement. 
        FlowElement fe = new FlowElement();

        fe.put(PipDefs.FE_VERSION,"1.0a");
        fe.put(PipDefs.FE_INPUT_ID,new Integer((new Random()).nextInt()));
        fe.put(PipDefs.FE_TIMEOUT_MS, cp.getTimeoutSeconds()*1000);
        fe.put(PipDefs.FE_PACKET_SIZE, new Integer(56));
        fe.put(PipDefs.FE_COUNT, cp.getNumberOfPings());
        fe.put(PipDefs.FE_T_PING_MS, new Integer(50));
        //fe.put(PipDefs.FE_SIGNIFICANCE_LEVEL, new Float((float)cp.getSignificanceLevel()/1000.0f));
        fe.put(PipDefs.FE_ANOMALY_DETECTOR_PARAM1, new Double((float)cp.getAnomalyDetectionParameter1()));
        fe.put(PipDefs.FE_ANOMALY_DETECTOR_PARAM2, new Double((float)cp.getAnomalyDetectionParameter2()));
        fe.put(PipDefs.FE_ANOMALY_DETECTOR_PARAM3, new Double((float)cp.getAnomalyDetectionParameter3()));
        fe.put(PipDefs.FE_ANOMALY_DETECTOR_PARAM4, new Double((float)cp.getAnomalyDetectionParameter4()));
        fe.put(PipDefs.FE_ANOMALY_DETECTOR_PARAM5, new Double((float)cp.getAnomalyDetectionParameter5()));
        fe.put(PipDefs.FE_T_CAMP_MS,cp.getIFEExecutionPeriod());
        fe.put(PipDefs.FE_LANDMARKS_LIST,landmarks);

        // Create the pipeline. 
        Pipeline pipeline = new Pipeline();

        // Start concatenating the different pipeline elements to process
        // the data from the raw pings' information until the IFE. 
        
        // First element of the pipeline: CampaignDumpReader (through stdin). 
//        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        Scanner stdin = new Scanner (System.in);
        Pipelineable initialPipelineElement = new StdinCampaignReader(stdin);
        pipeline.addAsFirst("reader", initialPipelineElement);

        // Second element of the pipeline: single landmark anomaly detector. 
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put(AnomalyDetectorBernoulli.EPSILON_KEY, cp.getAnomalyDetectionParameter1());
        params.put(AnomalyDetectorBernoulli.LOWER_INDEX_KEY, cp.getAnomalyDetectionParameter2());
        params.put(AnomalyDetectorBernoulli.UPPER_INDEX_KEY, cp.getAnomalyDetectionParameter3());
        GeneralAnomalyDetector ad = new GeneralAnomalyDetector(params);
        pipeline.add("ad", ad);

        // Third element of the pipeline: IF Estimator. 
        IFEstimator ife = new IFEstimator();
        pipeline.add("ife", ife);

        IFDumper ifdumper = new IFDumper("ifesummary.logger", true);
        pipeline.add("ifdump", ifdumper);
        
        // Concatenate the different elements of the pipeline. 
        initialPipelineElement.addAsSink(ad);
        ad.addAsSink(ife);
        ife.addAsSink(ifdumper);

        PipelineCreator pc = new PipelineCreator();
        pc.pipeline = pipeline;
        pc.inputFlowElement = fe;
        return pc;
    }


    /*********************/
    /***** PROCESSING ****/
    /*********************/

    public static PipelineCreator getPipelineProcessFileComplete(
            ConfigFileParser cp, String filename) throws Exception{

        /* Create pipeline passing cp, and obtaining historyData also fe standard */

        //ArrayList<String> landmarksStr = cp.getRawProvidedLandmarks();
        ArrayList<Landmark> landmarks = new ArrayList<Landmark>();

        //for(String l:landmarksStr){
        //    landmarks.add(new Landmark(MiscIP.solveName(l), l));
        //}
        //landmarks.add(new Landmark(Landmark.LANDMARK_GATEWAY, "Gateway"));

        //HashMap<String, CurveElement> curves =
                IFEstimatorToCurveElement.initializeCurveElements();

        //HistoryData historyData = new HistoryData(curves);

        /* NEW */

        FlowElement fe = new FlowElement();

        fe.put(PipDefs.FE_VERSION,"1.0a");
        fe.put(PipDefs.FE_INPUT_ID,new Integer((new Random()).nextInt()));
        fe.put(PipDefs.FE_TIMEOUT_MS, cp.getTimeoutSeconds()*1000);
        fe.put(PipDefs.FE_PACKET_SIZE, new Integer(56));
        fe.put(PipDefs.FE_COUNT, cp.getNumberOfPings());
        fe.put(PipDefs.FE_T_PING_MS, new Integer(50));
        //fe.put(PipDefs.FE_SIGNIFICANCE_LEVEL, new Float((float)cp.getSignificanceLevel()/1000.0f));
        fe.put(PipDefs.FE_ANOMALY_DETECTOR_PARAM1, new Double((float)cp.getAnomalyDetectionParameter1()));
        fe.put(PipDefs.FE_ANOMALY_DETECTOR_PARAM2, new Double((float)cp.getAnomalyDetectionParameter2()));
        fe.put(PipDefs.FE_ANOMALY_DETECTOR_PARAM3, new Double((float)cp.getAnomalyDetectionParameter3()));
        fe.put(PipDefs.FE_ANOMALY_DETECTOR_PARAM4, new Double((float)cp.getAnomalyDetectionParameter4()));
        fe.put(PipDefs.FE_ANOMALY_DETECTOR_PARAM5, new Double((float)cp.getAnomalyDetectionParameter5()));
        fe.put(PipDefs.FE_T_CAMP_MS,cp.getIFEExecutionPeriod());


        fe.put(PipDefs.FE_LANDMARKS_LIST,landmarks);

        Pipeline pipeline = new Pipeline();

        //pipeline.setHistoryData(historyData);

        Pipelineable initialPipelineElement = new CampaignDumpReader(filename);
        //Pipelineable initialPipelineElement = new CampaignGenerator2G("output.txt");
        pipeline.addAsFirst("reader", initialPipelineElement);



        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put(AnomalyDetectorBernoulli.EPSILON_KEY, cp.getAnomalyDetectionParameter1());
        params.put(AnomalyDetectorBernoulli.LOWER_INDEX_KEY, cp.getAnomalyDetectionParameter2());
        params.put(AnomalyDetectorBernoulli.UPPER_INDEX_KEY, cp.getAnomalyDetectionParameter3());
        GeneralAnomalyDetector ad = new GeneralAnomalyDetector(params);
        pipeline.add("ad", ad);

        //GatewayChecker gc = new GatewayChecker(pipeline);
        //pipeline.add(gc);

        IFEstimator ife = new IFEstimator();
        pipeline.add("ife", ife);

        //IFController ic = new IFController(pipeline);
        //pipeline.add(ic);


        initialPipelineElement.addAsSink(ad);
        ad.addAsSink(ife);
        //ad.addAsSink(gc);

        //ife.addAsSink(this);
        //ife.addAsSink(ic);
        //pipeline.add(this);
        IFDumper ifdumper = new IFDumper("ifesummary.logger");
        pipeline.add("ifdump", ifdumper);
        ife.addAsSink(ifdumper);

        //pipeline.setPredefinedInputFlowElement(fe);

        PipelineCreator pc = new PipelineCreator();
        //pc.historyDataValue = historyData;
        pc.pipeline = pipeline;
        pc.inputFlowElement = fe;
        return pc;
    }


    public static PipelineCreator getPipelineProcessInvertedFiles(
            ConfigFileParser cp, String directory, Landmark monitoredPoint) throws Exception{

        /* Create pipeline passing cp, and obtaining historyData also fe standard */

        //ArrayList<String> landmarksStr = cp.getRawProvidedLandmarks();
        //ArrayList<Landmark> landmarks = new ArrayList<Landmark>();

        //for(String l:landmarksStr){
        //    landmarks.add(new Landmark(MiscIP.solveName(l), l));
        //}
        //landmarks.add(new Landmark(Landmark.LANDMARK_GATEWAY, "Gateway"));

        //HashMap<String, CurveElement> curves =
        //        IFEstimatorToCurveElement.initializeCurveElements();

        //HistoryData historyData = new HistoryData(curves);

        /* NEW */

        FlowElement fe = new FlowElement();

        fe.put(PipDefs.FE_VERSION,"1.0a");
        fe.put(PipDefs.FE_INPUT_ID,new Integer((new Random()).nextInt()));
        fe.put(PipDefs.FE_TIMEOUT_MS, cp.getTimeoutSeconds()*1000);
        fe.put(PipDefs.FE_PACKET_SIZE, new Integer(56));
        fe.put(PipDefs.FE_COUNT, cp.getNumberOfPings());
        fe.put(PipDefs.FE_T_PING_MS, new Integer(50));
        //fe.put(PipDefs.FE_SIGNIFICANCE_LEVEL, new Float((float)cp.getSignificanceLevel()/1000.0f));
        fe.put(PipDefs.FE_ANOMALY_DETECTOR_PARAM1, new Double((float)cp.getAnomalyDetectionParameter1()));
        fe.put(PipDefs.FE_ANOMALY_DETECTOR_PARAM2, new Double((float)cp.getAnomalyDetectionParameter2()));
        fe.put(PipDefs.FE_ANOMALY_DETECTOR_PARAM3, new Double((float)cp.getAnomalyDetectionParameter3()));
        fe.put(PipDefs.FE_ANOMALY_DETECTOR_PARAM4, new Double((float)cp.getAnomalyDetectionParameter4()));
        fe.put(PipDefs.FE_ANOMALY_DETECTOR_PARAM5, new Double((float)cp.getAnomalyDetectionParameter5()));
        fe.put(PipDefs.FE_T_CAMP_MS, cp.getIFEExecutionPeriod());


        

        Pipeline pipeline = new Pipeline();

        //pipeline.setHistoryData(historyData);

        InverseDumpReader ipe = new InverseDumpReader(directory, monitoredPoint, cp);
        Pipelineable initialPipelineElement = ipe;

        fe.put(PipDefs.FE_LANDMARKS_LIST, ipe.getLandmarkList());

        //Pipelineable initialPipelineElement = new CampaignGenerator2G("output.txt");
        pipeline.addAsFirst("reader", initialPipelineElement);



        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put(AnomalyDetectorBernoulli.EPSILON_KEY, cp.getAnomalyDetectionParameter1());
        params.put(AnomalyDetectorBernoulli.LOWER_INDEX_KEY, cp.getAnomalyDetectionParameter2());
        params.put(AnomalyDetectorBernoulli.UPPER_INDEX_KEY, cp.getAnomalyDetectionParameter3());
        GeneralAnomalyDetector ad = new GeneralAnomalyDetector(params);
        pipeline.add("ad", ad);

        //GatewayChecker gc = new GatewayChecker(pipeline);
        //pipeline.add(gc);

        IFEstimator ife = new IFEstimator();
        pipeline.add("ife", ife);

        //IFController ic = new IFController(pipeline);
        //pipeline.add(ic);


        initialPipelineElement.addAsSink(ad);
        ad.addAsSink(ife);
        //ad.addAsSink(gc);

        //ife.addAsSink(this);
        //ife.addAsSink(ic);
        //pipeline.add(this);

        //pipeline.setPredefinedInputFlowElement(fe);


        IFDumper ifdumper = new IFDumper("ifesummary.logger");
        pipeline.add("ifdump", ifdumper);
        ife.addAsSink(ifdumper);

        PipelineCreator pc = new PipelineCreator();
        pc.historyDataValue = null; //historyData;
        pc.pipeline = pipeline;
        pc.inputFlowElement = fe;
        return pc;
    }
    

    public static PipelineCreator getInverseOnTheFlyProcessingPipeline(
            ConfigFileParser cp, String monitoredPoint, int inverseDumpReaderMode, String str_date) throws Exception{

        /* Create pipeline passing cp, and obtaining historyData also fe standard */

        //ArrayList<String> landmarksStr = cp.getRawProvidedLandmarks();
        //ArrayList<Landmark> landmarks = new ArrayList<Landmark>();

        //for(String l:landmarksStr){
        //    landmarks.add(new Landmark(MiscIP.solveName(l), l));
        //}
        //landmarks.add(new Landmark(Landmark.LANDMARK_GATEWAY, "Gateway"));

        
        

        /* NEW */

        FlowElement fe = new FlowElement();

        fe.put(PipDefs.FE_VERSION,"1.0a");
        fe.put(PipDefs.FE_INPUT_ID,new Integer((new Random()).nextInt()));
        fe.put(PipDefs.FE_TIMEOUT_MS, cp.getTimeoutSeconds()*1000);
        fe.put(PipDefs.FE_PACKET_SIZE, new Integer(56));
        fe.put(PipDefs.FE_COUNT, cp.getNumberOfPings());
        fe.put(PipDefs.FE_T_PING_MS, new Integer(50));
        //fe.put(PipDefs.FE_SIGNIFICANCE_LEVEL, new Float((float)cp.getSignificanceLevel()/1000.0f));
        fe.put(PipDefs.FE_ANOMALY_DETECTOR_PARAM1, new Double((float)cp.getAnomalyDetectionParameter1()));
        fe.put(PipDefs.FE_ANOMALY_DETECTOR_PARAM2, new Double((float)cp.getAnomalyDetectionParameter2()));
        fe.put(PipDefs.FE_ANOMALY_DETECTOR_PARAM3, new Double((float)cp.getAnomalyDetectionParameter3()));
        fe.put(PipDefs.FE_ANOMALY_DETECTOR_PARAM4, new Double((float)cp.getAnomalyDetectionParameter4()));
        fe.put(PipDefs.FE_ANOMALY_DETECTOR_PARAM5, new Double((float)cp.getAnomalyDetectionParameter5()));
        fe.put(PipDefs.FE_T_CAMP_MS, cp.getIFEExecutionPeriod());

        Pipeline pipeline = new Pipeline();

        //pipeline.setHistoryData(historyData);

        /*****************************/
        /* WE HAVE TO CHANGE IT HERE */
        /*****************************/
        
        //String str_date="07/05/1986 13:22:15.333 GMT +0000";

        Date date = MiscDate.parseHumanReadableString(str_date);
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeZone(new SimpleTimeZone(0, "Base"));
        calendar.setTime(date);



        InverseDumpReaderOnTheFly idrf = new InverseDumpReaderOnTheFly(monitoredPoint, inverseDumpReaderMode, calendar);

        Pipelineable initialPipelineElement = idrf;

        //fe.put(PipDefs.FE_LANDMARKS_LIST, idrf.getLandmarkList()); /* This will be done on the fly. */

        pipeline.addAsFirst("reader", initialPipelineElement);


        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put(AnomalyDetectorBernoulli.EPSILON_KEY, cp.getAnomalyDetectionParameter1());
        params.put(AnomalyDetectorBernoulli.LOWER_INDEX_KEY, cp.getAnomalyDetectionParameter2());
        params.put(AnomalyDetectorBernoulli.UPPER_INDEX_KEY, cp.getAnomalyDetectionParameter3());
        GeneralAnomalyDetector ad = new GeneralAnomalyDetector(params);
        pipeline.add("ad", ad);

        
        IFEstimator ife = new IFEstimator();
        pipeline.add("ife", ife);

        initialPipelineElement.addAsSink(ad);
        ad.addAsSink(ife);
        

        IFDumper ifdumper = new IFDumper("ifesummary.logger");
        pipeline.add("ifdump", ifdumper);
        ife.addAsSink(ifdumper);

        PipelineCreator pc = new PipelineCreator();
        pc.historyDataValue = null;
        pc.pipeline = pipeline;
        pc.inputFlowElement = fe;
        return pc;
    }

    private static void checkParameters(ArrayList<String> landmarks) throws Exception{
        logger.info("Checking parameters...");
        String rep;
        if((rep = Misc.areThereEqualElements(landmarks))!=null){
            throw new Exception("There are repeated IP adresses ("+rep+").");
        }

        for (String l: landmarks){
            float resp1 = 1.0f;
            float resp2 = 1.0f;
            resp1 = Pinger.ping(l, 5, 32);
            Thread.sleep(10);
            resp2 = Pinger.ping(l, 5, 32);
            if (resp1<0 && resp2<0){
                throw new Exception("Unreachable landmark: '" + l + "'.");
            }
        }
        logger.info("Done. " + landmarks.size() + " valid landmarks.");
    }
}
