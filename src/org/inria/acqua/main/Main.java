package org.inria.acqua.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.inria.acqua.forms.ConfigurationWindow;
import org.inria.acqua.layers.ExternalModule;
import org.inria.acqua.misc.Landmark;
import org.inria.acqua.mjmisc.MiscIP;
import org.inria.acqua.parsers.ConfigParser;
import org.inria.acqua.planetlabpinger.PlanetlabPingerModule;
import org.inria.acqua.plugins.PipelineCreator;
import org.inria.acqua.plugins.campaigngenerator.InverseDumpReaderOnTheFly;
import jargs.gnu.CmdLineParser;

/** 
 * This is the starter class. */
public class Main{
	private static Logger logger = Logger.getLogger(Main.class.getName()); 
	
	/**
	 * Creates a default set of properties for the log4j logging module.
	 * @return properties. */
	public static Properties getSilentLoggingProperties(){
		Properties properties = new Properties();
		properties.put("log4j.rootLogger",				"ERROR,NULL"); 	// By default, do not show anything.
		properties.put("log4j.logger.org",				"ERROR,STDOUT");	// For this module, show warning messages in stdout.
		properties.put("log4j.logger.proactive", 		"ERROR,STDOUT");
		properties.put("log4j.logger.qosprober", 		"ERROR,STDOUT");
		/* NULL Appender. */
		properties.put("log4j.appender.NULL",			"org.apache.log4j.varia.NullAppender");
		/* STDOUT Appender. */
		properties.put("log4j.appender.STDOUT",			"org.apache.log4j.ConsoleAppender");
		properties.put("log4j.appender.STDOUT.Target",	"System.out");
		properties.put("log4j.appender.STDOUT.layout",	"org.apache.log4j.PatternLayout");
		properties.put("log4j.appender.STDOUT.layout.ConversionPattern","%-4r [%t] %-5p %c %x - %m%n");
		return properties;
	}
	
	/**
	 * Creates a default set of properties for the log4j logging module.
	 * @return properties. */
	public static Properties getVerboseLoggingProperties(){
		Properties properties = new Properties();
		properties.put("log4j.rootLogger",				"INFO,STDOUT"); 		// By default, show everything.
		/* STDOUT Appender. */
		properties.put("log4j.appender.STDOUT",			"org.apache.log4j.ConsoleAppender");
		properties.put("log4j.appender.STDOUT.Target",	"System.out");
		properties.put("log4j.appender.STDOUT.layout",	"org.apache.log4j.PatternLayout");
		properties.put("log4j.appender.STDOUT.layout.ConversionPattern","[%20.20c] %5p -     %m%n");
		return properties;
	}
	

	/**
	 * Configures de log4j module for logging.
	 * @param debuglevel debug level (or verbosity level) to be used when loading log4j properties. 
	 * @param givenlogfile path of the file where to put the logs, if null then log4j.properties will be used as default. */
	public static void log4jConfiguration(int debuglevel, String givenlogfile){
		System.setProperty("log4j.configuration", "");
		String defaultlogfile = "log4j.properties";
		if (debuglevel == 3){ // Maximum debug level.
			// We load the log4j.properties file. 
			if (givenlogfile != null && new File(givenlogfile).exists() == true){
				PropertyConfigurator.configure(givenlogfile);
			}else if (new File(defaultlogfile).exists() == true){
				PropertyConfigurator.configure(defaultlogfile);
			}else{
				Properties properties = getVerboseLoggingProperties();
				PropertyConfigurator.configure(properties);
			}
		}else {
			// We do the log4j configuration on the fly. 
			Properties properties = getSilentLoggingProperties();
			PropertyConfigurator.configure(properties);
		}
	}


