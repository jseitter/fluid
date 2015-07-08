package com.subside.fluid;

import java.util.Date;

/**
 * The main startup class for the Fluid Streaming Server system.
 * 
 * Configuration:
 * fluid.mediaServerName - the name of the media server class to
 * load and start
 *
 * @author Lars Rönnbäck
 */
public final class Fluid {
	public static final String version = "Fluid Streaming Server/Final-1h";
	// starting time
	public static final Date startingTime = new Date();
	private static int logLevel;
    private Fluid(String filename) throws Exception {
    	Configuration conf = new Configuration(filename);
    	try {
    		logLevel = Integer.parseInt(conf.getProperty("Fluid.logLevel"));
    	}
    	catch(Exception e) {
    		logLevel = 0;
    	}
    	new MediaStream(conf);
    }
    /**
     * The main method where it all begins.
     *
     * @param args The config file name should be given as 
     *             as a command line argument
     */
    public static void main(String args[]) {
    	if(args.length < 1) {
    		System.out.println(Fluid.version);
    		System.out.println("Usage: <JVM> Fluid <config>\n");
    		System.exit(1);
    	}	
    	try {
    		new Fluid(args[0]);
    	}
    	catch(Exception e) {
        	e.printStackTrace(System.err);
        	System.exit(1);
    	}
    }
    public static void log(String message) {
    	log(message, 0);
    }
    public static void log(String message, int level) {
    	if(level <= logLevel)
    		System.out.println("[" + Timestamp.getTimestamp(Timestamp.SHORT) + "] " + message);
    }
    public static void error(String message) {
    	error(message, 0);
    }
    public static void error(String message, int level) {
    	if(level <= logLevel)
    		System.err.println("[" + Timestamp.getTimestamp(Timestamp.SHORT) + "] " + message);
    }
    public static void error(Exception e) {
    	error(e, 0);
    }
    public static void error(Exception e, int level) {
    	if(level <= logLevel) {
    		System.err.print("[" + Timestamp.getTimestamp(Timestamp.SHORT) + "] ");
    		e.printStackTrace(System.err);
    	}
    }
}
