/*
 * Created on Mar 2, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.subside.fluid;

/**
 * @author Lars Samuelsson
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Frame {
	private byte[] frame;
	private int bitrate;
	private int size;
	public Frame(byte[] frame, int bitrate) {
		this(frame, bitrate, frame.length);
	}
	public Frame(byte[] frame, int bitrate, int size) {
		this.frame = frame;
		this.bitrate = bitrate;
		this.size = size;
	}
	public byte[] getBytes() {
		return frame;
	}
	public int getBitrate() {
		return bitrate;
	}
	public int getSize() {
		return size;
	}
}
