package com.subside.fluid;

import java.text.*;
import java.util.*;

/**
 * A small utility for getting a timestamp string.
 * 
 * @author Lars Samuelsson
 */
public final class Timestamp {
    /**
     * Short format for timestamp
     */
    public static final int SHORT = 0;
    /**
     * Medium format for timestamp
     */
    public static final int MEDIUM = 1;
    /**
     * Long format for timestamp
     */
    public static final int LONG = 2;
    /**
     * Full format for timestamp
     */
    public static final int FULL = 3;
    // formatting stuff
    private static DateFormat shortFormat =
    	DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
    private static DateFormat mediumFormat =
    	DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
    private static DateFormat longFormat =
    	DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);
    private static DateFormat fullFormat =
    	DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
    // invis to javadoc
    private Timestamp() {
    }
    /**
     * Returns a formatted version of the current date
     * and time.
     * 
     * @return Current date and time
     */
    public static String getTimestamp(int format) {
    	switch(format) {
    	case SHORT:
    		return shortFormat.format(new Date());
    	case MEDIUM:
    		return mediumFormat.format(new Date());
    	case LONG:
    		return longFormat.format(new Date());
    	case FULL:
    		return fullFormat.format(new Date());
    	default:
    		return null;
    	}
    }
}
    
