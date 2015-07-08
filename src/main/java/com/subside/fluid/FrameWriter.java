/*
 * Created on Dec 29, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.subside.fluid;

import java.io.IOException;

/**
 * @author lars
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public abstract class FrameWriter implements Configurable {
	protected OutputConnector outputConnector;
	public void setOutputConnector(OutputConnector outputConnector) {
		this.outputConnector = outputConnector;
	}
	public abstract void writeFrame(Frame frame) throws IOException;	
    public void configure(Configuration conf) throws ConfigurationException {
    	conf.addConfigurable(this);
    	Fluid.log("FrameWriter configured", 2);
    }
}
