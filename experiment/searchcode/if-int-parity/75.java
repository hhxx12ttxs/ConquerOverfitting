package de.akuz.osynce.macro.serial;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import de.akuz.osynce.macro.serial.interfaces.Command;
import de.akuz.osynce.macro.serial.interfaces.Packet;
import de.akuz.osynce.macro.serial.interfaces.PacketListener;
import de.akuz.osynce.macro.serial.interfaces.SerialPortDevice;
import de.akuz.osynce.macro.serial.packet.Commands;
import de.akuz.osynce.macro.serial.packet.PacketException;
import de.akuz.osynce.macro.serial.packet.ProviderManager;

/**
 * This class is a simple implementation of the SerialPortDevice interface.
 * Non-blocking methods aren't implemented at the moment. Time out issues
 * aren't handled. This implementation uses the RXTX library which has
 * to be installed in the system.
 * @author Till Klocke
 *
 */
public class RXTXSerialPortDevice implements SerialPortDevice, SerialPortEventListener{
	
	/**
	 * Returns a list of names of available ports on this system
	 * @return list of port names
	 */
	public static List<String> getAvailablePortNames(){
		List<String> portNames = new LinkedList<String>();
		@SuppressWarnings("rawtypes")
		Enumeration ports = CommPortIdentifier.getPortIdentifiers();
		while(ports.hasMoreElements()){
			portNames.add(((CommPortIdentifier)ports.nextElement()).getName());
		}
		return Collections.unmodifiableList(portNames);
	}
	
	private final List<PacketListener> listeners =
		new ArrayList<PacketListener>();
	
	private String portName;
	
	private SerialPort port;
	private final static String identifier = "O-Synce Macro PC-Link";
	
	private final static int timeout = 5000;
	private final static int baudrate = 9600;
	private final static int dataBits = SerialPort.DATABITS_8;
	private final static int stopBits = SerialPort.STOPBITS_1;
	
	private final static int parity = SerialPort.PARITY_NONE;
	
	public RXTXSerialPortDevice(){
		
	}

	public RXTXSerialPortDevice(String portName){
		this.portName = portName;
	}
	
	@Override
	public void addPacketListener(PacketListener listener) {
		if(listener!=null){
			listeners.add(listener);
		}
		
	}

	@Override
	public void close() {
		if (port != null) {
			port.close();
			port = null;
		}
	}
	
	private void notifyExceptionOccured(PacketException e){
		for(PacketListener l : listeners){
			l.exceptionOccured(e);
		}
	}
	
	private void notifyPacketReceived(Packet packet){
		for(PacketListener l : listeners){
			l.packetReceived(packet);
		}
	}

	private void notifyPacketStarted(Commands command){
		for(PacketListener l : listeners){
			l.packetStarted(command);
		}
	}

	private void notifyReceivingPayload(int count, int total){
		for(PacketListener l : listeners){
			l.receivingPayload(count, total);
		}
	}

	public void open() throws DeviceException{
		open(this.portName);
	}

	@Override
	public void open(String portName) throws DeviceException{
		if(port != null){
			throw new IllegalStateException("Port is already open");
		}
		this.portName = portName;
		try {
			CommPortIdentifier portId =
				CommPortIdentifier.getPortIdentifier(portName);
			port = (SerialPort)portId.open(identifier, timeout);
			port.setSerialPortParams(baudrate, dataBits, stopBits, parity);
			//port.notifyOnDataAvailable(true);
			//port.addEventListener(this);
		} catch (NoSuchPortException e) {
			throw new DeviceException(e);
		} catch (PortInUseException e) {
			throw new DeviceException(e);
		} catch (UnsupportedCommOperationException e) {
			throw new DeviceException(e);
		}
	}

	/**
	 * This method reads all available bytes from the serial port and
	 * parses them into a packet. The parsing is handled by a PacketProvider
	 * which is chosen according to the first byte received.
	 * @return
	 * @throws IOException
	 */
	private Packet readPacketBytes() throws IOException{
		int count = 0;
		Packet packet = null;
		int read = 0;
		while((read = port.getInputStream().read())>-1){
			byte readByte = (byte)read;
			if(count == 0){
				packet =
					ProviderManager.getInstance()
					.getEmptyPacket(readByte);
				notifyPacketStarted(packet.getCommand());
			} else {
				packet.addReceivedByte(readByte);
				notifyReceivingPayload(count,-1);
			}
			count++;
		}
		notifyPacketReceived(packet);
		return packet;
	}
	
	@Override
	public void removePacketListener(PacketListener listener) {
		if(listener!=null){
			listeners.remove(listener);
		}
		
	}
	
	@Override
	public Packet sendCommand(Command command) throws PacketException {
		try {
			port.getOutputStream().write(command.getBytes());
			int availableBytes = 0;
			while(availableBytes==0){
				availableBytes = port.getInputStream().available();
			}
			return readPacketBytes();
		} catch (IOException e) {
			PacketException pe = new PacketException(e);
			notifyExceptionOccured(pe);
			throw pe;
		}
	}
	
	@Override
	public void sendCommand(Command command, PacketListener listener)
			throws PacketException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void serialEvent(SerialPortEvent event) {
		
	}

}

