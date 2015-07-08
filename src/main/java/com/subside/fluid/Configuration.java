package com.subside.fluid;

import java.util.Properties;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.IOException;
import java.io.FileInputStream;

/**
 * A class for handling configuration files.
 *
 * This is basically Properties with the extended 
 * functionality of keeping track of Configurable
 * objects and notifying these whenever the 
 * configuration change.
 * 
 * @author Lars Samuelsson
 */
public class Configuration extends Properties {
	private static final long serialVersionUID = 1L;
	private ArrayList configurables;
    /** 
     * Create a configuration from the specified config file.
     * 
     * @param configFile A configuration file (properties)
     */
    public Configuration(String configFile) throws IOException {
    	super();
    	configurables = new ArrayList();
    	FileInputStream file = new FileInputStream(configFile);
    	load(file);
    }
    /**
     * Adds a Configurable object to the list of objects 
     * associated with this configuration.
     *
     * @param configurable A Configurable that will  
     *                     subscribe to configuration changes
     */
    public void addConfigurable(Configurable configurable) {
    	configurables.add(configurable);
    }
    /**
     * Removes a Configurable object from the list of objects 
     * associated with this configuration.
     *
     * @param configurable A Configurable that will cancel its 
     *                     subscription to configuration changes
     */
    public void removeConfigurable(Configurable configurable) {
    	configurables.remove(configurable);
    }
    /**
     * Used to notify the associated Configurables that there
     * has been a change in the configuration.
     */
    public void notifyConfigurables() throws ConfigurationException {
    	synchronized(configurables) {
    		Iterator iter = configurables.iterator();
    		while(iter.hasNext()) {
    			((Configurable)iter.next()).configure(this);
    		}
    	}
	}
}
