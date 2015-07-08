/*
 * Created on Jan 22, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.subside.fluid;

/**
 * @author larsr
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class DelimiterNotFoundException extends Exception {
	private static final long serialVersionUID = 1L;
	public DelimiterNotFoundException(String message) {
		super(message);
	}
	public DelimiterNotFoundException(Exception e) {
		super(e);
	}
}
