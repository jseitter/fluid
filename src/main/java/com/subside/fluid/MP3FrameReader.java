/*
 * Created on Mar 2, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.subside.fluid;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author Lars Rönnbäck
 *
 * A helper class for reading individual frames from an
 * MP3 or MP3Pro input stream.
 * <P>
 * For each frame the header is first read to determine
 * the framesize. Other information
 * such as the type, layer, bitrate and sampling frequency
 * is extracted in the process as well.
 * <P>
 * http://www.codeproject.com/audio/MPEGAudioInfo.asp
 */
public class MP3FrameReader extends FrameReader {
    /**
     * MPEG 1 version
     */
    public static final int MPEG1  = 3;
    /**
     * MPEG 2 version
     */
    public static final int MPEG2  = 2;
    /**
     * MPEG 2.5 version
     */
    public static final int MPEG25 = 0;
    /**
     * Layer I
     */
    public static final int LAYERI = 3;
    /**
     * Layer II
     */
    public static final int LAYERII = 2;
    /**
     * Layer III
     */
    public static final int LAYERIII = 1;
    // bitrate lookup table (0 free, -1 bad) kbit/s
    private static final int[][] bitrates = {
    //  V1L1 V1L2 V1L3 V2L1 V2L2 V2L3 
    	{  0,   0,   0,   0,   0,   0}, 
    	{ 32,  32,  32,  32,   8,   8}, 
    	{ 64,  48,  40,  48,  16,  16}, 
    	{ 96,  56,  48,  56,  24,  24},
    	{128,  64,  56,  64,  32,  32}, 
    	{160,  80,  64,  80,  40,  40},
    	{192,  96,  80,  96,  48,  48},
    	{224, 112,  96, 112,  56,  56}, 
    	{256, 128, 112, 128,  64,  64}, 
    	{288, 160, 128, 144,  80,  80},
    	{320, 192, 160, 160,  96,  96},
    	{352, 224, 192, 176, 112, 112}, 
    	{384, 256, 224, 192, 128, 128},
    	{416, 320, 256, 224, 144, 144},
    	{448, 384, 320, 256, 160, 160},
    	{ -1,  -1,  -1,  -1,  -1,  -1}	
    };
    // frequency lookup (-1 bad)
    private static final int frequencies[][] = {
    //   MPEG1  MPEG2  MPEG2.5
    	{44100, 22050, 11025},
    	{48000, 24000, 12000},
    	{32000, 16000,  8000},
    	{   -1,    -1,    -1}
    };
    private static final int samples[][] = {
    //   MPEG1  MPEG2  MPEG2.5
    	{ 384,  384,  384}, // Layer I
    	{1152, 1152, 1152}, // Layer II
    	{1152,  576,  576}  // Layer III
    };
    private static final int framesync = 2047;
    private static final int headerLength = 4;
    private static final int headerDelimiter = 255;
    private int framesize;
    private int bitrate;
    private int frequency;
    private int sample;
    private int version;
    private int layer;
    private int pad;
    private int xb, yb, xf, yf;
    private int bytes;
    private byte[] header;
    private byte[] frame;
    private byte[] tag, candidate;
	private final Bitmask headerBitmask;
	public MP3FrameReader() {
		this.tag = "TAG".getBytes();
		this.candidate = new byte[this.tag.length];
		this.header = new byte[headerLength];
		this.headerBitmask = new Bitmask();
	}
	public Frame readFrame() throws IOException {
		boolean validFrame = false;
		while(!validFrame) {
			try {
				if ((bytes = inputConnector.read(header, 0, headerLength,
						headerDelimiter)) < 0)
					return null;
			} 
			catch (DelimiterNotFoundException e) {
				// out of sync
				continue;
			}
			if(bytes < headerLength) {
				Fluid.error("Incomplete header");
				continue;
			}
			headerBitmask.setBytes(header);
			// last 11 bits are set in a valid frame
			if (headerBitmask.get(21, 31) != framesync) {
				Fluid.error("MP3: Invalid frame");
				continue;
			}
			version = headerBitmask.get(19, 20);
			layer = headerBitmask.get(17, 18);
			yb = headerBitmask.get(12, 15);
			yf = headerBitmask.get(10, 11);
			pad = headerBitmask.get(9);
			if (layer == 0) {
				Fluid.error("MP3: Layer failure");
				continue;
			}
			if (layer == LAYERI)
				pad = pad * 4;
			switch (version) {
			case MPEG1:
				xb = 3 - layer;
				xf = 0;
				break;
			case MPEG2:
				xb = 6 - layer;
				xf = 1;
				break;
			case MPEG25:
				xb = 6 - layer;
				xf = 2;
				break;
			default:
				Fluid.error("MP3: Unknown MPEG version");
				continue;
			}
			bitrate = bitrates[yb][xb];
			if (bitrate == 0) {
				Fluid.error("MP3: Free bitrates are not supported");
				continue;
			}
			if (bitrate < 0) {
				Fluid.error("MP3: Bitrate failure");
				continue;
			}
			frequency = frequencies[yf][xf];
			if (frequency < 0) {
				Fluid.error("MP3: Frequency failure");
				continue;
			}
			// assume the frame is valid
			validFrame = true;
		}
		sample = samples[3 - layer][xf];
		framesize = ((sample * bitrate * 1000 / 8) / frequency) + pad;
		frame = new byte[framesize];
		System.arraycopy(header, 0, frame, 0, headerLength);
		bytes = inputConnector.read(frame, headerLength, framesize - headerLength);
		// look for intruding ID3 tag
		for(int i = bytes + headerLength - tag.length; i >= headerLength; i--) {
			System.arraycopy(frame, i, candidate, 0, tag.length);
			if(Arrays.equals(candidate, tag)) {
				bytes = i - headerLength;
				break;
			}
		}
		return new Frame(frame, bitrate, bytes + headerLength);
	}
}
