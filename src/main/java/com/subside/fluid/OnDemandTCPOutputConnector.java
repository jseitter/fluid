package com.subside.fluid;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;

public class OnDemandTCPOutputConnector extends OutputConnector implements ClientHandler {
	private Client client;
	private static ServerSocket clientConnection;
	private int idleTimeout;
    private int port;
    private int max;
    private String ruleFile;
    private String username;
    private String password;
    private boolean hasRequested;
    private boolean hasResponded;
    public OnDemandTCPOutputConnector() {
    	hasRequested = false;
    	hasResponded = false;
    }
    public void addClient(Client client) {
    	this.client = client;
    }
    public void removeClient(Client client) {
    	this.client = null;
    }
	public void write(byte[] bytes, int off, int len) throws IOException {
		if(client != null) {
			if(client.isGreeted())
				client.write(bytes, off, len);
		}
		else
			throw new IOException("Client has disconnected");
	}
	public void write(int b) throws IOException {
		if(client != null) {
			if(client.isGreeted())
				client.write(b);
		}
		else
			throw new IOException("Client has disconnected");
	}
	public void write(byte[] bytes) throws IOException {
		if(client != null) {
			if(client.isGreeted())
				client.write(bytes);
		}
		else
			throw new IOException("Client has disconnected");
	}
	public void connect() throws ConnectionException, IOException {
		if(clientConnection == null || clientConnection.isClosed()) {
			try {
				clientConnection = new ServerSocket(port);
				Fluid.log("Target: Accepting connections on port " + port);
			}
			catch(BindException e) {
				throw new ConnectionException(e);
			}
		}
		if(username != null && password != null) 
			new Client(this, clientConnection.accept(), idleTimeout, max, ruleFile, username, password);
		else
			new Client(this, clientConnection.accept(), idleTimeout, max, ruleFile);
		try {
			synchronized(this) {
				// give it at most one second
				wait(1000);
			}
		} 
		catch(InterruptedException e) {
			// nada
		}
		if(!hasRequested) {
			throw new IOException("No request was made");
		}
	}
	public void configure(Configuration conf) throws ConfigurationException {
		conf.addConfigurable(this);
	    ruleFile = conf.getProperty("onDemandTCPOutputConnector.rule");
	    if(ruleFile == null)
	    	throw new ConfigurationException("onDemandTCPOutputConnector.rule not specified");
		String portString = conf.getProperty("onDemandTCPOutputConnector.port");
		if(portString == null)
			throw new ConfigurationException("onDemandTCPOutputConnector.port not specified");
		try {
			port = Integer.parseInt(portString);
		}
		catch(Exception e) {
			throw new ConfigurationException("onDemandTCPOutputConnector.port is not a number");
		}
		String maxString = conf.getProperty("onDemandTCPOutputConnector.max");
		if(maxString == null)
			throw new ConfigurationException("onDemandTCPOutputConnector.max not specified");
		try {
			max = Integer.parseInt(maxString);
		}
		catch(Exception e) {
			throw new ConfigurationException("onDemandTCPOutputConnector.max is not a number");
		}
		try {
			idleTimeout = Integer.parseInt(conf.getProperty("onDemandTCPOutputConnector.timeout"));
		}
		catch(Exception e) {
			idleTimeout = 10;
		}
	    username = conf.getProperty("onDemandTCPOutputConnector.username");
	    password = conf.getProperty("onDemandTCPOutputConnector.password");
	}
	public synchronized void handleEvent(StreamEvent evt) {
		if("SourceLength".equals(evt.getEventName())) {
			Integer length = (Integer)evt.getEventObject();
			client.setContentLength(length.intValue());
		}
		if("SourceException".equals(evt.getEventName())) {
			client.handleException((Exception)evt.getEventObject());
			disconnect();
		}
		synchronized(this) {
			hasResponded = true;
			notifyAll();
		}
	}
	public boolean acceptsMoreOutputs() {
		return true;
	}
	public void disconnect() {
		try {
			if(client != null)
				client.disconnect();
		}
		catch(IOException e) {
			// it was closed already
		}
	}
	public void handleRequest(Client client, byte[] request) {
		StreamEventListener[] listeners = getStreamEventListeners();
		for (int i = 0; i < listeners.length; i++) {
			listeners[i].handleEvent(new StreamEvent("ClientRequest", request, client));
		}
		synchronized (this) {
			hasRequested = true;
			notifyAll();
			try {
				synchronized(this) {
					// give it at most one second
					wait(1000);
				}
			} 
			catch(InterruptedException e) {
				// nada
			}
		}
		if(!hasResponded)
			Fluid.error(getClass().getName() + ": No response from the InputConnector");
	}
	public boolean isConnected() {
		return client != null && client.isConnected();
	}
}
