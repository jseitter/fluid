/*
 * Created on Dec 31, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.subside.fluid;

/**
 * @author lars
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ConfigurationException extends Exception {
	private static final long serialVersionUID = 1L;
	public ConfigurationException(String message) {
		super(message);
	}
	public ConfigurationException(Exception e) {
		super(e);
	}
}
