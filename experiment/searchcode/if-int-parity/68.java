package com;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.TooManyListenersException;
/**
 * Is a singleton.
 * sends messages to the LED-controller hardware by searching the 
 * correct COM-port, opening it, transferring the message and closing the port.
 * Implements the protocol defined in "E12-LEDCTRL-001_01.pdf", except for the CRC.
 * Since using only one device for now, the DESTINATION is always "LCTL".
 * @author Niclas Scheuing
 * @version 0.1
 */

public class IOController {
	
	/**
	 * the COM-port which has to be used
	 */
	private CommPortIdentifier portId;
	/**
	 * destination to which the message has to be sent. according to protocol.
	 */
	private String destination;
	/**
	 * the SEPERATOR described in the protocol doc.
	 */
	private String seperator;
	/**
	 * true if the port is open, false else
	 */
	private boolean portIsOpen;
	/**
	 * the opened COM-port
	 * is null when not open.
	 */
	private SerialPort serialPort;
	/**
	 * write into this stream to send a message to the LED-controller hardware.
	 */
	private OutputStream outputStream;
	/**
	 * read from this stream of the LED-controller hardware has sent a message.
	 */
	private InputStream inputStream;
	/**
	 * I don't exactly understand all these values, but it seems to be working like this.
	 */
	int baudrate = 57600;
	int dataBits = SerialPort.DATABITS_8;
	int stopBits = SerialPort.STOPBITS_1;
	int parity = SerialPort.PARITY_NONE;
	private static IOController instance = null;
	/**
	 * never called from outside. using singelton pattern.
	 */
	private IOController(){
		this.destination = "LCTL";
		this.seperator = ":";
		this.portIsOpen = false;
		findPort(); //dangerous to do so. should never put exception.throwing stuff in the constructor
	}
	/**
	 * used instead of the constructor. according to singelton-pattern
	 * @return
	 */
	public static IOController getInstance(){
		if(instance == null){
			instance = new IOController();
		}
		return instance;
	}
	
	/**
	 * to be called from outside for sending a message to the LED-controller hardware.
	 * port has be be opened and closed manually.
	 * @param data a string containing the correctly formated DATA part of the protocol.
	 * @return true if the correct answer (according to the protocol) has been received. false else.
	 */
	public boolean sendMessageToLEDController(String data){
		boolean success = true;
		byte[] dataArray = createMessageArray(data);
		sendData(dataArray);
		//success = verifyAnswer(dataArray);
		try {
			Thread.sleep(100);
		} catch(InterruptedException e) { }
		return success;
	}
	
	public boolean addListener(){
		return false; //TODO
	}
	
	/**
	 * searching for serial-ports, assuming the first found is the right one.
	 * Setting the value of <code>port</code> to the found port.
	 * <p> TODO: improve the stability and error-handeling.
	 * @return true if the port has been found.
	 */
	private boolean findPort() { 
		
		Enumeration<CommPortIdentifier> enumComm = CommPortIdentifier.getPortIdentifiers();
		CommPortIdentifier serialPortId;
		boolean portFound = false;
		while(enumComm.hasMoreElements()) {
			serialPortId = (CommPortIdentifier) enumComm.nextElement();
			if(serialPortId.getPortType() == CommPortIdentifier.PORT_SERIAL){
				System.out.println("Found serial-port: "+serialPortId.getName()+". If this is the wrong port, this software sucks.");
				portFound = true;
				portId = serialPortId;
				break;
			}
		}
		if (portFound != true) {
			System.out.println("No serial-ports found");
		}
		assert(invariant());
		return portFound;
	}
	
