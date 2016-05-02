/* Chris Cummins - 14 Mar 2012
 *
 * This file is part of Kummins Library.
 *
 * Kummins Library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Kummins Library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Kummins Library.  If not, see <http://www.gnu.org/licenses/>.
 */

package jcummins.serial;

/**
 * @author Chris Cummins
 *
 */
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TooManyListenersException;

/**
 * 
 * @author Chris Cummins
 * 
 */
public class SerialComm {
	private String portName;
	private int baudRate;
	private int dataBits;
	private int stopBits;
	private int parity;
	private int flowControl;

	public SerialPort serialPort;
	private InputStream in;
	private OutputStream out;

	private byte[] readBuffer = new byte[1024];

	/**
	 * 
	 * @param portName
	 * @param baudRate
	 * @param dataBits
	 * @param stopBits
	 * @param parity
	 * @param flowControl
	 */
	public SerialComm(String portName, int baudRate, int dataBits,
			int stopBits, int parity, int flowControl) {
		this.portName = portName;
		this.baudRate = baudRate;
		this.dataBits = dataBits;
		this.stopBits = stopBits;
		this.parity = parity;
		this.flowControl = flowControl;
	}

	/**
	 * 
	 * @param portName
	 * @throws NoSuchPortException
	 * @throws PortInUseException
	 * @throws UnsupportedCommOperationException
	 * @throws IOException
	 * @throws Exception
	 */
	public void connect(String ownerName) throws NoSuchPortException,
			PortInUseException, UnsupportedCommOperationException, IOException {
		CommPortIdentifier portID = CommPortIdentifier
				.getPortIdentifier(portName);
		CommPort port = portID.open(ownerName, 5000);
		serialPort = (SerialPort) port;
		serialPort.setSerialPortParams(baudRate, dataBits, stopBits, parity);
		serialPort.setFlowControlMode(flowControl);
		in = serialPort.getInputStream();
		out = serialPort.getOutputStream();
		(new Thread(new SerialWriter(System.in, out))).start();
	}

	/**
	 * 
	 */
	public void close() {
		try {
			in.close();
			out.close();
		} catch (IOException e) {
		}
		serialPort.close();
	}

	public void setSerialReader() {
		(new Thread(new SerialReader(in, System.out))).start();
	}

	public void write(char c) throws IOException {
		out.write(c);
	}

	public void write(String s) throws IOException {
		out.write((s + "\r").getBytes());
	}

	public String read() {
		int availableBytes = 0;
		try {
			availableBytes = in.available();
			if (availableBytes > 0) {
				in.read(readBuffer, 0, availableBytes);
			}
		} catch (IOException e) {
			// Don't care.
		}
		return new String(readBuffer, 0, availableBytes);
	}

	/**
	 * 
	 * @author Chris Cummins
	 * 
	 */
	private class SerialEventHandler implements SerialPortEventListener {
		public void serialEvent(SerialPortEvent event) {
			switch (event.getEventType()) {
			case SerialPortEvent.DATA_AVAILABLE:
				System.err.println("Received: " + read());
				break;
			}
		}
	}

	/**
	 * Set the serial event handler
	 */
	public void setSerialEventHandler() {
		try {
			// Add the serial port event listener
			serialPort.addEventListener(new SerialEventHandler());
			serialPort.notifyOnDataAvailable(true);
		} catch (TooManyListenersException e) {
			System.err.println(e.getMessage());
		}
	}

	public InputStream getInputStream() {
		return in;
	}

	public OutputStream getOutputStream() {
		return out;
	}

	public String getPortName() {
		return portName;
	}

	public static void main(String[] args) {
		System.out.println("hello!");
		String portName = "COM3";
		int baudRate = 9600;
		int dataBits = SerialPort.DATABITS_8;
		int stopBits = SerialPort.STOPBITS_1;
		int parity = SerialPort.PARITY_NONE;
		int flowControl = SerialPort.FLOWCONTROL_NONE;

		try {
			SerialComm t = new SerialComm(portName, baudRate, dataBits,
					stopBits, parity, flowControl);
			t.connect("test");
			t.setSerialReader();
		} catch (Throwable e) {
			e.printStackTrace();
		}

	}
}
