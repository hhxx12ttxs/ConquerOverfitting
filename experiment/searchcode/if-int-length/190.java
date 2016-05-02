package cc.creativecomputing.protocol.udp;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import cc.creativecomputing.io.CCByteBuffer;

public class CCUDPByteInputStream {
	
	private static InetAddress LOCAL_HOST;
	static {
		try {
			LOCAL_HOST = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			throw new CCUDPException("Could not get local host address.",e);
		}
	}
	
	protected CCByteBuffer _myByteBuffer;
	protected int _myStreamPosition;
	protected InetAddress _myAddress;

	protected CCUDPByteInputStream(final InetAddress theAddress, final byte[] theBytes, final int theBytesLength) {
		_myByteBuffer = new CCByteBuffer(theBytes, theBytesLength);
		_myStreamPosition = 0;
		_myAddress = theAddress;
	}
	
	protected CCUDPByteInputStream(final byte[] theBytes, final int theBytesLength) {
		this(LOCAL_HOST, theBytes, theBytesLength);
	}
	
	protected CCUDPByteInputStream(CCUDPByteInputStream theInputStream){
		_myByteBuffer = theInputStream._myByteBuffer;
		_myStreamPosition = 0;
		_myAddress = theInputStream.address();
	}
	
	/**
	 * Returns the IP address of the machine from which the data was received.
	 * @return the IP address of the machine from which the datagram was received.
	 */
	public InetAddress address() {
		return _myAddress;
	}
	
	/**
	 * Returns true as long as there is a byte to read
	 * @return
	 */
	public boolean hasBytes(){
		return _myStreamPosition < _myByteBuffer.length();
	}
	
	/**
	 * Returns the lengths of the stream in bytes
	 * @return
	 */
	public int length() {
		return _myByteBuffer.length();
	}
	
	/**
	 * Returns the next n bytes from the stream as array
	 * @param theLength, number of bytes to return
	 * @return
	 */
	public byte[] readBytes(final int theLength){
		byte[] myResult = _myByteBuffer.getBytes(_myStreamPosition, theLength);
		_myStreamPosition += theLength;
		return myResult;
	}

	/**
	 * Read a char from the byte stream.
	 * @return a Character
	 */
	public char readChar() {
		char myResult = _myByteBuffer.readChar(_myStreamPosition);
		_myStreamPosition += 1;
		return myResult;
	}

	/**
	 * Read an Integer (32 bit int) from the byte stream.
	 * @return an Integer
	 */
	public int readInteger() {
		final int result = _myByteBuffer.getInteger(_myStreamPosition);
		_myStreamPosition += 4;
		return result;
	}

	/**
	 * Read a Big Integer (long) from the byte stream.
	 * @return a BigInteger
	 */
	public long readLong() {
		final long result = _myByteBuffer.getLong(_myStreamPosition);
		_myStreamPosition += 8;
		return result;
	}

	/**
	 * Read a float from the byte stream.
	 * @return a Float
	 */
	public float readFloat() {
		return Float.intBitsToFloat(readInteger());
	}

	/**
	 * Read a double
	 * @return a Double
	 */
	public double readDouble() {
		return Double.longBitsToDouble(readLong());
	}

	/**
	 * Get the length of the string currently in the byte stream.
	 * @return
	 */
	private int lengthOfCurrentString() {
		int i = 0;
		while (_myByteBuffer.bytes()[_myStreamPosition + i] != 0 && _myStreamPosition + i < _myByteBuffer.bytes().length - 1){
			i++;
		}
		
		return i;
	}

	/**
	 * Move to the next byte with an index in the byte array divisable by four.
	 */	
	protected void moveToFourByteBoundry() {
		// If i'm already at a 4 byte boundry, I need to move to the next one
		int mod = _myStreamPosition % 4;
		_myStreamPosition += (4 - mod);
	}

	/**
	 * Read a string from the byte stream.
	 * @return the next string in the byte stream
	 */
	public String readString() {
		int strLen = lengthOfCurrentString();
		char[] stringChars = new char[strLen];
		for (int i = 0; i < strLen; i++)
			stringChars[i] = (char) _myByteBuffer.bytes()[_myStreamPosition++];
		moveToFourByteBoundry();
		return new String(stringChars);
	}
}
