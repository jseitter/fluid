package com.subside.fluid;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DelimitedInputStream extends BufferedInputStream {
		public DelimitedInputStream(InputStream inputStream) {
			super(inputStream);
		}
		public int read(byte[] buffer, int offset, int length, int delim) throws IOException, DelimiterNotFoundException {
			byte[] scan = new byte[length];
			int num = super.read(scan, 0, length);
			// end of stream
			if(num < 0)
				return num;
			// scan for header
			int pos = 0;
			while(pos < num && signedByteToUnsignedInt(scan[pos]) != delim) {
				pos += 1;
			}
			// if nothing was found
			if(pos >= num) {
				throw new DelimiterNotFoundException("Having searched " + num + " bytes)");
			}
			else {
				int rest = pos + length - num;
				if(rest > 0) {
					System.arraycopy(scan, pos, buffer, offset, num - pos);
					num = read(buffer, offset + num - pos, rest);
					if(num < 0)
						return num;
				}
				else {
					System.arraycopy(scan, pos, buffer, offset, length);
				}
			}
			return length;
		}
		public int read(byte[] buffer, int offset, int length) throws IOException {
			return readTotal(buffer, offset, length, 0);
		}
		private int readTotal(byte[] buffer, int offset, int length, int total) throws IOException {
			int num;
			while((num = super.read(buffer, offset, length)) < length) {
				if(num < 0)
					return num;
				else
					return readTotal(buffer, offset + num, length - num, total + num);
			}
			return total + num;
		}
	    private int signedByteToUnsignedInt(byte b) {
	    	return (int) b & 0xFF;
	    }
}
