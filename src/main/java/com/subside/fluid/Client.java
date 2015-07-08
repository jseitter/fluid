package com.subside.fluid;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client implements Runnable {
	public static String[] greetingHTTP = new String[] {
		"HTTP/1.0 200 OK",
		"Server: " + Fluid.version,
		"Content-Type: fluid/media",
		"Cache-Control: no-cache",
		"Pragma: no-cache",
		"Connection: close"
	};
	public static String[] greetingICY = new String[] {
		"ICY 200 OK",
		"icy-notice1:<BR>This stream requires <a href=\"http://www.winamp.com/\">Winamp</a><BR>",
		"icy-notice2:" + Fluid.version + "<BR>",
		"icy-name:Fluid Streaming Server",
		"icy-genre:various",
		"icy-url:http://www.subside.com",
		"icy-pub:1"
	};
	public static String[] rejectFull = new String[] {
		"HTTP/1.0 503 Service Unavailable",
		"Server: " + Fluid.version,
		"Content-Length: 0",
		"Connection: close",
		"Content-Type: text/html"
	};
	public static String[] requestAuthorization = new String[] {
		"HTTP/1.0 401 Authorization Required", 
		"Server: " + Fluid.version,
		"WWW-Authenticate: Basic realm=\"Private Streaming Experience\"",
		"Content-Length: 0",
		"Connection: close",
		"Content-Type: text/html"
	};
	public static int numberOfConnections = 0;
	public static int numberOfClients = 0;
	private int maxNumberOfClients;
	private Thread engine;
    private Socket clientSocket;
    private OutputStream outputStream;
    private ClientHandler handler;
    private String username;
    private String password;
    private String hostname;
    private long connectionTime;
    private long bytesSent;
	private long contentLength;
    private boolean greeted;
    private boolean connected;
	private boolean idle; 
	private int idleTimeout;
    public Client(ClientHandler handler, Socket clientSocket, int idleTimeout, int max, String ruleFile) throws IOException {
    	this(handler, clientSocket, idleTimeout, max, ruleFile, null, null);
    }
    public Client(ClientHandler handler, Socket clientSocket, int idleTimeout, int max, String ruleFile, String username, String password) throws IOException {
    	Client.numberOfConnections++;
    	if(ClientValidator.isAllowed(clientSocket.getInetAddress(), ruleFile)) {
        	this.handler = handler;
        	this.clientSocket = clientSocket;
        	this.outputStream = clientSocket.getOutputStream();
        	this.username = username;
        	this.password = password;
        	this.greeted = false;
        	this.connected = false;
        	this.connectionTime = System.currentTimeMillis();
        	this.bytesSent = 0;
        	this.contentLength = -1;
        	this.maxNumberOfClients = max;
    		this.idle = true;
    		this.idleTimeout = idleTimeout;
        	this.hostname = clientSocket.getInetAddress().getCanonicalHostName();
        	this.engine = new Thread(this, "Client Engine");
    		engine.start();
    	}
    	else {
    		clientSocket.close();
    	}
    }
    public void disconnect() throws IOException {
    	if(!clientSocket.isClosed() && bytesSent < contentLength)
    		write(new byte[(int)(contentLength - bytesSent)]);
    	connected = false;
    	clientSocket.close();
    }
	public void write(byte[] bytes, int off, int len) {
		idle = false;
		try {
			outputStream.write(bytes, off, len);
			outputStream.flush();
			bytesSent += bytes.length;
		}
		catch(IOException e) {
			Fluid.error(e, 2);
			connected = false;
		}
	}
	public void write(int b) {
		idle = false;
		try {
			outputStream.write(b);
			outputStream.flush();
			bytesSent += 1;
		}
		catch(IOException e) {
			Fluid.error(e, 2);
			connected = false;
		}
	}	
	public void write(byte[] bytes) {
		idle = false;
		try {
			outputStream.write(bytes);
			outputStream.flush();
			bytesSent += bytes.length;
		}
		catch(IOException e) {
			Fluid.error(e, 2);
			connected = false;
		}
	}
	public OutputStream getOutputStream() {
		return outputStream;
	}
	public void setContentLength(int contentLength) {
		this.contentLength = contentLength;
	}
	public boolean isConnected() {
		return connected;
	}
	public boolean isGreeted() {
		return connected & greeted;
	}
    public void run() {
		handler.addClient(this);
		connected = true;
		int numberOfRetries = 0;
		int seconds = 0;
		InputStream inputStream;
   		try {
   	    	while(connected && !clientSocket.isClosed()) {
    			inputStream = clientSocket.getInputStream();
    			// give the target a chance to send something
    			try {
    				Thread.sleep(500);
    			}
    			catch(InterruptedException e) {
    				// nah
    			}
    			if(inputStream.available() > 0) {
    				byte[] request = new byte[inputStream.available()];
    				inputStream.read(request);
    				String requestString = new String(request);
    				Fluid.log("Request:\n" + requestString, 1);
    				// authorization needed
    				if(username != null && password != null) {
    					int index = 0;
    					boolean authorized = false;
    					if((index = requestString.indexOf("Authorization: Basic")) >= 0) {
    						String base64 = requestString.substring(index + 21).trim();
    						if((index = base64.indexOf(" ")) > 0)
    							base64 = base64.substring(0, index);
    						if((index = base64.indexOf("\r")) > 0)
    							base64 = base64.substring(0, index);
    						if((index = base64.indexOf("\n")) > 0)
    							base64 = base64.substring(0, index);
    						if(base64.equals(Base64Encoder.encode(username + ":" + password)))
    							authorized = true;
    					}
    					if(!authorized) {
    						for(int i = 0; i < requestAuthorization.length; i++) {
    							outputStream.write(requestAuthorization[i].getBytes());
    							outputStream.write("\r\n".getBytes());
    						}
							outputStream.write("\r\n\r\n".getBytes());
    						connected = false;
    						break;
    					}
    				}
    				// pulling stats
    				if(requestString.indexOf("stats.xml") > 0) {
    					String maxListeners = "";
    					if(maxNumberOfClients > 0)
    						maxListeners = "<limit>" + maxNumberOfClients + "</limit>";
    					String response = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\r\n" +
    					"<icestats>" +
    					"<client_connections>" + Client.numberOfConnections + "</client_connections>" +
    					"<server>" + Fluid.version + "</server>" + 
    					"<server_start>" + Fluid.startingTime + "</server_start>" +
    					"<sources>1</sources>" +
    					"<source mount=\"/live\">" +
    					"<listeners>" + Client.numberOfClients + "</listeners>" +
    					maxListeners +
    					"<public>0</public>" +
    					"</source>" +
    					"</icestats>\r\n\r\n";
    					String responseHeader = "HTTP/1.0 200 OK\r\n" + 
    					"Content-Type: text/xml\r\n" +
    					"Content-Length: " + response.length() + "\r\n\r\n";
						outputStream.write(responseHeader.getBytes());
						outputStream.write(response.getBytes("ISO-8859-1"));
    					connected = false;
    					break;
    				}
    				if(maxNumberOfClients > 0 && numberOfClients >= maxNumberOfClients) {
						for(int i = 0; i < rejectFull.length; i++) {
							outputStream.write(rejectFull[i].getBytes());
							outputStream.write("\r\n".getBytes());
						}
						outputStream.write("\r\n\r\n".getBytes());
						connected = false;
						break;
    				}
    				// everything is ok, let the handler do its stuff
    				handler.handleRequest(this, request);
    				// then welcome the client
    				if(!greeted) {
    					String agent = "Unknown client";
    					int index = 0;
    					if((index = requestString.indexOf("User-Agent:")) >= 0) {
    						agent = requestString.substring(index + 11).trim();
    						if((index = agent.indexOf("\r")) > 0)
    							agent = agent.substring(0, index);
    						if((index = agent.indexOf("\n")) > 0)
    							agent = agent.substring(0, index);    						
    					}
    					Fluid.log(
    							"Target: " + this + 
    							" (" + agent + ") connected"
    							);
    					String[] greeting;
    					if(requestString.indexOf("Icy-MetaData:1") >= 0)
    						greeting = greetingICY;
    					else
    						greeting = greetingHTTP;
    					for(int i = 0; i < greeting.length; i++) {
        					Fluid.log("Greeting: " + greeting[i], 2);
    						outputStream.write(greeting[i].getBytes());
    						outputStream.write("\r\n".getBytes());
    					}
    					outputStream.write(("Content-Length: " + contentLength).getBytes());
    					outputStream.write("\r\n\r\n".getBytes());
    					Fluid.log("Setting greeted to true (real greeting)", 2);
						Client.numberOfClients++;
						greeted = true;
    				}
    			}
    			// assume client does not need greeting if it stays silent after two retries
    			if(!greeted) {
    				if(++numberOfRetries > 1) {
        				Client.numberOfClients++;
        				greeted = true;
    				}
    			}
    			// check if idle
    			seconds = (seconds + 1) % (2 * idleTimeout);
    			if(seconds == 0) {
    				if(idle) {
    					connected = false;
    					Fluid.log(
    							"Target: " + this + 
    							" idle timeout (" + idleTimeout + " seconds)"
    							);
    					break;
    				}
    				else {
    					idle = true;
    				}    				
    			}
    		}
			clientSocket.close();
   		}
   	    catch(IOException e) {
    		connected = false;
    	}
   	    if(greeted) {
   	   	    Client.numberOfClients--;
   	    	// the 1 avoids division by zero
   	    	long millis = 1 + System.currentTimeMillis() - connectionTime; 
   	    	Fluid.log(
   					"Target: " + this 
   					+ " (" + bytesSent + " B over "
   					+ (long) (millis / 1000 / 3600) + "."
   					+ (long) (millis / 1000 / 60) % 60 + ":"
   					+ (long) (millis / 1000) % 60 % 60 + " = "
   					+ (long) (bytesSent * 8 * 1000 / millis) + " bps)"
   					+ " disconnected"
				);
   	    }
   	    handler.removeClient(this);
    }
    public void handleException(Exception e) {
    	try {
    		outputStream.write("HTTP/1.0 404 Not Found\r\n".getBytes());
    		outputStream.write("Content-type: text/plain\r\n".getBytes());
    		outputStream.write(("Content-Length: " + e.getMessage().length() + "\r\n\r\n").getBytes());
    		outputStream.write(e.getMessage().getBytes());    	
    	}
    	catch(IOException ioe) {
    		// client already disconnected
    		connected = false;
    	}
    }
    public String toString() {
    	String instance = super.toString();
    	instance = instance.substring(instance.indexOf("@") + 1);
		return instance + "@" + hostname;
	}
}
