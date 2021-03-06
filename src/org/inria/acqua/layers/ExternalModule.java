package org.inria.acqua.layers;
import java.io.*;
import java.util.*;

import org.apache.log4j.Logger;
import org.inria.acqua.exceptions.NoMoreWorkException;
import org.inria.acqua.exceptions.UnsupportedCommandException;
import org.inria.acqua.forms.labelnotification.LabelNotifierEmitter;
import org.inria.acqua.forms.labelnotification.LabelNotifierReceptor;
import org.inria.acqua.misc.*;
import org.inria.acqua.mjmisc.Misc;
import org.inria.acqua.parsers.ConfigFileParser;
import org.inria.acqua.plugins.*;
import org.inria.acqua.plugins.ifecontroller.IFController;
import org.inria.acqua.plugins.ifestimator.IFEstimatorToCurveElement;
import org.inria.acqua.realtimer.RealtimeNotifiable;
import org.inria.acqua.realtimer.RealtimeTask;

/**
 * This class connects all the components of the system (including the pipeline)
 * and uses them to make the system work, providing the timing.
 * It manages all data flowing through the pipeline. 
 * @author mjost
 */
public class ExternalModule implements Pipelineable, RealtimeNotifiable{
	private static Logger logger = Logger.getLogger(ExternalModule.class.getName()); 
    private ArrayList<ChannelController> channelControllers =
            new ArrayList<ChannelController>();
    private int taskExecutionPeriodCampaignMs;        /** Period to execute IFE. */
    private RealtimeTask realtimeTask;      /** Object that adjusts the timing to fulfill the timing constraints. */
    private boolean stopAnyThread = false;
    private boolean executionPaused = false;
    private LabelNotifierEmitter labelNotifierEmitter;
    private long numberOfLoops = -1;
    private String sessionFileName;
    private HistoryData historyData;
    private Integer rangeX1 = 0;
    private Integer rangeX2 = 100;
    private ConfigFileParser cp;
    private Pipeline pipeline;
    private FlowElement inputFlowElement;
    private long counter = 0;

    public ExternalModule(PipelineCreator pc, ConfigFileParser cpar, LabelNotifierReceptor lab, int campaign_period_ms) throws Exception{

        this.labelNotifierEmitter = new LabelNotifierEmitter();

        this.labelNotifierEmitter.addLabelNotifierReceptor(lab);

        this.cp = cpar;
        
        taskExecutionPeriodCampaignMs = campaign_period_ms;
        
        sessionFileName = cp.getSessionFileName();

        pipeline = pc.getPipeline();
        
        Pipelineable p = pipeline.get("ife");

        if (p!=null){
            p.addAsSink(this);
        }

        historyData = pc.getHistoryData();
        inputFlowElement = pc.getPredefinedInputFlowElement();

    }

    public void addChannelController(ChannelController value){
        channelControllers.add(value);
    }

    /** Notify the change of data to each channel. */
    private void notifyChanges(HistoryData history) throws Exception{
        for (ChannelController cc: channelControllers){
            cc.refreshCurve(history, rangeX1, rangeX2);
        }
    }

    /** Changing the signatures we tell each curve what to print specifically. */
    public String getAllSignaturesFromChannelControllers(){
        Iterator<ChannelController> i = channelControllers.iterator();
        ChannelController current;
        String ret = "";
        while(i.hasNext()){
            current = i.next();
            ret = ret + " " + current.getSignature();
        }
        return ret.trim();
    }

    /** Changing the signatures we tell each curve what to print specifically. */
    public void setAllSignaturesOfChannelControllers(String sign){
        sign = sign.trim();
        try{
            Iterator<ChannelController> i = channelControllers.iterator();
            ChannelController current;
            while(i.hasNext()){
                int cut = sign.indexOf(' ');
                current = i.next();
                if (i.hasNext()){
                    current.setSignature(sign.substring(0, cut).trim());
                    sign = sign.substring(cut).trim();
                }else{
                    current.setSignature(sign.trim());
                }
            }
            this.notifyChanges(historyData);
        }catch(Exception e){
            labelNotifierEmitter.showMessageInGUI("Cannot assign all the new signatures to the curves.", LabelNotifierReceptor.SERIOUSNESS_ATTENTION);
            e.printStackTrace();
        }
    }


    public void runLoop() throws Exception{
        runNLoops(-1);
    }


    /** Here the realtime task is created and executed if necessary. */
    public void runNLoops(long numberOfLoops) throws Exception{
        this.numberOfLoops = numberOfLoops;
        if (realtimeTask!=null){
            if (realtimeTask.isActive() && !realtimeTask.isDone()){
                realtimeTask.terminate();
            }else{
                realtimeTask = new RealtimeTask(this, taskExecutionPeriodCampaignMs);
                realtimeTask.setLabelNotifierReceptors(this.labelNotifierEmitter.getReceptors());
                realtimeTask.start();
            }
        }else{
            realtimeTask = new RealtimeTask(this, taskExecutionPeriodCampaignMs);
            realtimeTask.setLabelNotifierReceptors(this.labelNotifierEmitter.getReceptors());
            realtimeTask.start();
        }
    }

