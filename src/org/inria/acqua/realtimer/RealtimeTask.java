
package org.inria.acqua.realtimer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.inria.acqua.exceptions.NoMoreWorkException;
import org.inria.acqua.forms.labelnotification.LabelNotifierEmitter;
import org.inria.acqua.forms.labelnotification.LabelNotifierReceptor;

/**
 * This class performs real-time execution of
 * a task that executes IFE.
 * @author mjost
 */
public class RealtimeTask extends Thread{
	private static Logger logger = Logger.getLogger(RealtimeTask.class.getName()); 
    private RealtimeNotifiable notifiable;
    private boolean alive = false;
    private boolean done = false;
    private int periodMilliSeconds;
    private LabelNotifierEmitter labelNotifierEmitter;

    public RealtimeTask(RealtimeNotifiable rn, int periodMilliSeconds) throws Exception{
        labelNotifierEmitter = new LabelNotifierEmitter();
        this.notifiable = rn;
        this.setPriority(Thread.MAX_PRIORITY);
        if (periodMilliSeconds < 0){
            throw new Exception("Invalid period: " + periodMilliSeconds);
        }
        this.periodMilliSeconds = periodMilliSeconds;
        this.setName("realtime-task");
    }

    public void setLabelNotifierReceptor(LabelNotifierReceptor l){
        labelNotifierEmitter.addLabelNotifierReceptor(l);
    }

    public void setLabelNotifierReceptors(ArrayList<LabelNotifierReceptor> l){
        for(LabelNotifierReceptor r: l){
            labelNotifierEmitter.addLabelNotifierReceptor(r);
        }
    }

   /** Is this task synchronizer active (i.e. running)? */
    public boolean isActive(){
        return alive;
    }

    /** Is this task synchronizer done (i.e. finished running)? */
    public boolean isDone(){
        return done;
    }

    /** Terminate this task synchronizer execution. */
    public void terminate(){
        alive = false;
    }

    

    @Override
    public void run(){
        //long currentms;
        long initialTimeMs;
        //long execution_timems;
        long sleepingTime;
        long executionCounter = 0;
        long nextExecution;
        long now;
        
        
        alive = true;
        initialTimeMs = Calendar.getInstance().getTimeInMillis();
        while(alive==true){
            
            executionCounter++;

            if (executionCounter%10==0){
                logger.info("[day no. "+ String.format(Locale.ENGLISH, "%f",(float)(((float)executionCounter*(float)periodMilliSeconds)/(24.0*60*60*1000)))+"][counter " + executionCounter + "]");
            }
            //currentms = Calendar.getInstance().getTimeInMillis();
            try{
                /* This is to ensure that executions will START periodically. */
                alive = alive && !notifiable.executeAndCheckIfStops();
            }catch(NoMoreWorkException r){ /* We stop (and kill this Realtimer). */
                logger.info(r);
                alive = false;
            }catch(Exception e){ /* We do not stop. We show the problem but we continue. */
                labelNotifierEmitter.showMessageInGUI(e.getMessage(), LabelNotifierReceptor.SERIOUSNESS_NOTIFICATION);
                logger.warn(e.getMessage());
            }
            

            if (periodMilliSeconds>0){
                //execution_timems = Calendar.getInstance().getTimeInMillis() - currentms;
                //sleepingTime = periodMilliSeconds - execution_timems;
                nextExecution = (initialTimeMs + (executionCounter * periodMilliSeconds));
                now = Calendar.getInstance().getTimeInMillis();

                
                sleepingTime = nextExecution - now;

                Date nextdate = new Date(nextExecution); String nextdatestr = nextdate.getHours() + ":" + nextdate.getMinutes() + ":" + nextdate.getSeconds();
                Date nowdate = new Date(now); String nowdatestr = nowdate.getHours() + ":" + nowdate.getMinutes()  + ":" + nowdate.getSeconds();
                try {
                    if (sleepingTime > 0){
                        Thread.sleep(sleepingTime);
                    }else{
                        Thread.sleep(1); /* Minimun period. */
                        throw new Exception("Time const. were not respected (" + sleepingTime + "ms).");
                    }
                } catch (Exception ex) {
                    labelNotifierEmitter.showMessageInGUI(ex.getMessage(), LabelNotifierReceptor.SERIOUSNESS_NOTIFICATION);
                    logger.warn(ex.getMessage());
                }
            }
        }
        done = true;
    }
}
