package org.inria.acqua.plugins.campaigngenerator;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;
import org.inria.acqua.misc.Landmark;
import org.inria.acqua.misc.Timestamp;
import org.inria.acqua.plugins.campaigngenerator.pingabstraction.Pinger;


/**
 * Pings many landmarks concurrently to decrease measurement time. 
 * @author mjost
 */
public class ParallelPinger extends Thread{
	private static Logger logger = Logger.getLogger(ParallelPinger.class.getName()); 
    private int count;
    private int packet_size;
    private int timeout_sec;
    private Landmark landmark;
    private int T_ping_ms;
    private int ppingerID;
    private int campaignID;
    private MeasurementReceiver measReceiver;

    public ParallelPinger(MeasurementReceiver mr, int campaignID, int ppid, Landmark l, int timeout_sec, int packet_size, int count, int T_ping_ms){
        this.measReceiver = mr;
        this.ppingerID = ppid;
        this.count = count;
        this.landmark = l;
        this.timeout_sec = timeout_sec;
        this.packet_size = packet_size;
        this.T_ping_ms = T_ping_ms;
        this.campaignID = campaignID;
    }

    @Override
    public void run(){
        logger.info("\tlandmark '" + landmark.getDescriptiveName() + "'.");
        
        Date initialTime = Calendar.getInstance().getTime();

        /* Refresh ping. */
        try{
            Pinger.ping(landmark.toString(), timeout_sec, packet_size);
        }catch(Exception e){
            logger.info("\tfailed to ping for refresh for '" + landmark.getDescriptiveName() + "'.");
            e.printStackTrace();
        }

        Timestamp[][] pings = new Timestamp[count][2];
        for (int p=0; p<count; p++){

            Date date = Calendar.getInstance().getTime();

            float ping_ms = -1;
            try {
                ping_ms = Pinger.ping(landmark.toString(), timeout_sec, packet_size);
            } catch (Exception e) {
                e.printStackTrace();
            }
            logger.info("\tPing to '"+this.landmark.getDescriptiveName()+"' done (" + p+1 + "/" + count + ").");
            pings[p][0] = new Timestamp(date);
            if (ping_ms<0){
                logger.info("\t\tping #'"+p+"'... timeout");
                try {
                    pings[p][1] = new Timestamp(Timestamp.TIMEOUT);
                } catch (ParseException ex) {
                    ex.printStackTrace();
                }
            }else{
                Date dateplus = new Date(date.getTime()+(long)ping_ms);

                logger.info("\t\tping #'"+p+"'... "+ping_ms+"ms\n");
                pings[p][1] = new Timestamp(dateplus);
            }

            try{
                Thread.sleep(T_ping_ms);
            }catch(Exception e){e.printStackTrace();}
            logger.info("\tInserted and sleep for '" + this.landmark.getDescriptiveName() + "' done ("+ p+1 + " /" + count+ ").");
        }
        try {
            measReceiver.insertMeasurement(campaignID, ppingerID, pings);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Date endTime = Calendar.getInstance().getTime();
        long differenceMs = endTime.getTime() - initialTime.getTime();
        int differenceS = (int)(differenceMs / 1000);
        logger.info("Ping process to '" + this.landmark.getDescriptiveName() + "' took '"+differenceS+"' seconds.");
    }

}
