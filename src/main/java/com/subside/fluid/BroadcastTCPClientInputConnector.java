package com.subside.fluid;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class BroadcastTCPClientInputConnector extends InputConnector implements Runnable {
	private String server;
	private int port;
	private String filename;
	private String username;
	private String password;
	private int interval;
	private Socket source;
	private boolean connected;
	private boolean idle;
	private int idleTimeout;
	private Thread engine;
	public BroadcastTCPClientInputConnector() {
		this.connected = false;
		this.idle = true;
	}
	public boolean acceptsMoreInputs() {
		return false;
	}
	public void connect() throws ConnectionException {
		while (!connected) {
			try {
				source = new Socket(server, port);
				OutputStream sourceOutputStream = source.getOutputStream();
				if (filename == null)
					filename = "/";
				if (!filename.startsWith("/"))
					filename = "/" + filename;
				String request = "GET " + filename + " HTTP/1.0\r\n" + "Host: "
						+ source.getLocalAddress() + "\r\n" + "User-Agent: "
						+ Fluid.version + "\r\n" + "Accept: */*\r\n"
						+ "Connection: close\r\n";
				if (username != null && password != null)
					request = request + "Authorization: Basic "
							+ Base64Encoder.encode(username + ":" + password)
							+ "\r\n";
				sourceOutputStream.write(request.getBytes());
				sourceOutputStream.write("\r\n\r\n".getBytes());
				sourceOutputStream.flush();
				InputStream sourceInputStream = source.getInputStream();
				setInputStream(sourceInputStream);
				// give the source a chance to send something
				try {
					Thread.sleep(1000);
				}
				catch(InterruptedException e) {
					// nah
				}
				if(sourceInputStream.available() > 0) {
					byte[] response = new byte[sourceInputStream.available()];
					sourceInputStream.read(response);
					String responseString = new String(response);
					if(responseString.indexOf(" 200 ") < 0)
						throw new IOException('\n' + responseString.trim());
				}
				connected = true;
				engine = new Thread(this, getClass().getName() + " Engine");
				engine.start();
			} 
			catch (UnknownHostException e) {
				connected = false;
				throw new ConnectionException(e);
			}
			catch (IOException e) {
				connected = false;
				Fluid.error(e);
				Fluid.log(
						"Source: Could not connect to http://" +
						server + ":" + port + filename + "(retrying in " + interval + " seconds)" 
						);
				try {
					Thread.sleep(interval * 1000);
				}
				catch(InterruptedException ie) {
					// nah
				}
			}
		}
		Fluid.log(
				"Source: Connected to http://" +
				server + ":" + port + filename
				);
	}
	public void disconnect() throws IOException {
		connected = false;
		source.close();
	}
	public boolean isConnected() {
		return connected;
	}
	public void configure(Configuration conf) throws ConfigurationException {
		server = conf.getProperty("broadcastTCPClientInputConnector.server");
		if(server == null)
			throw new ConfigurationException("broadcastTCPClientInputConnector.server not specified");
		String portString = conf.getProperty("broadcastTCPClientInputConnector.port");
		if(portString == null)
			throw new ConfigurationException("broadcastTCPClientInputConnector.port not specified");
		try {
			port = Integer.parseInt(portString);
		}
		catch(Exception e) {
			throw new ConfigurationException("broadcastTCPClientInputConnector.port is not a number");
		}
		filename = conf.getProperty("broadcastTCPClientInputConnector.filename");
		username = conf.getProperty("broadcastTCPClientInputConnector.username");		
		password = conf.getProperty("broadcastTCPClientInputConnector.password");		
		try {
			interval = Integer.parseInt(conf.getProperty("broadcastTCPClientInputConnector.interval"));
		}
		catch(Exception e) {
			interval = 30;
		}
		try {
			idleTimeout = Integer.parseInt(conf.getProperty("broadcastTCPClientInputConnector.timeout"));
		}
		catch(Exception e) {
			idleTimeout = 30;
		}
	}
	public void handleEvent(StreamEvent evt) {
		// nope
	}	
	public int read() throws IOException {
		int bytesRead = -1;
		if(!connected)
			return bytesRead;
		try {
			bytesRead = super.read();
		}
		catch(IOException e) {
			// skip to next
		}
		if(bytesRead == -1) {
			try {
				connected = false;
				connect();
			}
			catch(ConnectionException e) {
				return bytesRead;
			}
			return read();
		}
		idle = false;
		return bytesRead;
	}
	public int read(byte[] b) throws IOException {
		int bytesRead = -1;
		if(!connected)
			return bytesRead;
		try {
			bytesRead = super.read(b);
		}
		catch(IOException e) {
			// skip to next
		}
		if(bytesRead == -1) {
			try {
				connected = false;
				connect();
			}
			catch(ConnectionException e) {
				return bytesRead;
			}
			return read(b);
		}
		idle = false;
		return bytesRead;
	}
	public int read(byte[] b, int off, int len) throws IOException {
		int bytesRead = -1;
		if(!connected)
			return bytesRead;
		try {
			bytesRead = super.read(b, off, len);
		}
		catch(IOException e) {
			// skip to next
		}
		if(bytesRead == -1) {
			try {
				connected = false;
				connect();
			}
			catch(ConnectionException e) {
				return bytesRead;
			}
			return read(b, off, len);
		}
		idle = false;
		return bytesRead;
	}
	public int read(byte[] b, int off, int len, int delim) throws IOException, DelimiterNotFoundException {
		int bytesRead = -1;
		if(!connected)
			return bytesRead;
		try {
			bytesRead = super.read(b, off, len, delim);
		}
		catch(IOException e) {
			// skip to next
		}
		if(bytesRead == -1) {
			try {
				connected = false;
				connect();
			}
			catch(ConnectionException e) {
				return bytesRead;
			}
			return read(b, off, len, delim);
		}
		idle = false;
		return bytesRead;
	}
	public void run() {
		int seconds = 0;
		while(connected) {
			try {
				Thread.sleep(1000);
			}
			catch(InterruptedException e) {
				// nah
			}
			seconds = (seconds + 1) % idleTimeout;
			if(seconds == 0) {
				if(idle) {
					try {
						disconnect();
					}
					catch(IOException e) {
						connected = false;
					}
				}
				else {
					idle = true;
				}
			}
		}
	}
}
