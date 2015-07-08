package com.subside.fluid;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

public class BroadcastTCPOutputConnector extends OutputConnector implements Runnable, ClientHandler {
	private ArrayList clients;
	private static ServerSocket clientConnection;
	private int idleTimeout;
    private int port;
    private int max;
    private String ruleFile;
    private String username;
    private String password;
    private Thread engine;
    public BroadcastTCPOutputConnector() {
    	clients = new ArrayList();
    	engine = new Thread(this, getClass().getName() + " Engine");
    }
    public void addClient(Client client) {
    	synchronized(clients) {
    		clients.add(client);
    	}
    }
    public void removeClient(Client client) {
    	synchronized(clients) {
    		clients.remove(client);
    	}
    }
    public Client[] getClients() {
    	Client[] clientArray = null;
    	synchronized(clients) {
    		clientArray = new Client[clients.size()];
    		clients.toArray(clientArray);
    	}
    	return clientArray;
    }
	public void write(byte[] bytes, int off, int len) {
		Client[] clients = getClients();
		for(int i = 0; i < clients.length; i++) {
			if(clients[i].isGreeted())
				clients[i].write(bytes, off, len);
		}
	}
	public void write(int b) {
		Client[] clients = getClients();
		for(int i = 0; i < clients.length; i++) {
			if(clients[i].isGreeted())
				clients[i].write(b);
		}
	}
	public void write(byte[] bytes) {
		Client[] clients = getClients();
		for(int i = 0; i < clients.length; i++) {
			if(clients[i].isGreeted())
				clients[i].write(bytes);
		}
	}
	public void run() {
		Fluid.log("Target: Accepting connections on port " + port);
		while(isConnected()) {
			try {
				// accept and do as little processing as possible
				if(username != null && password != null) 
					new Client(this, clientConnection.accept(), idleTimeout, max, ruleFile, username, password);
				else
					new Client(this, clientConnection.accept(), idleTimeout, max, ruleFile);
			}
			catch(IOException e) {
				// accept next client
			}
		}
	}
	public void connect() throws ConnectionException {
		if(clientConnection == null || clientConnection.isClosed()) {
			try {
				clientConnection = new ServerSocket(port);
			}
			catch(IOException e) {
				throw new ConnectionException(e);
			}
		}
		engine.start();
	}
	public void configure(Configuration conf) throws ConfigurationException {
		conf.addConfigurable(this);
	    ruleFile = conf.getProperty("broadcastTCPOutputConnector.rule");
	    if(ruleFile == null)
	    	throw new ConfigurationException("broadcastTCPOutputConnector.rule not specified");
		String portString = conf.getProperty("broadcastTCPOutputConnector.port");
		if(portString == null)
			throw new ConfigurationException("broadcastTCPOutputConnector.port not specified");
		try {
			port = Integer.parseInt(portString);
		}
		catch(Exception e) {
			throw new ConfigurationException("broadcastTCPOutputConnector.port is not a number");
		}
		String maxString = conf.getProperty("broadcastTCPOutputConnector.max");
		if(maxString == null)
			throw new ConfigurationException("broadcastTCPOutputConnector.max not specified");
		try {
			max = Integer.parseInt(maxString);
		}
		catch(Exception e) {
			throw new ConfigurationException("broadcastTCPOutputConnector.max is not a number");
		}
		try {
			idleTimeout = Integer.parseInt(conf.getProperty("broadcastTCPOutputConnector.timeout"));
		}
		catch(Exception e) {
			idleTimeout = 10;
		}
	    username = conf.getProperty("broadcastTCPOutputConnector.username");
	    password = conf.getProperty("broadcastTCPOutputConnector.password");
	}
	public synchronized void handleEvent(StreamEvent evt) {
		if("SourceDetails".equals(evt.getEventName())) {
			String details = new String((byte[])evt.getEventObject());
			String[] lines = details.split("\n");
			ArrayList greeting = new ArrayList();
			greeting.add("ICY 200 OK");
			for(int i = 0; i < lines.length; i++) {
				int index;
				if((index = lines[i].indexOf(':')) > 0 &&
						lines[i].length() > index) {
					greeting.add(lines[i]);
				}
			}
			if(greeting.size() > 1) {
				Client.greetingICY = new String[greeting.size()];
				greeting.toArray(Client.greetingICY);
			}
		}
	}
	public boolean acceptsMoreOutputs() {
		return false;
	}
	public void disconnect() throws IOException {
		Client[] clients = getClients();
		for(int i = 0; i < clients.length; i++) {
			try {
				clients[i].disconnect();
			}
			catch(IOException e) {
				// try to close the rest instead
			}
		}
		clientConnection.close();
	}
	public void handleRequest(Client client, byte[] request) {
		StreamEventListener[] listeners = getStreamEventListeners();
		for(int i = 0; i < listeners.length; i++) {
			listeners[i].handleEvent(new StreamEvent("ClientRequest", request, client));
		}
	}
	public boolean isConnected() {
		return !(clientConnection == null || clientConnection.isClosed());
	}
}
