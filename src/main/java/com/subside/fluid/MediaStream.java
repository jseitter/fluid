/*
 * Created on 2005-jan-11
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.subside.fluid;

import java.io.IOException;

/**
 * @author Lars
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class MediaStream implements Runnable {
	private Thread streamEngine;
	private Configuration conf;
	public MediaStream(Configuration conf) {
		this.conf = conf;
		// do as much processing as possible in the thread
		this.streamEngine = new Thread(this, getClass().getName() + " Engine");
		this.streamEngine.start();
	}
	public void run() {
		Fluid.log("MediaStream starting", 2);
		boolean spawnedNewStream = false;
		InputConnector inputConnector = null;
		OutputConnector outputConnector = null;
		Transceiver transceiver = null;
		try {
			// read the configuration
			String frameReaderName = conf.getProperty("mediaStream.frameReaderName");
			if(frameReaderName == null)
				throw new ConfigurationException("mediaStream.frameReaderName not specified");
			String frameWriterName = conf.getProperty("mediaStream.frameWriterName");
			if(frameWriterName == null)
				throw new ConfigurationException("mediaStream.frameWriterName not specified");
	    	String inputConnectorName = conf.getProperty("mediaStream.inputConnector");    	
	    	if(inputConnectorName == null)
				throw new ConfigurationException("mediaStream.inputConnector not specified");
	    	String outputConnectorName = conf.getProperty("mediaStream.outputConnector");    	
	    	if(outputConnectorName == null)
				throw new ConfigurationException("mediaStream.outputConnector not specified");
			String frameBufferSize = conf.getProperty("mediaStream.frameBufferSize");
			if(frameBufferSize == null)
				throw new ConfigurationException("mediaStream.frameBufferSize not found");
			// create a frame reader
			FrameReader frameReader = null;
			try {
				Class frameReaderClass = Class.forName(frameReaderName);
				frameReader = (FrameReader)frameReaderClass.newInstance();
			}
			catch(Exception e) {
				throw new ConfigurationException(e);
			}
			// configure the frame reader
			frameReader.configure(conf);
			// create an input connector
	    	try {
	    		Class inputConnectorClass = Class.forName(inputConnectorName);
	    		inputConnector = (InputConnector)inputConnectorClass.newInstance();
			} 
	    	catch(Exception e) {
	    		throw new ConfigurationException(e);
	    	}
	    	// configure the input connector
    		inputConnector.configure(conf);
    		// set the input connector of the frame reader
	    	frameReader.setInputConnector(inputConnector);
	    	// create a frame writer
	    	FrameWriter frameWriter = null;
			try {
				Class frameWriterClass = Class.forName(frameWriterName);
				frameWriter = (FrameWriter)frameWriterClass.newInstance();
			}
			catch(Exception e) {
				throw new ConfigurationException(e);
			}
			// configure the frame writer
			frameWriter.configure(conf);	    
			// create an output connector
	    	try {
	    		Class outputConnectorClass = Class.forName(outputConnectorName);
	    		outputConnector = (OutputConnector)outputConnectorClass.newInstance();
			} 
	    	catch(Exception e) {
	    		throw new ConfigurationException(e);
	    	}
	    	// configure the output connector
    		outputConnector.configure(conf);
    		// set the output connector of the frame writer
	    	frameWriter.setOutputConnector(outputConnector);
	    	// create a frame buffer
			FrameBuffer frameBuffer = new FrameBuffer(Integer.parseInt(frameBufferSize));
			// create the transceiver
			transceiver = new Transceiver();
			// create the reading engine
			ReadingEngine readingEngine = new ReadingEngine(frameReader, frameBuffer, transceiver);
			// configure the reading engine
			readingEngine.configure(conf);
			// create the writing engine
			WritingEngine writingEngine = new WritingEngine(frameWriter, frameBuffer, transceiver);
			// configure the writing engine
			writingEngine.configure(conf);
			// have the outputConnector listen to the inputConnector
			outputConnector.addStreamEventListener(inputConnector);
			// have the inputConnector listen to the outputConnector
			inputConnector.addStreamEventListener(outputConnector);
			// connect to the media target (may block)
			outputConnector.connect();
			// set up a new stream if we are allowing connections to more targets
			if(outputConnector.acceptsMoreOutputs()) {
				new MediaStream(conf);
				spawnedNewStream = true;
			}
			// connect to the media source (may block)
			inputConnector.connect();
			// set up a new stream if we are allowing connections to more sources
			if(inputConnector.acceptsMoreInputs()) {
				new MediaStream(conf);
				spawnedNewStream = true;
			}
			// start the reading engine
			readingEngine.start();
			// start the writing engine
			writingEngine.start();
			// waiting for production engine to finish
			transceiver.waitForProducers();
			// disconnect from the media source
			if(inputConnector.isConnected())
				inputConnector.disconnect();
			// waiting for consumption engine to finish
			transceiver.waitForConsumers();
			// disconnect from the media target
			if(outputConnector.isConnected())
				outputConnector.disconnect();
		}
		catch(ConfigurationException e) {
			// malconfigured - die
			Fluid.error(e);
			if(transceiver != null) {
				transceiver.producerError(e);
				transceiver.consumerError(e);
			}
		} 
		catch(ConnectionException e) {
			// serious connection error - die
			Fluid.error(e);
			if(transceiver != null) {
				transceiver.producerError(e);
				transceiver.consumerError(e);
			}
		}
		catch(IOException e) {
			// all other i/o exeptions - possibly restart
			Fluid.error(e, 1);
			if(transceiver != null) {
				transceiver.producerError(e);
				transceiver.consumerError(e);
			}
			if(!spawnedNewStream)
				new MediaStream(conf);
		}
		catch(Exception e) {
			// something really bad happened - die
			Fluid.error(e);
			if(transceiver != null) {
				transceiver.producerError(e);
				transceiver.consumerError(e);
			}
		}
		finally {
			// close all connections
			try {
				if(inputConnector.isConnected())
					inputConnector.disconnect();
			}
			catch(Exception e) {
				// ignore
			}
			try {
				if(outputConnector.isConnected())
					outputConnector.disconnect();
			}
			catch(Exception e) {
				// ignore
			}
		}
		Fluid.log("MediaStream stopping", 2);
	}
}
