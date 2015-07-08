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
public class Timer {
	private double accumulatedTime;
	private double synchronizationTime;
	private int frames;
	private int framesBetweenSync;
	private int frameBufferSize;
	private char mode;
	private double saturator;
	private double delay;
	public Timer(int framesBetweenSync, int frameBufferSize) {
		this.framesBetweenSync = framesBetweenSync;
		this.frameBufferSize = frameBufferSize;
		this.frames = 0;
		this.saturator = 0.0f;
		this.mode = 'B';
	}
	// the buffer is between 1/4 and 3/4 full
	public void enterBalancedMode() {
		if(mode != 'B') {
			Fluid.log("Switching from mode '" + mode + "' to 'B'", 3);
			mode = 'B';
		}
	}
	// the buffer is less than 1/4 full
	public void enterStarvingMode() {
		if(mode != 'E') {
			Fluid.log("Switching from mode '" + mode + "' to 'E'", 3);
			mode = 'E';
		}
	}
	// the buffer is more than 3/4 full
	public void enterSatiatedMode() {
		if(mode != 'F') {
			Fluid.log("Switching from mode '" + mode + "' to 'F'", 3);
			mode = 'F';
		}
	}
	public long getDelay(Frame frame) {
        if(frame == null)
            throw new IllegalArgumentException("Null frame given as argument");
		frames = (frames + 1) % framesBetweenSync;
		if(frames == 1) {
			accumulatedTime = System.currentTimeMillis();
			synchronizationTime = accumulatedTime;
		}
		accumulatedTime += frame.getSize() * 8 / frame.getBitrate();
		switch(mode) {
		case 'B':
			saturator = 0.0f;
			break;
		case 'E':
			if(frames != 0)
				saturator = +(frameBufferSize / 8) * (accumulatedTime - synchronizationTime) / frames;
			break;
		case 'F':
			if(frames != 0)
				saturator = -(frameBufferSize / 8) * (accumulatedTime - synchronizationTime) / frames;
			break;
		}
		delay = accumulatedTime - System.currentTimeMillis() + saturator;
		if(delay > 0)
			return (long)delay;
		return 0;
	}
}
