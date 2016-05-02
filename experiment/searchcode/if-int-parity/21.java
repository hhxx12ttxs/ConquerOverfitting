package com.ja.tagreader;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TagReader {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(TagReader.class);
	private final String portName;
	private final int databits;
	private final int stopbits;
	private final int parity;
	private final int bautrate;
	private SerialReader reader;
	private CommPort commPort;
	private final Checksum check = new Checksum();

	private List<TagListener> listeners = new ArrayList<>();

	/**
	 * Creates a Tag Reader on a given port with default configuration: Baurate
	 * 9600, {@link SerialPort#DATABITS_8}, {@link SerialPort#STOPBITS_1},
	 * {@link SerialPort#PARITY_NONE}.
	 * 
	 * @param portName
	 *            Serial port name, e.g. /dev/ttyUSB0
	 */
	public TagReader(final String portName) {
		this(portName, 9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
				SerialPort.PARITY_NONE);
	}

	/**
	 * Creates a Tag Reader on a given port. See {@link SerialPort} for
	 * constants.
	 * 
	 * @param portName
	 *            Serial port name, e.g. /dev/ttyUSB0
	 * @param bautrate
	 * @param databits
	 * @param stopbits
	 * @param parity
	 */
	public TagReader(final String portName, final int bautrate,
			final int databits, final int stopbits, final int parity) {
		this.portName = portName;
		this.bautrate = bautrate;
		this.databits = databits;
		this.stopbits = stopbits;
		this.parity = parity;
	}

	public void start() throws Exception {

		CommPortIdentifier portIdentifier = CommPortIdentifier
				.getPortIdentifier(portName);
		if (portIdentifier.isCurrentlyOwned()) {
			throw new RuntimeException("Error: Port is currently in use");
		} else {
			commPort = portIdentifier.open(this.getClass().getName(), 2000);

			if (commPort instanceof SerialPort) {
				SerialPort serialPort = (SerialPort) commPort;
				serialPort.setSerialPortParams(bautrate, databits, stopbits,
						parity);
				serialPort.enableReceiveTimeout(1000);
				reader = new SerialReader(this, serialPort.getInputStream());
				new Thread(reader, "SerialReader").start();

			} else {
				throw new RuntimeException("Only serial ports supported");
			}
		}
	}

	public void addTagListener(TagListener listener) {
		if (listener != null) {
			listeners.add(listener);
		}
	}

	public void fireOnTag(String rfidTag) {
		try {
			LOGGER.info("Tag: {}", rfidTag);
			String id = check.checkAndGetId(rfidTag);

			for (TagListener l : listeners) {
				l.onRfidTag(id);
			}
		} catch (ChecksumException e) {
			LOGGER.error("Invalid RFID Tag {}. {}", rfidTag, e.getMessage());
			return;
		}
	}

	protected List<TagListener> getListeners() {
		return listeners;
	}

	public void stop() {
		if (reader != null) {
			LOGGER.debug("Stopping reader.");
			reader.close();
		}
		if (commPort != null) {
			try {
				LOGGER.debug("Closing port {}", commPort.getName());
				commPort.close();
			} catch (Exception e) {
				LOGGER.error("Stop failed.", e);
			}
		}
	}
}

