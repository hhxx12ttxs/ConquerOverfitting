package cstatest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.BitSet;

import com.chaosinmotion.asn1.BerInputStream;
import com.chaosinmotion.asn1.BerOutputStream;

import csta.statusreporting.MonitorObject;

public class maintest {

	
	public static void main(String[] args) throws IOException {
	
		//outputstream for encoding
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();		
		BerOutputStream out = new BerOutputStream(outStream);
		
		//input stream for decoding
		ByteArrayInputStream inputStream;
		BerInputStream in;
			
		//Criando objeto monitor
		MonitorObject mon = new MonitorObject();		
		
		//BitSet auxiliar
		BitSet bs = new BitSet(1);
		bs.set(0, 1);
		
		
		
		//Este trecho de codigo esta funiconando. 
			
		//mon.getDeviceObject().getDeviceIdentifier().getDeviceNumber().setValue(19);
		//mon.getDeviceObject().getMediaCallCharacteristics().setValue(bs);
		
				
		
		
		//Este trecho ainda esta em implementacao...		
		mon.getCallObject().getCallId().setValue(new byte[]{49, 57});		
		mon.getCallObject().getDeviceID().getDynamicID().setValue(new byte[]{});
		mon.getCallObject().getBoth().getCallId().setValue(new byte[]{});
		mon.getCallObject().getBoth().getDeviceID().getDynamicID().setValue(new byte[]{});
		
		
		System.out.println("\n -- ASN monitor object\n");
		
		//encode BerInteger
		outStream.reset();
		mon.encode(out);
		printHex(outStream.toByteArray());
		
	
		//decode
		inputStream = new ByteArrayInputStream(outStream.toByteArray());
		in = new BerInputStream(inputStream);
		mon.decode(in);
		System.out.println("  -- decoded value :  " + 
				mon.getCallObject().getDeviceID().getStaticID().getDeviceIdentifier().getDeviceNumber().getValue());		
		
		System.out.println("");
		System.out.println("");	
		
	}
	
	
	public static void printHex(byte[] coded) 
	{
		System.out.println("to byte array in HEX : ");
	    String hexDigits = "0123456789ABCDEF";
	    for (int i=0; i<coded.length; i++) {
	      int c = coded[i];
	      if (c < 0) c += 256;
	      int hex1 = c & 0xF;
	      int hex2 = c >> 4;
	      System.out.print(hexDigits.substring(hex2,hex2+1));
	      System.out.print(hexDigits.substring(hex1,hex1+1) + " ");
	    }
	    System.out.println();
	  }
	
	public static void printArray(byte[] array) {
 		//System.out.println("to byte array in DEC : ");
		for (int i=0; i<array.length; i++) {
			System.out.print(array[i]+" ");
		}
		System.out.println();
	}

}

