
package org.inria.acqua.planetlabpinger;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import org.inria.acqua.misc.FTPClientWrapper;
import org.inria.acqua.mjmisc.Misc;
import org.inria.acqua.mjmisc.MiscIP;
import org.inria.acqua.realtimer.RealtimeNotifiable;
import org.inria.acqua.realtimer.RealtimeTask;


public class PlanetlabPingerModule implements RealtimeNotifiable{

    private String FTPServerName;
    private int periodOfCampaign = -1;
    
    private int internalCounter = 0;

    private int dynamic_pingTimeout_sync = 3;
    private int dynamic_sizeOfPingPacket_sync = 56;
    private int dynamic_pingsPerCampaign_sync = 2;
    private int dynamic_inputID_sync;

    private String currentLocalIP_sync = "";
    private Calendar previous;

    private String FTPLoginName = "test";
    private String FTPPass = "test";

    private ArrayList<Subscription> subscriptionNames_sync = new ArrayList<Subscription>();
    private int dynamic_myRandomInt;
    private int dynamic_myLimitRandomInt;
    
    public PlanetlabPingerModule() throws Exception{
        setDynamic_myLimitRandomInt(30); /* Default value. It will be updated. */
        setDynamic_myRandomInt((new Random()).nextInt(getDynamic_myLimitRandomInt()));


        System.out.println("Random integer: " + getDynamic_myRandomInt() + "(maximum is " + getDynamic_myLimitRandomInt() + ").");
        /*  Read configuration file.
            This should contain
                FTP server
                period of campaign
                count
            Read configuration file.

         */

        this.FTPLoginName = "acqua";
        this.FTPPass = "_FTP17820_Client"; /* i=13365*4/3 */

        ArrayList<String> conffile =
                Misc.filterEmptyLines(Misc.getLines(Misc.readAllFile("planetlabpinger_config.txt")));
        for(String item: conffile){
            String[] raw_info = item.split(" ");
            if (raw_info[0].equals("FTP_SERVER")){
                this.FTPServerName = raw_info[1];
            }else if (raw_info[0].equals("PERIOD_OF_CAMPAIGN_MS")){
                this.periodOfCampaign = Integer.parseInt(raw_info[1]);
            }else{
                throw new Exception("In PPM configuration file, the item '" + raw_info[0] + "' is unknown.");
            }
        }

        if (this.periodOfCampaign == -1 || this.FTPServerName == null){
            throw new Exception("In PPM configuration file, not all the mandatory items are included.");
        }

        /* Connecting to the FTP server... */
        
        
        
    }

    public void start() throws Exception{

        RealtimeTask rttask = new RealtimeTask(this, periodOfCampaign);
        rttask.start();
    }

    public boolean executeAndCheckIfStops() throws Exception {

        if (Thread.activeCount() > 100){
            System.out.println("More than 100 active threads. Killing for safeness of the Planetlab node...");
            System.exit(-1);
        }
        
        Calendar calendarnow;
        try{
            calendarnow = Misc.getUniversalTime();
        }catch(Exception e){
            /* If we cannot get the current time if better to stop working... */
            e.printStackTrace();
            return true;
        }

        Calendar calendarnow_corrected = calendarnow;
        System.out.println("\n*** Cycle started: '" + Misc.calendarToString(calendarnow) + "', pingers started ("+Thread.activeCount()+" threads)...");

        /*
            In each periodic execution...;
                Check the set of nodes to ping (with i%10==0) in the FTP server and save this info locally;
                Do the ping towards the points that requested it;
                Upload the results (with i%2==0);
        */

        /* We get the set of accounts with subscription from the FTP server. */
        if (internalCounter==0 || internalCounter%getDynamic_myLimitRandomInt()==getDynamic_myRandomInt()){
            this.updateLocalParameters();
        }

        if (previous != null){ /* It is not the first cycle. */
            /* Make correction to prevent having two cycles with same minute or minute with difference of 2. */
            long difference;
            difference = calendarnow.getTimeInMillis() - previous.getTimeInMillis();
            /* The difference should be exactly 1000ms*60 */
            if (difference > 60*1000 + 15*1000 || difference < 60*1000 - 15*1000){
                System.out.println("++Strange difference of: " + difference + " now:" + Misc.calendarToString(calendarnow) + " previous:" + Misc.calendarToString(previous));
            }else{
                System.out.println("Real time diff: " + difference);
            }
            Misc.appendToFile("difference.txt", calendarnow.getTimeInMillis() + "\n");

            calendarnow_corrected = (Calendar) previous.clone();
            calendarnow_corrected.setTimeInMillis(previous.getTimeInMillis() + 60 * 1000);

            difference = calendarnow_corrected.getTimeInMillis() - previous.getTimeInMillis();    
        }

        previous = calendarnow_corrected;

        System.out.println("Current subscriptions ("+getSubscriptionNames().size()+"): " + 
                Misc.collectionToString(getSubscriptionNames()));

        for(Subscription subs: getSubscriptionNames()){    
            MechanicalPingerAndUploader mpau =
                    new MechanicalPingerAndUploader(subs, FTPServerName, FTPLoginName, FTPPass);
            mpau.setParameters(getDynamic_pingsPerCampaign(), getDynamic_pingTimeout(), getDynamic_sizeOfPingPacket(),
                    calendarnow_corrected, getCurrentLocalIP(), getDynamic_inputID());
            mpau.start();
        }
            
        internalCounter++;
        return false;
    }

