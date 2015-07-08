package com.subside.fluid;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class BroadcastPlaylistInputConnector extends InputConnector {
	private ArrayList filenames;
	private int current;
	private String playlist;
	private boolean connected;
	private boolean random;
	public BroadcastPlaylistInputConnector() {
		filenames = null;
		connected = false;
		current = -1;
		filenames = new ArrayList();
	}
	public void configure(Configuration conf) throws ConfigurationException {
		playlist = conf.getProperty("broadcastPlaylistInputConnector.playlist");
		if(playlist == null)
			throw new ConfigurationException("broadcastPlaylistInputConnector.playlist not specified");
		random = "true".equals(conf.getProperty("broadcastPlaylistInputConnector.random"));
	}
	public void connect() throws ConnectionException, IOException {
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(playlist)));
		}
		catch(IOException e) {
			throw new ConnectionException(e);
		}
		String filename;
		while((filename = bufferedReader.readLine()) != null) {
			filename = filename.trim();
			// ignore comments
			if(!filename.startsWith("#")) {
				filenames.add(filename);
			}				
		}
		if(filenames.isEmpty())
			throw new ConnectionException("The playlist is empty");
		boolean noFileFound = true;
		while(noFileFound) {
			try {
				switchInputStream();
				noFileFound = false;
			}
			catch(IOException e) {
				Fluid.error(e);
			}
		}
		if(filenames.isEmpty())
			throw new ConnectionException("None of the files in the playlist could be found");
		connected = true;
	}
	private void switchInputStream() throws IOException {
		if(getInputStream() != null)
			getInputStream().close();
		if(filenames.isEmpty())
			throw new IOException("All files in the playlist were marked as bad");
		if(random)
			current = (int)(Math.random() * filenames.size());
		else
			current = (current + 1) % filenames.size();
		try {
			Fluid.log("Playing: " + filenames.get(current), 1);
			setInputStream(new FileInputStream((String)filenames.get(current)));
		}
		catch(IOException e) {
			filenames.remove(current);
			throw e;
		}
	}
	public int read() throws IOException {
		int bytesRead = -1;
		if(filenames.isEmpty())
			return bytesRead;
		try {
			bytesRead = super.read();
		}
		catch(IOException e) {
			// skip to next
		}
		if(bytesRead == -1) {
			switchInputStream();
			return read();
		}
		return bytesRead;
	}
	public int read(byte[] b) throws IOException {
		int bytesRead = -1;
		if(filenames.isEmpty())
			return bytesRead;
		try {
			bytesRead = super.read(b);
		}
		catch(IOException e) {
			// skip to next
		}
		if(bytesRead == -1) {
			switchInputStream();
			return read(b);
		}
		return bytesRead;
	}
	public int read(byte[] b, int off, int len) throws IOException {
		int bytesRead = -1;
		if(filenames.isEmpty())
			return bytesRead;
		try {
			bytesRead = super.read(b, off, len);
		}
		catch(IOException e) {
			// skip to next
		}
		if(bytesRead == -1) {
			switchInputStream();
			return read(b, off, len);
		}
		return bytesRead;
	}
	public int read(byte[] b, int off, int len, int delim) throws IOException, DelimiterNotFoundException {
		int bytesRead = -1;
		if(filenames.isEmpty())
			return bytesRead;
		try {
			bytesRead = super.read(b, off, len, delim);
		}
		catch(IOException e) {
			// skip to next
		}
		if(bytesRead == -1) {
			switchInputStream();
			return read(b, off, len, delim);
		}
		return bytesRead;
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
		if(getInputStream() != null)
			getInputStream().close();
	}
	public boolean isConnected() {
		return connected;
	}
}
