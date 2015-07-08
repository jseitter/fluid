/*
 * Created on 2005-jan-11
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.subside.fluid;

import java.io.IOException;

/**
 * @author Lars
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TransparentFrameWriter extends FrameWriter {
	public void writeFrame(Frame frame) throws IOException {
		outputConnector.write(frame.getBytes(), 0, frame.getSize());
	}
}
