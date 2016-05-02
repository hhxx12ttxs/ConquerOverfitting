/**
 * @author Neil J Thomson (njt@fishlegs.co.uk)
 *
 * Copyright (C) 2013 Neil J Thomson
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 */
package couk.nucmedone.shinyplopper.chambers;

import java.util.HashMap;
import java.util.Map;

import jssc.SerialPort;
import jssc.SerialPortException;
import couk.nucmedone.shinyplopper.config.PloppyProps;

public abstract class AbstractChamber implements Chamber, Runnable {

	protected Map<String, String> unitMap = new HashMap<String, String>();

	protected StringBuffer nuclide = new StringBuffer();

	protected StringBuffer units = new StringBuffer();

	private ChamberListener listener;

	private SerialPort serialPort = null;

	private final PloppyProps props = new PloppyProps();

	private final int baudRate;

	private final int dataBits;

	private final int stopBits;

	private final int parity;

	private final String port;

	public AbstractChamber() {
		super();
		populateUnitMap();

		port = props.getDevice();
		baudRate = Integer.parseInt(props.getBaudrate());
		dataBits = Integer.parseInt(props.getDataBits());
		stopBits = Integer.parseInt(props.getStopBits());
		parity = props.getParityAsInteger();

		if (port != null && !port.equals(PloppyProps.NO_DEVICE)) {
			serialPort = new SerialPort(port);
		}
	}

	public void addListener(ChamberListener listener) {
		this.listener = listener;
	}

	protected void close() throws SerialPortException {
		serialPort.closePort();
	}

	protected int getNextByte() throws SerialPortException {
		byte[] bytes = serialPort.readBytes(1);
		return bytes[0];
	}

	public SerialPort getSerialPort(){
		return serialPort;
	}

	protected void open() throws SerialPortException {

		if (serialPort != null) {
			serialPort.openPort();
			serialPort.setParams(baudRate, dataBits, stopBits, parity);
		}
	}

	protected abstract void populateUnitMap();

	public abstract void read();

	public void run(){
		read();
	}

	public abstract void setNuclide(CharSequence nuclide);

	protected void update(CharSequence text) {
		listener.onActivityUpdate(text);
	}

	protected void write(CharSequence command) throws SerialPortException {
		if (serialPort != null && command != null && command.length() > 0) {
			serialPort.writeBytes(command.toString().getBytes());
		}
	}
}

