package com.subside.fluid;

// for serious exceptions
public class ConnectionException extends Exception {
	private static final long serialVersionUID = 1L;
	public ConnectionException(String message) {
		super(message);
	}
	public ConnectionException(Exception e) {
		super(e);
	}
}