    public static void main(String args[]) throws Exception{

    	// We tell the command-line parser what to expect. 
        CmdLineParser parser = new CmdLineParser();
        CmdLineParser.Option pconfig = parser.addStringOption('f', "config-file");
        
        CmdLineParser.Option pdebug = parser.addIntegerOption('d', "debug");
        CmdLineParser.Option plogfile = parser.addStringOption('l', "log-file");

        CmdLineParser.Option pgraphical = parser.addBooleanOption('g', "graphical");
        CmdLineParser.Option pmute = parser.addBooleanOption('m', "mute");
        CmdLineParser.Option pmutefile = parser.addBooleanOption('x', "mute-file");
        CmdLineParser.Option pinverse = parser.addBooleanOption('i', "inverse-ife");
        CmdLineParser.Option ppinger = parser.addBooleanOption('p', "pinger-mode");
        CmdLineParser.Option prealtime = parser.addBooleanOption('r', "real-time");
        CmdLineParser.Option pgren = parser.addBooleanOption('a', "grenouille-mode");

        try {
            parser.parse(args);
        }
        catch ( CmdLineParser.OptionException e ) {
            System.err.println(e.getMessage());
	        System.out.println(
                "Incorrect parameters.\n" +
                "Usage:\n" +
                "   <executable> options\n" +
                "Options:\n" +
                "   -g              graphical interface (default pipeline) \n" +
                "   -m              mute mode (no graphical interface), CampaignGenerator -> PingDumper \n" +
                "   -x FILE         mute mode (no CampaignGenerator in the pipeline, so ping-campaings are created by reading a file that contains json object with ping-information)\n" +
                "   -i DIRECTORY IP inverse IFE estimation\n" +
                "   -r              real time IFE generator (from ping-files generated from Planetlab nodes, put through FTP into the local filesystem)\n" +
                "Examples:\n" +
                "   <executable> -g\n" +
                "   <executable> -mf ..\\..\\experimental\\measurements-stageII\\ mo1\n" +
                "   <executable> -i D:\\PFE\\remote\\experimental\\measurements-stageII\\inverse 213.186.117.56 \n" +
                "\n"
                );

            System.exit(2);
        }

		log4jConfiguration(
				(Integer)parser.getOptionValue(pdebug, new Integer(3)), 
				(String)parser.getOptionValue(plogfile, null));
		
		String conffile = (String)parser.getOptionValue(pconfig);
		
//        try{
//            logger.info("Writing starting date...");
//            Misc.deleteFile("universal_start_time.txt");
//            Calendar c = Misc.getUniversalTimeWithInternet();
//            Misc.writeAllFile("universal_start_time.txt", MiscIFE.getTimeFormattedHigh(c) + " " + MiscIFE.getTimeFormattedLow(c));
//        }catch(Exception e){
//            e.printStackTrace();
//        }
        
	// Parsing the arguments. 
        if ((Boolean)parser.getOptionValue(pgraphical, false)){
            /* Run with graphical mode. */
            logger.info("Running graphical mode...");
            ConfigurationWindow.execute(null);
            return;
        }else if ((Boolean)parser.getOptionValue(pmute, false)){
            /* Case when we let the tool running, only debugging. */
            logger.info("Running mute mode (only generating a pings' file)...");
            ConfigParser cp = new ConfigParser(conffile);
            PipelineCreator pc = PipelineCreator.getMuteRunningPipeline(cp);
            ExternalModule ai = new ExternalModule(pc, cp, null, cp.getIFEExecutionPeriod());
            ai.runLoop();
            return;
        }else if ((Boolean)parser.getOptionValue(pmutefile, false)){
            /* Case when we use as input an already existing file (captured before). */
            logger.info("Running mute mode with dump reading...");
            String filename = args[1];
            ConfigParser cp = new ConfigParser(conffile);
            PipelineCreator pc = PipelineCreator.getPipelineProcessFileComplete(cp, filename);
            ExternalModule ai = new ExternalModule(pc, cp, null, cp.getIFEExecutionPeriod());
            ai.runLoop();
            return;
        }else if ((Boolean)parser.getOptionValue(pinverse, false)){
            String monitp = args[2];
            logger.info("Using as monitored point '"+monitp+"'.");
            /* Case when we analyze the samples obtained from other points with inverse IFE. */
            logger.info("Running inverse mode...");
            ConfigParser cp = new ConfigParser(conffile);
            PipelineCreator pc = PipelineCreator.getPipelineProcessInvertedFiles(
                    cp, args[1], /* D:\\PFE\\remote\\experimental\\measurements-stageII\\inverse */
                    new Landmark(MiscIP.solveName(monitp))); /* "213.186.117.56" */
            ExternalModule ai = new ExternalModule(pc, cp, null, cp.getIFEExecutionPeriod());
            ai.runLoop();
            return;

        }else if ((Boolean)parser.getOptionValue(ppinger, false)){
            /* Case when we analyze the samples obtained from other points with inverse IFE. */
            logger.info("Running in Planetlab Pinger mode for Inverse IFE...");
            //ConfigParser cp = new ConfigParser(ConfigParser.EXPERIMENTS_CONFIG_FILE_NAME);
            PlanetlabPingerModule ppm = new PlanetlabPingerModule();
            ppm.start();

            return;
        }else if ((Boolean)parser.getOptionValue(pgren, false)){
            /* Case when we read from stdin the pings, and we put in stdout the anomalies. */
            logger.info("Running in Grenouille mode...");
            ConfigParser cp = new ConfigParser(conffile);
            PipelineCreator pc = PipelineCreator.getPipelineGrenouille(cp);
            ExternalModule ai = new ExternalModule(pc, cp, null, cp.getIFEExecutionPeriod());
            ai.runLoop();
            return;
        }else if ((Boolean)parser.getOptionValue(prealtime, false)){
            /* Case when we analyze on real time the samples obtained from other points with inverse IFE. */

            /* java -jar IFE.jar -j moon 14/08/2011_07:47:32.484_GMT_+0200 */
            String monitp = args[1];

            String dateString = args[2].replace('_', ' ').trim();
            
            logger.info("Running inverse mode from on the fly measurements...");

            logger.info("Using as monitored point '" + monitp + "'.");
            
            ConfigParser cp = new ConfigParser(conffile);

            PipelineCreator pc = PipelineCreator.getInverseOnTheFlyProcessingPipeline(
                    cp, 
                    monitp, InverseDumpReaderOnTheFly.MODE_READ_ALL_FROM,
                    dateString);
            ExternalModule ai = new ExternalModule(pc, cp, null, cp.getIFEExecutionPeriod());
            ai.runLoop();
            return;

        }else{
            /* By default choice... */
            logger.warn("You must specify an action parameter...");
        }
    }
}

