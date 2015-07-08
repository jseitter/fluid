package com.subside.fluid;

import java.io.IOException;

public interface Connector extends Configurable {
	// throwing ConnectionException will cause the server to die
	// throwing IOException will cause the server to restart
	public void connect() throws ConnectionException, IOException;
	public void disconnect() throws ConnectionException, IOException;
	public boolean isConnected();
}
