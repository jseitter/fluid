package com.subside.fluid;

public interface ClientHandler {
	public void addClient(Client client);
	public void removeClient(Client client);
	public void handleRequest(Client client, byte[] request);
}
