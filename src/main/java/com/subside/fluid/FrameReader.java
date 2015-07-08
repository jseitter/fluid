/*
 * Created on Mar 2, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.subside.fluid;

import java.io.IOException;

/**
 * @author Lars Samuelsson
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public abstract class FrameReader implements Configurable {
	protected InputConnector inputConnector;
	public void setInputConnector(InputConnector inputConnector) {
		this.inputConnector = inputConnector;
	}
	public abstract Frame readFrame() throws IOException;
    public void configure(Configuration conf) throws ConfigurationException {
    	conf.addConfigurable(this);
    	Fluid.log("FrameReader configured", 2);
    }
}
