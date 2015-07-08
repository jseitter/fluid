/*
 * Created on Mar 2, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.subside.fluid;

/**
 * @author Lars Rönnbäck
 *
 * This buffer uses the results from "Formal Verification of a 
 * Bounded Buffer with Three Separate Concerns" by Torsten Nelson,
 * Donald Cowan and Paulo Alencar, University of Waterloo, International 
 * Conference on Software Engineering - ICSE, Toronto, Canada, May 2001.
 * <P> 
 * Since we are starting with an empty buffer the following order of 
 * policies should be handled to avoid deadlocks: 
 * {FullPolicy followed-by EmptyPolicy followed-by FIFOPolicy}
 */
public class FrameBuffer {
	private final Frame[] buffer;
	private final int size;
	private int first, last;
	private int starvingLimit, satiatedLimit;
	public FrameBuffer(int size) {
		if(size < 4)
			size = 4;
		this.buffer = new Frame[size];
		this.size = size;
		this.first = 0;
		this.last = 0;
		this.starvingLimit = (int)(0.25 * size);
		this.satiatedLimit = (int)(0.75 * size);
	}
	public synchronized void enbuf(Frame frame) throws InterruptedException {
		// full policy
		int nextposition = (last + 1) % size;
		while (nextposition == first) {
			wait();
			nextposition = (last + 1) % size;
		}
		// empty policy
		notify();
		// FIFO policy
		last = nextposition;
		buffer[last] = frame;
	}
	public synchronized Frame debuf() throws InterruptedException {
		// full policy
		notify();
		// empty policy
		while (first == last) {
			wait();
		}
		// FIFO policy
		Frame frame = buffer[first];
		first = (first + 1) % size;
		return frame;
	}
	public synchronized boolean isEmpty() {
		return first == last;
	}
	public synchronized boolean isFull() {
		return size() == size;
	}
	public synchronized boolean isBalanced() {
		return size() > starvingLimit & size() < satiatedLimit;
	}
	public synchronized boolean isStarving() {
		return size() <= starvingLimit;
	}
	public synchronized boolean isSatiated() {
		return size() >= satiatedLimit;
	}
	public synchronized int size() {
		return (last - first + size) % size;
	}
	public synchronized int capacity() {
		return size;
	}
}
