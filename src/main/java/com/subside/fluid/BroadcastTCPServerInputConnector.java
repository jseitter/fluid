package com.subside.fluid;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Tested with:
 * Nullsoft Shoutcast Source DSP 1.9.0
 * (working)
 * SpacialAudio SAM Encoders
 * (working)
 * jetCast 2.0.4.1109 (Basic Version)
 * (can't get it to connect at all)
 *
 */
public class BroadcastTCPServerInputConnector extends InputConnector implements Runnable {
	private static final String[] accept = new String[] {
		"OK2",
		"icy-caps:11"
	};
	private static final String[] reject = new String[] {
		"invalid password"
	};
	private static ServerSocket serverConnection;
	private Socket sourceSocket;
	private int port;
	private boolean connected;
	private boolean pausing;
	private boolean idle; 
	private int idleTimeout;
	private String password;
	private String pause;
	private Thread engine;
	public BroadcastTCPServerInputConnector() {
		this.connected = false;
		this.pausing = true;
		this.idle = true;
	}
	public void configure(Configuration conf) throws ConfigurationException {
		String portString = conf.getProperty("broadcastTCPServerInputConnector.port");
		if(portString == null)
			throw new ConfigurationException("broadcastTCPServerInputConnector.port not specified");
		try {
			port = Integer.parseInt(portString);
		}
		catch(Exception e) {
			throw new ConfigurationException("broadcastTCPServerInputConnector.port is not a number");
		}
		password = conf.getProperty("broadcastTCPServerInputConnector.password");
		try {
			idleTimeout = Integer.parseInt(conf.getProperty("broadcastTCPServerInputConnector.timeout"));
		}
		catch(Exception e) {
			idleTimeout = 5;
		}
		pause = conf.getProperty("broadcastTCPServerInputConnector.pause");
	}
	public void connect() throws ConnectionException, IOException {
		if(pause != null) {
			try {
				setInputStream(new FileInputStream(pause));
			}
			catch(IOException e) {
				throw new ConnectionException(e);
			}
		}
		if (serverConnection == null || serverConnection.isClosed()) {
			try {
				serverConnection = new ServerSocket(port);
			} 
			catch (BindException e) {
				throw new ConnectionException(e);
			}
		}
		connected = true;
		engine = new Thread(this, getClass().getName() + " Engine");
		engine.start();
	}
	public void acceptServerInput() {
		// new Exception("DEBUG").printStackTrace(System.err);
		while(pausing) {
			try {
				if (serverConnection == null || serverConnection.isClosed()) 
					serverConnection = new ServerSocket(port);
				Fluid.log("Source: Accepting connection on port " + port);
				sourceSocket = serverConnection.accept();
				InputStream sourceInputStream = sourceSocket.getInputStream();
				OutputStream sourceOutputStream = sourceSocket.getOutputStream();
				// give the source a chance to send something
				try {
					Thread.sleep(1000);
				} 
				catch (InterruptedException e) {
					// nah
				}
				byte[] rawInput = new byte[sourceInputStream.available()];
				sourceInputStream.read(rawInput);
				String rawString = new String(rawInput).trim();
				int index = rawString.indexOf("\n");
				String[] greeting = accept;
				if (password != null && (
						index <= 0 || 
						!password.equals(rawString.substring(0, index).trim()))) {
					greeting = reject;
				}
				for (int i = 0; i < greeting.length; i++) {
					sourceOutputStream.write(greeting[i].getBytes());
					sourceOutputStream.write("\r\n".getBytes());
				}
				sourceOutputStream.write("\r\n\r\n".getBytes());
				if (greeting == reject) {
					sourceSocket.close();
				} else {
					// send the (possible) information to the event listeners
					StreamEventListener[] listeners = getStreamEventListeners();
					for(int i = 0; i < listeners.length; i++) {
						listeners[i].handleEvent(new StreamEvent("SourceDetails", rawInput, this));
					}				
					Fluid.log("Source: Accepted input from "
							+ sourceSocket.getInetAddress().getCanonicalHostName());
					setInputStream(sourceInputStream);
					pausing = false;
				}
			}
			catch(IOException e) {
				Fluid.error(e);
			}
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
		if(serverConnection != null)
			serverConnection.close();
		if(sourceSocket != null)
			sourceSocket.close();
		synchronized(this) {
			notifyAll();
		}
	}
	public boolean isConnected() {
		return connected;
	}
	public int read() throws IOException {
		int bytesRead = -1;
		boolean waited = false;
		if(!connected)
			return bytesRead;
		// we are reading from the pause file
		if(pausing && pause != null) {
			bytesRead = super.read();
			// start from the beginning when end is reached
			if(bytesRead == -1) {
				setInputStream(new FileInputStream(pause));
				return read();
			}
		}
		// we are pausing and waiting for server input
		if(pausing && pause == null) {
			waited = true;
			synchronized(this) {
				try {
					wait();
				}
				catch(InterruptedException e) {
					// nah
				}
			}
		}
		// we are reading from the server input
		if(!pausing || waited) {
			try {
				bytesRead = super.read();
			}
			catch(IOException e) {
				// switch to pause mode
			}
			if(bytesRead == -1) {
				pausing = true;
				return read();
			}
		}
		idle = false;
		return bytesRead;
	}
	public int read(byte[] b) throws IOException {
		int bytesRead = -1;
		boolean waited = false;
		if(!connected)
			return bytesRead;
		// we are reading from the pause file
		if(pausing && pause != null) {
			bytesRead = super.read(b);
			// start from the beginning when end is reached
			if(bytesRead == -1) {
				setInputStream(new FileInputStream(pause));
				return read(b);
			}
		}
		// we are pausing and waiting for server input
		if(pausing && pause == null) {
			waited = true;
			synchronized(this) {
				try {
					wait();
				}
				catch(InterruptedException e) {
					// nah
				}
			}
		}
		// we are reading from the server input
		if(!pausing || waited) {
			try {
				bytesRead = super.read(b);
			}
			catch(IOException e) {
				// switch to pause mode
			}
			if(bytesRead == -1) {
				pausing = true;
				return read(b);
			}
		}
		idle = false;
		return bytesRead;
	}
	public int read(byte[] b, int off, int len) throws IOException {
		int bytesRead = -1;
		boolean waited = false;
		if(!connected)
			return bytesRead;
		// we are reading from the pause file
		if(pausing && pause != null) {
			bytesRead = super.read(b, off, len);
			// start from the beginning when end is reached
			if(bytesRead == -1) {
				setInputStream(new FileInputStream(pause));
				return read(b, off, len);
			}
		}
		// we are pausing and waiting for server input
		if(pausing && pause == null) {
			waited = true;
			synchronized(this) {
				try {
					wait();
				}
				catch(InterruptedException e) {
					// nah
				}
			}
		}
		// we are reading from the server input
		if(!pausing || waited) {
			try {
				bytesRead = super.read(b, off, len);
			}
			catch(IOException e) {
				// switch to pause mode
			}
			if(bytesRead == -1) {
				pausing = true;
				return read(b, off, len);
			}
		}
		idle = false;
		return bytesRead;
	}
	public int read(byte[] b, int off, int len, int delim) throws IOException, DelimiterNotFoundException {
		int bytesRead = -1;
		boolean waited = false;
		if(!connected)
			return bytesRead;
		// we are reading from the pause file
		if(pausing && pause != null) {
			bytesRead = super.read(b, off, len, delim);
			// start from the beginning when end is reached
			if(bytesRead == -1) {
				setInputStream(new FileInputStream(pause));
				return read(b, off, len, delim);
			}
		}
		// we are pausing and waiting for server input
		if(pausing && pause == null) {
			waited = true;
			synchronized(this) {
				try {
					wait();
				}
				catch(InterruptedException e) {
					// nah
				}
			}
		}
		// we are reading from the server input
		if(!pausing || waited) {
			try {
				bytesRead = super.read(b, off, len, delim);
			}
			catch(IOException e) {
				// switch to pause mode
			}
			if(bytesRead == -1) {
				pausing = true;
				return read(b, off, len, delim);
			}
		}
		idle = false;
		return bytesRead;
	}
	public void run() {
		int seconds = 0;
		while(connected) {
			if(pausing) {
				seconds = 0;
				acceptServerInput();
				synchronized(this) {
					notifyAll();
				}				
			}
			try {
				Thread.sleep(1000);
			}
			catch(InterruptedException e) {
				// nah
			}
			seconds = (seconds + 1) % idleTimeout;
			if(seconds == 0) {
				if(idle) {
					Fluid.log("Source: Idle timeout (" + idleTimeout + " seconds)");
					if(pause != null) {
						try {
							setInputStream(new FileInputStream(pause));
						}
						catch(IOException e) {
							Fluid.error(e);
							break;
						}
					}
					pausing = true;
					try {
						if(sourceSocket != null)
							sourceSocket.close();
					}
					catch(IOException e) {
						// nah
					}
				}
				else {
					idle = true;
				}
			}
		}
	}
}
