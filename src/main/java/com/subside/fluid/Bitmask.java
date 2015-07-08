package com.subside.fluid;

/**
 * A class for easy retrieval of individual bits in a bitmask.
 *
 * Often data is read in the form of bytes, but many times 
 * there is a need to test and extract individual bits from 
 * a series of bytes. 
 *
 * @author Lars Samuelsson
 */
public class Bitmask {
    private byte[] bits;
    private byte[] bytes;
    public Bitmask() {
    	this.bits = null;
    	this.bytes = null;
    }
    /**
     * Creates a bitmask from the given array of bytes.
     *
     * @param bytes The byte array to convert to bits
     */
    public Bitmask(byte[] bytes) {
    	setBytes(bytes);
    }
    /**
     * Sets the array of bytes in this Bitmask.
     * 
     * @param bytes The byte array to use in the Bitmask.
     */
    public void setBytes(byte bytes[]) {
    	this.bytes = bytes;
    	int len = bytes.length * 8;
    	bits = new byte[len];
    	for(int i = 0; i < bytes.length; i++) {
    		for(int k = 7; k >= 0; k--) {
    			bits[--len] = (byte) ((bytes[i] >>> k) & 0x01);
    		}
    	}    	
    }
    /**
     * Extracts a series of bits as an int.
     * 
     * The returned int will be the bits from the 
     * bitmask shifted to the rightmost position.
     * The numbering of the bits are in accordance 
     * with low-endian representation.
     * <P>
     * Consider the bitmask: <BR>
     * 1110011010 <BR>
     * 9876543210 <BR>
     * <P>
     * get(5, 8) would produce the value 1100
     *
     * @param from First bit to extract from (from the right)
     * @param to   Last bit to extract to (from the right)
     * @return     The extracted bits shifted to the rightmost
     *             position
     */
    public int get(int from, int to) {
    	if(bits == null)
    		throw new IllegalStateException();
    	int dec = 0;
    	int pow = 1;
    	for(int i = from; i <= to; i++) {
    		dec += bits[i] * pow;
    		pow *= 2;
    	}
    	return dec;
    }
    /**
     * Extracts an individual bit.
     * 
     * @param index The index of the bit to extract
     *              (from the right)
     * @return      The extracted bit (0 or 1)
     */
    public byte get(int index) {
    	if(bits == null)
    		throw new IllegalStateException();
    	return bits[index];
    }
    /**
     * Gets the bits as bytes.
     *
     * @return The bits as a byte array
     */
    public byte[] getBytes() {
    	if(bits == null)
    		throw new IllegalStateException();
    	return bytes;
    }
    /**
     * Returns a string representation of this Bitmask.
     * 
     * The bits will be printed out as a series of 
     * zeros and ones with a space between each 
     * byte.
     * 
     * @return A bitmask viewed as a string
     */
    public String toString() {
    	if(bits == null)
    		throw new IllegalStateException();
    	String mask = new String();
    	String space = new String();
    	for(int i = bits.length - 1; i >= 0; i--) {
    		space = "";
    		if((i + 1) % 8 == 0 && i < bits.length - 1)
    			space = " ";
    		mask += space + bits[i];
    	}
    	return mask;
    }
    /**
     * Utility method for assembling a matrix of bytes
     * into an array of bytes.
     * 
     * The assembly is done row-wise in the matrix, 
     * ie the resulting array will start with 
     * matrix[0][] followed by matrix[1][] etc.
     *
     * @param matrix A byte matrix
     * @return       The matrix assembled into an array
     */
    public static byte[] assemble(byte[][] matrix, int rows) {
    	int len = 0;
    	for(int i = 0; i < rows; i++) 
    		len += matrix[i].length;
    	byte[] assembled = new byte[len];
    	int off = 0;
    	for(int i = 0; i < rows; i++) {
    		System.arraycopy(matrix[i], 0, assembled, off, matrix[i].length);
    		off += matrix[i].length;
    	}
    	return assembled;
    }
}
