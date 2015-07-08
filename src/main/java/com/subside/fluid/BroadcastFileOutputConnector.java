package com.subside.fluid;

import java.io.IOException;
import java.io.FileOutputStream;

public class BroadcastFileOutputConnector extends OutputConnector {
	private String filename;
	private FileOutputStream fileOutputStream;
	private boolean connected;
	public BroadcastFileOutputConnector() {
		connected = false;
	}
	public void configure(Configuration conf) throws ConfigurationException {
		filename = conf.getProperty("broadcastFileOutputConnector.filename");
		if(filename == null)
			throw new ConfigurationException("broadcastFileOutputConnector.filename not specified");
	}
	public void connect() throws ConnectionException {
		try {
			setOutputStream(fileOutputStream = new FileOutputStream(filename));
			connected = true;
		}
		catch(IOException e) {
			throw new ConnectionException(e);
		}
	}
	public synchronized void handleEvent(StreamEvent evt) {
		// does not handle events
	}
	public boolean acceptsMoreOutputs() {
		return false;
	}
	public void disconnect() throws IOException {
		connected = false;
		if(fileOutputStream != null)
			fileOutputStream.close();
	}
	public boolean isConnected() {
		return connected;
	}
}
