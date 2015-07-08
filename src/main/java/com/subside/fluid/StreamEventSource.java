package com.subside.fluid;

public interface StreamEventSource {
	public void addStreamEventListener(StreamEventListener listener);
	public StreamEventListener[] getStreamEventListeners();
}
