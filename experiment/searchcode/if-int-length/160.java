package librfid.rfid;

import java.math.BigInteger;

import librfid.utils.ByteUtils;

public class Serial {
	
	private byte[] bytes;
	
	public Serial(byte[] ibytes) {
		bytes = ibytes;
	}
	
	public Serial(String serial) {
		bytes = serial.getBytes();
	}

	public int length() {
		return bytes.length;
	}
	
	public int size() {
		return this.length();
	}
	
	public String toString() {
		return ByteUtils.byteArrayToHex(bytes);
	}
	
	public  String toHexString() {
		String result = "";
		for (int i = 0 ; i < bytes.length-1 ; i++) {
			result += ByteUtils.byteToHex(bytes[i]);
		}
		if(bytes.length > 0)
			result += ByteUtils.byteToHex(bytes[bytes.length-1]);;
		return result;
	}
	
	public int vendorID() {
		return ByteUtils.unsignedByteToInt(bytes[1]);
	}
	
	public byte[] toByteArray() {
		return bytes;
	}
	
	//public int toInteger() {
	//	return ByteUtils.mergeBytes(bytes);
	//}
	
	public int toInteger() {
		return (new BigInteger(bytes)).hashCode();
	}
	
	public boolean equals(Serial s) {
		return (s.toString().equals(this.toString()));
	}
	
	public boolean equals(Object o) {
		return (o.getClass() == Serial.class) && (o.toString().equals(this.toString()));
	}
	
}

