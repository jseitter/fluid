package com.subside.fluid;

/**
 * An interface for use with the Configuration class.
 *
 * @author Lars Samuelsson
 */
public interface Configurable {
    /** 
     * Tells a configurable to configure itself with 
     * the specified configuration.
     *
     * This method is used by the notifyConfigurables()
     * method in the Configuration class.
     *
     * @param conf The associated configuration
     */
    public void configure(Configuration conf) throws ConfigurationException;
}
