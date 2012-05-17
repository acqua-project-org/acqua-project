package forms;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;
import planetlabpinger.PlanetlabPingerModule;
import mjmisc.log.MyLog;
import layers.ExternalModule;
import misc.Landmark;
import misc.MiscIFE;
import mjmisc.Misc;
import mjmisc.MiscDate;
import mjmisc.MiscIP;
import parsers.ConfigParser;
import plugins.PipelineCreator;
import plugins.campaigngenerator.InverseDumpReaderOnTheFly;
import plugins.campaigngenerator.pingabstraction.Pinger;

public class Main{
    
    public static void print(String str){
        System.out.println(str);
    }
    
    public static void main(String args[]) throws Exception{

        try{
            System.out.println("Writing starting date...");
            Misc.deleteFile("universal_start_time.txt");
            Calendar c = Misc.getUniversalTimeWithInternet();
            Misc.writeAllFile("universal_start_time.txt", MiscIFE.getTimeFormattedHigh(c) + " " + MiscIFE.getTimeFormattedLow(c));
        }catch(Exception e){
            e.printStackTrace();
        }
        
        if (args.length >= 1){
            if (args[0].equals("-g")){
                /* Run with graphical mode. */
                System.out.println("Running graphical mode...");
                MyLog.startLog();
                ConfigurationWindow.execute(null);
                return;
            }else if (args[0].equals("-m")){
                /* Case when we let the tool running, only debugging. */
                System.out.println("Running mute mode (only generating a pings' file)...");
                MyLog.startLog();
                ConfigParser cp = new ConfigParser(ConfigParser.DEFAULT_CONFIG_FILE_NAME);
                Misc.deleteFilesFrom("logger",".");
                PipelineCreator pc = PipelineCreator.getMuteRunningPipeline(cp);
                ExternalModule ai = new ExternalModule(pc, cp, null, cp.getIFEExecutionPeriod());
                ai.runLoop();
                return;
            }else if(args[0].equals("-mf")){
                /* Case when we use as input an already existing file (captured before). */
                System.out.println("Running mute mode with dump reading...");
                String filename = args[1];
                ConfigParser cp = new ConfigParser(ConfigParser.EXPERIMENTS_CONFIG_FILE_NAME);
                MyLog.startLog();
                PipelineCreator pc = PipelineCreator.getPipelineProcessFileComplete(cp, filename);
                ExternalModule ai = new ExternalModule(pc, cp, null, cp.getIFEExecutionPeriod());
                ai.runLoop();
                return;
            }else if(args[0].equals("-i")){
                String monitp = args[2];
                System.out.println("Using as monitored point '"+monitp+"'.");
                /* Case when we analyze the samples obtained from other points with inverse IFE. */
                System.out.println("Running inverse mode...");
                MyLog.startLog();
                ConfigParser cp = new ConfigParser(ConfigParser.EXPERIMENTS_CONFIG_FILE_NAME);
                Misc.deleteFilesFrom("logger",".");
                PipelineCreator pc = PipelineCreator.getPipelineProcessInvertedFiles(
                        cp, args[1], /* D:\\PFE\\remote\\experimental\\measurements-stageII\\inverse */
                        new Landmark(MiscIP.solveName(monitp))); /* "213.186.117.56" */
                ExternalModule ai = new ExternalModule(pc, cp, null, cp.getIFEExecutionPeriod());
                ai.runLoop();
                return;

            }else if(args[0].equals("-p")){
                /* Case when we analyze the samples obtained from other points with inverse IFE. */
                System.out.println("Running in Planetlab Pinger mode for Inverse IFE...");
                MyLog.startLog();

                //ConfigParser cp = new ConfigParser(ConfigParser.EXPERIMENTS_CONFIG_FILE_NAME);
                Misc.deleteFilesFrom("logger",".");

                PlanetlabPingerModule ppm = new PlanetlabPingerModule();
                ppm.start();

                return;
            }else if(args[0].equals("-j")){
                /* Case when we analyze on real time the samples obtained from other points with inverse IFE. */

                /* java -jar IFE.jar -j moon 14/08/2011_07:47:32.484_GMT_+0200 */
                String monitp = args[1];

                String dateString = args[2].replace('_', ' ').trim();
                
                System.out.println("Running inverse mode from on the fly measurements...");

                System.out.println("Using as monitored point '" + monitp + "'.");
                
                MyLog.startLog();
                ConfigParser cp = new ConfigParser(ConfigParser.EXPERIMENTS_CONFIG_FILE_NAME);

                PipelineCreator pc = PipelineCreator.getInverseOnTheFlyProcessingPipeline(
                        cp, 
                        monitp, InverseDumpReaderOnTheFly.MODE_READ_ALL_FROM,
                        dateString);
                ExternalModule ai = new ExternalModule(pc, cp, null, cp.getIFEExecutionPeriod());
                ai.runLoop();
                return;

            }else{
                /* By default choice... */
            }
        }
        System.out.println(
                "Incorrect parameters.\n" +
                "Usage:\n" +
                "   java -jar IFE.jar options\n" +
                "Options:\n" +
                "   -g              graphical interface\n" +
                "   -m              mute mode\n" +
                "   -mf FILEPATH    mute mode with file\n" +
                "   -i DIRECTORY IP inverse IFE estimation\n" +
                "Examples:\n" +
                "   java -jar IFE.jar -g\n" +
                "   java -jar IFE.jar -mf ..\\..\\experimental\\measurements-stageII\\ mo1\n" +
                "   java -jar IFE.jar -i D:\\PFE\\remote\\experimental\\measurements-stageII\\inverse 213.186.117.56 \n" +
                "\n"
                );

    }


    /*
    public static void main2(String args[]) throws IOException, Exception{
        ArrayList<File> files = Misc.getListOfFiles("gz", "..\\..\\grenmeas-some\\");
        int i=0;
        for (File input: files){
            String output = input.getCanonicalPath().substring(0, input.getCanonicalPath().length()-3) + ".xml";
            GZip.ungzip(input.getCanonicalPath(), output);
        
            XMLParser xml = new XMLParser(output);
            Node node = xml.queryOneAnswer(
                    "/resultset/row/field/@name"
                );
            String res = node.getNodeValue();
            System.out.println(res);
            File file = new File(output);
            file.delete();
            i++;
            if (i>10){
                break;
            }
        }
    }
    */



    
}