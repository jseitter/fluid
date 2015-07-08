package com.subside.fluid;

public class StreamEvent {
	private String eventName;
	private Object eventObject;
	private Object eventSource;
	public StreamEvent(String eventName, Object eventObject, Object eventSource) {
		this.eventName = eventName;
		this.eventObject = eventObject;
		this.eventSource = eventSource;
	}
	public String getEventName() {
		return eventName;
	}
	public Object getEventObject() {
		return eventObject;
	}
	public Object getEventSource() {
		return eventSource;
	}
}
