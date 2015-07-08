/*
 * Created on Mar 2, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.subside.fluid;

import java.util.HashMap;
import java.util.ArrayList;

/**
 * @author Lars Samuelsson
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Transceiver {
	private HashMap producers;
	private HashMap consumers;
	public Transceiver() {
		producers = new HashMap();
		consumers = new HashMap();
	}
	public void registerProducer(Thread producer, Transceptable transceptable) {
		synchronized(producers) {
			producers.put(producer, transceptable);
			Fluid.log(producer.getName() + " registered as a producer", 2);
		}
	}
	public void registerConsumer(Thread consumer, Transceptable transceptable) {
		synchronized(consumers) {
			consumers.put(consumer, transceptable);
			Fluid.log(consumer.getName() + " registered as a consumer", 2);
		}
	}
	public void unregisterProducer(Thread producer) {
		synchronized(producers) {
			producers.remove(producer);
			Fluid.log(producer.getName() + " unregistered as a producer", 2);
		}
	}
	public void unregisterConsumer(Thread consumer) {
		synchronized(consumers) {
			consumers.remove(consumer);
			Fluid.log(consumer.getName() + " unregistered as a consumer", 2);
		}
	}
	public boolean isProducing() {
		synchronized(producers) {
			return !producers.isEmpty();
		}
	}
	public boolean isConsuming() {
		synchronized(consumers) {
			return !consumers.isEmpty();
		}
	}
	public void consumerError(Exception e) {
		Object[] threads;
		synchronized(producers) {
			threads = new ArrayList(producers.keySet()).toArray();
		}
		Transceptable transceptable;
		for(int i = 0; i < threads.length; i++) {
			transceptable = (Transceptable)producers.get(threads[i]);
			if(transceptable != null) 
				transceptable.intercept(e);
		}
	}
	public void producerError(Exception e) {
		Object[] threads;
		synchronized(consumers) {
			threads = new ArrayList(consumers.keySet()).toArray();
		}
		Transceptable transceptable;
		for(int i = 0; i < threads.length; i++) {
			transceptable = (Transceptable)consumers.get(threads[i]);
			if(transceptable != null) 
				transceptable.intercept(e);
		}
	}
	public void waitForProducers() {
		Object[] threads;
		synchronized(producers) {
			threads = new ArrayList(producers.keySet()).toArray();
		}
		for(int i = 0; i < threads.length; i++) {
			Thread thread = (Thread)threads[i];
			if(thread != null && thread.isAlive()) {
				try {
					thread.join();
				}
				catch(InterruptedException e) {
					break;
				}
			}
		}
	}
	public void waitForConsumers() {
		Object[] threads;
		synchronized(consumers) {
			threads = new ArrayList(consumers.keySet()).toArray();
		}
		for(int i = 0; i < threads.length; i++) {
			Thread thread = (Thread)threads[i];
			if(thread != null && thread.isAlive()) {
				try {
					thread.join();
				}
				catch(InterruptedException e) {
					break;
				}
			}
		}
	}
}