    private void updateLocalParameters(){
        Runnable r = new Runnable(){
            public void run(){
                System.out.println("\t*** Loading subscriptions and configuration from server...");
                try{
                    setCurrentLocalIP(MiscIP.getPublicIPAddress());

                    FTPClientWrapper clientFTP = new FTPClientWrapper(FTPServerName, FTPLoginName, FTPPass);

                    String raw = clientFTP.downloadAsStringWithTimeout("/pinger/remoteconf.txt", 30*1000);
                    
                    ArrayList<String> lines = Misc.filterEmptyLines(Misc.getLines(raw));
                    loadExecutionParameters(lines);
                    setSubscriptionNames(Subscription.parseMany(lines));
                    System.out.println("\tCurrent subscriptions ("+getSubscriptionNames().size()+"): " + Misc.collectionToString(getSubscriptionNames()));
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        };
        Thread t = new Thread(r);
        t.start();
    }

    private void loadExecutionParameters(ArrayList<String> lines){
        for(String s:lines){
            try{
                String sections[] = s.split(" ");
                if (sections[0].equals("*ping_timeout")){
                    setDynamic_pingTimeout(Integer.parseInt(sections[1]));
                    System.out.println("\tPing Timeout updated: " + getDynamic_pingTimeout());
                }else if(sections[0].equals("*ping_packet_size_bytes")){
                    setDynamic_sizeOfPingPacket(Integer.parseInt(sections[1]));
                    System.out.println("\tPing packet size updated: " + getDynamic_sizeOfPingPacket());
                }else if(sections[0].equals("*pings_per_campaign")){
                    setDynamic_pingsPerCampaign(Integer.parseInt(sections[1]));
                    System.out.println("\tPings per campaign updated: " + getDynamic_pingsPerCampaign());
                }else if(sections[0].equals("*input_id")){
                    setDynamic_inputID(Integer.parseInt(sections[1]));
                    System.out.println("\tInput ID updated: " + getDynamic_inputID());
                }else if(sections[0].equals("*parameters_refresh_in_minutes")){
                    setDynamic_myLimitRandomInt(Integer.parseInt(sections[1]));
                    setDynamic_myRandomInt((new Random()).nextInt(getDynamic_myLimitRandomInt()));
                    System.out.println("\tParameters refresh period (minutes) updated: " + getDynamic_myLimitRandomInt());
                    System.out.println("\tNew random int generated: " + getDynamic_myRandomInt());

                }else if(sections[0].equals("*finalize")){
                    System.out.println("\tFINISHED.");
                    System.exit(0);


                }else if(sections[0].startsWith("**")){ 
                    // It is a comment, omit it.
                }else if(sections[0].startsWith("*") == false){
                    // It is a client.
                }else{
                    System.out.println("\tUnknown option '" + sections[0] + "'.");
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }



    private synchronized void setCurrentLocalIP(String ip){
        this.currentLocalIP_sync = ip;
    }

    private synchronized String getCurrentLocalIP(){
        return currentLocalIP_sync;
    }


    private synchronized void setDynamic_pingTimeout(int a){
        this.dynamic_pingTimeout_sync = a;
    }
    private synchronized int getDynamic_pingTimeout(){
        return this.dynamic_pingTimeout_sync;
    }

    private synchronized void setDynamic_sizeOfPingPacket(int a){
        this.dynamic_sizeOfPingPacket_sync = a;
    }
    private synchronized int getDynamic_sizeOfPingPacket(){
        return this.dynamic_sizeOfPingPacket_sync;
    }

    private synchronized void setDynamic_pingsPerCampaign(int a){
        this.dynamic_pingsPerCampaign_sync = a;
    }
    private synchronized int getDynamic_pingsPerCampaign(){
        return this.dynamic_pingsPerCampaign_sync;
    }

    private synchronized void setDynamic_inputID(int a){
        this.dynamic_inputID_sync = a;
    }
    private synchronized int getDynamic_inputID(){
        return this.dynamic_inputID_sync;
    }

    private synchronized void setSubscriptionNames(ArrayList<Subscription> d){
        subscriptionNames_sync = d;
    }

    private synchronized ArrayList<Subscription> getSubscriptionNames(){
        return subscriptionNames_sync;
    }

    private synchronized void setDynamic_myLimitRandomInt(int d){
        dynamic_myLimitRandomInt = d;
    }


    private synchronized int getDynamic_myLimitRandomInt(){
        return dynamic_myLimitRandomInt;
    }


    private synchronized void setDynamic_myRandomInt(int d){
        dynamic_myRandomInt = d;
    }


    private synchronized int getDynamic_myRandomInt(){
        return dynamic_myRandomInt;
    }




}