    /** Execute periodic task and check whether is time to stop or not. */
    public boolean executeAndCheckIfStops() throws Exception{
        if (!executionPaused){

            numberOfLoops = (numberOfLoops>0?numberOfLoops-1:numberOfLoops);
            if (numberOfLoops==0){ /* Condition reached because of N looped executions. */
                this.saveContentofChannelControllers(new File(sessionFileName));
                logger.info("Execution correct.");
                System.exit(0);
            }else if (numberOfLoops>0){
                logger.info("Step " + numberOfLoops + "...");
            }

            try{
                this.runCampaign();
            }catch(NoMoreWorkException e){ /* Work done. */
                this.setExecutionPaused(true);
                stopAnyThread = true;
                throw e;
            }catch(Exception e){ /* Error... */
                if (Defs.STOP_MEASUREMENT_ON_IFE_ERROR == true){
                    this.setExecutionPaused(true);
                    stopAnyThread = true;
                }
                labelNotifierEmitter.showMessageInGUI(e.getMessage(), LabelNotifierReceptor.SERIOUSNESS_ERROR);
                logger.warn(e.getMessage());
                throw e;
            }
        }
        return stopAnyThread;
    }

    private void runCampaign() throws Exception{
        pipeline.insertFlowElement(inputFlowElement, PipDefs.SIGN_INPUT);
    }

    public void stopAnyThread(){
        this.stopAnyThread = true;
    }

    public void setExecutionPaused(boolean b){
        this.executionPaused = b; 
    }

    public boolean getExecutionPaused(){
        return this.executionPaused;
    }

    /** Save this session, including all data collected. */
    public void saveContentofChannelControllers(File filename){
        
        FileOutputStream fos = null;
        ObjectOutputStream out = null;
        ArrayList<Object> arr = new ArrayList<Object>();

        arr.add(historyData);
        arr.add(this.channelControllers);

        try{
            fos = new FileOutputStream(filename);
            out = new ObjectOutputStream(fos);
            out.writeObject(arr);
            out.close();
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    /** Load a previously saved session. */
    public void loadContentofChannelControllers(File filename, ArrayList<Curve> curves) throws Exception{
        FileInputStream fis = null;
        ObjectInputStream in = null;
        ArrayList<Object> arr = null;

        fis = new FileInputStream(filename);
        in = new ObjectInputStream(fis);
        arr = (ArrayList<Object>)in.readObject();
        in.close();
        
        historyData = (HistoryData)arr.get(0);
        channelControllers = (ArrayList<ChannelController>)arr.get(1);

        int i=0;
        for (ChannelController cc: channelControllers) {
            curves.get(i).setCaption(cc.getTitle());
            cc.setCurve(curves.get(i++));
        }
        
        this.notifyChanges(historyData);
    }

    /** Updates the plots with a given range. */
    public synchronized void changeContentRange(Integer deltaX1, Integer deltaX2) throws Exception{
        int size = historyData.getSize();

        this.rangeX1 += deltaX1;
        this.rangeX2 += deltaX2;

        if (rangeX1>=rangeX2){
            int aux;
            aux = rangeX1;
            rangeX1 = rangeX2;
            rangeX2 = aux;
        }
        rangeX1=(rangeX1<0)?0:rangeX1;
        rangeX2=(rangeX2>size-1)?size-1:rangeX2;
        if (rangeX2-rangeX1<20){
            if (rangeX1>0){
                rangeX1--;
            }else{
                rangeX2++;
            }
        }

        this.notifyChanges(historyData);
    }

    /** Initializes the tool environment, by deleting some files that must be deleted every new session. */
    public void initToolEnvironment(){
        try{
            Misc.deleteFilesFrom("log", ".");
        }catch(Exception e){
            logger.warn("Cannot prepare environment for the tool: " + e.getMessage());
            System.exit(-1);
        }
    }

    public void addAsSink(Pipelineable p) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void insertFlowElement(FlowElement fe, String signature) throws Exception {
        if (counter++%300==0){
            logger.info("\t\t\t\t\t\t\t\tCycle multiple of 300 reached...");
        }
        //System.out.println("Needs to be corrected. 2 types of pipeline are managed on pipeline creator, there is no reason why to assume that all pipelines will end with an ifestimator, which is the assumption we're doing here while using IFEstimatorToCurveElement\n");
        if (historyData != null){
            try{
                IFEstimatorToCurveElement.addFlowElementToCurveElement(fe, historyData.getCurveElements());
                this.notifyChanges(historyData);
            }catch(Exception e){
                logger.warn("Error inserting FlowElement: " + e.getMessage(), e);
            }
        }
    }

    public void putCommand(String command) throws Exception{
        pipeline.putCommand(command, null);
    }

    public ArrayList<Object> sendCommand(String command, ArrayList<Object> args) throws UnsupportedCommandException {
        if (IFController.COMMAND_NOTIFY_TO_USER.equals(command)){
            this.labelNotifierEmitter.showMessageInGUI((String)args.get(0), LabelNotifierReceptor.SERIOUSNESS_ATTENTION);
            return null;
        }

        throw new UnsupportedCommandException(command);
        
    }

    public boolean isRunning(){
        return !this.executionPaused;
    }
}
