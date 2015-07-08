package com.subside.fluid;

import java.io.FileInputStream;
import java.io.IOException;

public class OnDemandFileInputConnector extends InputConnector {
	private String filename;
	private FileInputStream fileInputStream;
	private boolean connected;
	private String path;
	public OnDemandFileInputConnector() {
		connected = false;
	}
	public void configure(Configuration conf) throws ConfigurationException {
		path = conf.getProperty("onDemandFileInputConnector.path");
	}
	public void connect() throws IOException {
		StreamEventListener[] listeners = getStreamEventListeners();
		try {
			if(filename == null || "".equals(filename))
				throw new IOException("No filename was given");
			if(filename.indexOf("..") >= 0)
				throw new IOException("Relative paths are not allowed");
			if(path != null) {
				if(!path.endsWith("/") && !"".equals(path))
					path = path + "/";
				filename = path + filename;
			}
			setInputStream(fileInputStream = new FileInputStream(filename));
			Integer length = new Integer(fileInputStream.available());
			for(int i = 0; i < listeners.length; i++) {
				listeners[i].handleEvent(new StreamEvent("SourceLength", length, this));
			}
		}
		catch(IOException e) {
			for(int i = 0; i < listeners.length; i++) {
				listeners[i].handleEvent(new StreamEvent("SourceException", e, this));
			}			
			throw e;
		}
		connected = true;
	}
	public synchronized void handleEvent(StreamEvent evt) {
		// find what the client wants to hear
		if("ClientRequest".equals(evt.getEventName())) {
			String request = new String((byte[])evt.getEventObject());
			Fluid.log(getClass().getName() + ": " + request, 2);
			if(request.indexOf("GET") >= 0) {
				filename = request.substring(request.indexOf(" ", request.indexOf("GET")) + 1);
				filename = filename.substring(0, filename.indexOf(" ")).trim();
				if(filename.startsWith("/"))
					filename = filename.substring(1);
			}
		}
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

