/*
 * Created on 2005-sep-05
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.subside.fluid;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
/**
 * @author larsr
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public abstract class InputConnector implements Connector, StreamEventListener, StreamEventSource {
	private DelimitedInputStream inputStream;
	private ArrayList listeners;
	public InputConnector() {
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
	public InputStream getInputStream() {
		return inputStream;
	}
	public void setInputStream(InputStream inputStream) {
		this.inputStream = new DelimitedInputStream(inputStream);
	}
	// wrapped stream operations
	public int read() throws IOException {
		return inputStream.read();
	}
	public int read(byte[] b) throws IOException {
		return inputStream.read(b);
	}
	public int read(byte[] b, int off, int len) throws IOException {
		return inputStream.read(b, off, len);
	}
	public int read(byte[] b, int off, int len, int delim) throws IOException, DelimiterNotFoundException {
		return inputStream.read(b, off, len, delim);
	}
	public abstract boolean acceptsMoreInputs();
}
