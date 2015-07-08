package com.subside.fluid;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public abstract class OutputConnector implements Connector, StreamEventListener, StreamEventSource {
	private OutputStream outputStream;
	private ArrayList listeners;
	public OutputConnector() {
		listeners = new ArrayList();
	}
	public void addStreamEventListener(StreamEventListener listener) {
		listeners.add(listener);
	}
	public StreamEventListener[] getStreamEventListeners() {
    	StreamEventListener[] listenerArray = new StreamEventListener[listeners.size()];
    	listeners.toArray(listenerArray);
    	return listenerArray;
	}
	public OutputStream getOutputStream() {
		return outputStream;
	}
	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}
	// wrapped stream operations
	public void write(byte[] bytes) throws IOException {
		if(outputStream != null) {
			outputStream.write(bytes);
			outputStream.flush();
		}
	}
	public void write(byte[] bytes, int off, int len) throws IOException {
		if(outputStream != null) {
			outputStream.write(bytes, off, len);
			outputStream.flush();
		}
	}
	public void write(int b) throws IOException {
		if(outputStream != null) {
			outputStream.write(b);
			outputStream.flush();
		}
	}
	public abstract boolean acceptsMoreOutputs();
}