	/**
	 * Opens the port <code>port</code>.
	 * Assigns the input- and output-stream.
	 * Adds a SerialPortEventListener.
	 * @return true if no exception was thrown. false else.
	 */
	public boolean openPort(){
		assert(invariant());
		assert(!portIsOpen);
		boolean exceptionHappend = false;		
		try {
			serialPort = (SerialPort) portId.open("Öffnen und Senden", 500);
		} catch (PortInUseException e) {
			System.out.println("Port belegt");
			exceptionHappend= true;
			return true;
		}

		try {
			outputStream = serialPort.getOutputStream();
		} catch (IOException e) {
			System.out.println("Keinen Zugriff auf OutputStream");
			exceptionHappend= true;
			serialPort.close();
		}

		try {
			inputStream = serialPort.getInputStream();
		} catch (IOException e) {
			System.out.println("Keinen Zugriff auf InputStream");
			exceptionHappend= true;
			serialPort.close();
		}
		try {
			serialPort.addEventListener(new SerialPortEventListener(){
				@Override
				public void serialEvent(SerialPortEvent event) {
					if(event.getEventType()==SerialPortEvent.DATA_AVAILABLE){
						messageFormPortArrived();
					}
				}
			});
		} catch (TooManyListenersException e) {
			System.out.println("TooManyListenersException für Serialport");
			exceptionHappend= true;
			serialPort.close();
		}
		serialPort.notifyOnDataAvailable(true);
		try {
			serialPort.setSerialPortParams(baudrate, dataBits, stopBits, parity);
		} catch(UnsupportedCommOperationException e) {
			System.out.println("Konnte Schnittstellen-Paramter nicht setzen");
			exceptionHappend= true;
			serialPort.close();
		}
		if(!exceptionHappend){
			portIsOpen = true;
		}
		assert(invariant());
		return !exceptionHappend;
	}

	/**
	 * sends the message <code>data</code> to the LED-controller hardware.
	 * @param data byte[] to be sent to the LED-controller hardware
	 * @return true if the message has been sent without errors.
	 */
	private boolean sendData(byte[] data){
		assert(invariant());
		assert(portIsOpen);
		assert(data.length>5);
		boolean sent = true;
		try {
			outputStream.write(data);
		} catch (IOException e) {
			System.out.println("Fehler beim Senden");
			sent = false;
		}
		return sent;
	}

	/**
	 * creates an byte[] according to the protocol doc with the defined parts (DATA, SEPERATOR,...).
	 * @param data String containing the DATA according to the protocol.
	 * @return the data, which can be sent to the LED-controller
	 */
	private byte[] createMessageArray(String data){
		assert(data!= null);
		assert(data.length()>0);
		assert(invariant());
		
		int length = 1+3+destination.length()+seperator.length()+data.length(); // startByte+lengthField+Destination+Seperator+Data
		byte[] bytes = new byte[length]; 
		/*START CHAR*/
		bytes[0]=0x01;
		/*LENGHTH*/
		String len = String.valueOf(length);
		bytes[1] = '0'; //filling the length field with 0 first
		bytes[2] = '0';
		bytes[3] = '0';
		for(int i=0; i<len.length(); i++){
			bytes[i+1+(3-len.length())] = (byte)(int)(len.charAt(i)); //some ugly casting to get the ASCII code of each character as a byte.
		}
		/*DESTINATION*/
		for(int i=0; i<destination.length(); i++){
			bytes[i+4] = (byte)(int)(destination.charAt(i)); 
		}
		/*SEPERATOR*/
		for(int i=0; i<seperator.length(); i++){
			bytes[i+4+destination.length()] = (byte)(int)(seperator.charAt(i)); 
		}
		/*DATA*/
		for(int i=0; i< data.length(); i++){
			bytes[i+1+3+destination.length()+seperator.length()] = (byte)(int)(data.charAt(i));
		}
		//for(int i=0; i<bytes.length; i++){ System.out.print(Character.toString ((char) bytes[i])); }
		assert(invariant());
		assert(bytes[0]==(byte)(1));
		assert(bytes[length-1] == (byte)(int)(';'));
		assert(bytes.length>0);
		return bytes;
	}
	
	public void closePort(){
		assert(invariant());
		assert(portIsOpen);
		serialPort.close();
		portIsOpen = false;
	}
	private void messageFormPortArrived() {
		//TODO
	}
	
	private boolean invariant() {
		boolean invar;
		invar = (seperator != null && seperator.length()==1);
		invar = (destination != null && destination.length()>1);
		invar = (portId != null);
		
		return invar;
	}
}

