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
public class ReadingEngine implements Configurable, Transceptable, Engine {
	private FrameReader frameReader;
	private FrameBuffer frameBuffer;
	private Transceiver transceiver;
	private boolean consumerError;
	private Thread readingEngine;
	public ReadingEngine(FrameReader frameReader, FrameBuffer frameBuffer, Transceiver transceiver) {
		this.frameReader = frameReader;
		this.frameBuffer = frameBuffer;
		this.transceiver = transceiver;
		this.consumerError = false;
		readingEngine = new Thread(this, getClass().getName() + " Engine");
		transceiver.registerProducer(readingEngine, this);
	}
	public void run() {
		Frame frame;
		try {
			while(!consumerError & ((frame = frameReader.readFrame()) != null && transceiver.isConsuming())) {
				frameBuffer.enbuf(frame);
			}
		}
		catch(InterruptedException e) {
			// shutdown in progress
			Fluid.error("ReadingEngine: Interrupted", 2);
		}
		catch(IOException e) {
			Fluid.error(e, 2);
			transceiver.producerError(e);
		}
		transceiver.unregisterProducer(readingEngine);
	}
	public void configure(Configuration conf) throws ConfigurationException {
		conf.addConfigurable(this);
		Fluid.log("Reading Engine configured", 2);
	}
	public void start() {
		readingEngine.start();
	}
	public void intercept(Exception e) {
		consumerError = true;
		readingEngine.interrupt();
	}
}
