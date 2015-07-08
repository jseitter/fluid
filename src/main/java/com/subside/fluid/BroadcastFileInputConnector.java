package com.subside.fluid;

import java.io.FileInputStream;
import java.io.IOException;

public class BroadcastFileInputConnector extends InputConnector {
	private String filename;
	private FileInputStream fileInputStream;
	private boolean connected;
	public BroadcastFileInputConnector() {
		connected = false;
	}
	public void configure(Configuration conf) throws ConfigurationException {
		filename = conf.getProperty("broadcastFileInputConnector.filename");
		if(filename == null)
			throw new ConfigurationException("broadcastFileInputConnector.filename not specified");
	}
	public void connect() throws ConnectionException {
		try {
			setInputStream(fileInputStream = new FileInputStream(filename));
			connected = true;
		}
		catch(IOException e) {
			throw new ConnectionException(e);
		}
	}
	public synchronized void handleEvent(StreamEvent evt) {
		// just print what the client is saying
		Fluid.log(getClass().getName() + ": " + new String((byte[])evt.getEventObject()), 2);
	}
	public boolean acceptsMoreInputs() {
		return false;
	}
	public void disconnect() throws IOException {
		connected = false;
		if(fileInputStream != null)
			fileInputStream.close();
	}
	public boolean isConnected() {
		return connected;
	}
}
