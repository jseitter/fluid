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
public class WritingEngine implements Configurable, Transceptable, Engine {
	private FrameWriter frameWriter;
	private FrameBuffer frameBuffer;
	private Transceiver transceiver;
	private Thread writingEngine;
	private int framesBetweenSync;
	private boolean producerError;
	private boolean saturation;
	public WritingEngine(FrameWriter frameWriter, FrameBuffer frameBuffer, Transceiver transceiver) {
		this.frameWriter = frameWriter;
		this.frameBuffer = frameBuffer;
		this.transceiver = transceiver;
		this.producerError = false;
		this.saturation = false;
		writingEngine = new Thread(this, getClass().getName() + " Engine");
		transceiver.registerConsumer(writingEngine, this);
	}
	public void run() {
		Frame frame;
		Timer timer = new Timer(framesBetweenSync, frameBuffer.capacity());
		try {
			while(!producerError & (!frameBuffer.isEmpty() || transceiver.isProducing())) {
				frame = frameBuffer.debuf();
				if(frame != null) {
					if(saturation) {
						if(frameBuffer.isStarving())
							timer.enterStarvingMode();
						if(frameBuffer.isSatiated())
							timer.enterSatiatedMode();
						if(frameBuffer.isBalanced())
							timer.enterBalancedMode();
					}
					try	{
						Thread.sleep(timer.getDelay(frame));
					}
					catch(InterruptedException e) {
						// nada
					}
					frameWriter.writeFrame(frame);
				}
			}
		}
		catch(InterruptedException e) {
			// shutdown in progress
			Fluid.error("WritingEngine: Interrupted", 2);			
		}
		catch(IOException e) {
			Fluid.error(e, 2);
			transceiver.consumerError(e);
		}
		transceiver.unregisterConsumer(writingEngine);
	}
	public void configure(Configuration conf) throws ConfigurationException {
		conf.addConfigurable(this);
		try {
			framesBetweenSync = Integer.parseInt(conf.getProperty("writingEngine.framesBetweenSync"));
		}
		catch(Exception e) {
			framesBetweenSync = 128;
		}
		saturation = "true".equals(conf.getProperty("writingEngine.saturation"));
		Fluid.log("Writing Engine configured", 2);
	}
	public void start() {
		writingEngine.start();
	}
	public void intercept(Exception e) {
		producerError = true;
		writingEngine.interrupt();
	}
}

