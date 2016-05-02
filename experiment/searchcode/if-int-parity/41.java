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

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;


import java.io.IOException;

import jcummins.misc.ThreadDelay;
import jcummins.serial.SerialComm;



public class PingTester {

    private final char PING_CODE;
    private final int RESPONSE_TIME;

    public PingTester(char pingCode, int responseTime) {
	this.PING_CODE = pingCode;
	this.RESPONSE_TIME = responseTime;
    }

    public boolean pingTest(int testReps, String portName, int baudRate,
	    int dataBits, int stopBits, int parity, int flowControl)
	    throws NoSuchPortException, PortInUseException,
	    UnsupportedCommOperationException {
	SerialComm t = new SerialComm(portName, baudRate, dataBits, stopBits,
		parity, flowControl);
	try {
	    t.connect(PingTester.class.getName());
	} catch (IOException e) {
	    return false;
	}
	for (int i = 0; i < testReps; i++)
	    if (!pingTest(t))
		return false;
	return true;
    }

    public boolean pingTest(String portName, int baudRate, int dataBits,
	    int stopBits, int parity, int flowControl)
	    throws NoSuchPortException, PortInUseException,
	    UnsupportedCommOperationException {
	return pingTest(1, portName, baudRate, dataBits, stopBits, parity,
		flowControl);
    }

    protected boolean pingTest(SerialComm t) {
	try {
	    t.write(PING_CODE);
	} catch (IOException e) {
	    return false;
	}
	ThreadDelay.delay(RESPONSE_TIME);
	String pResponse = t.read();
	t.close();

	if (pResponse == String.valueOf(PING_CODE))
	    return true;
	else
	    return false;
    }

    public String toString() {
	return PingTester.class.getName() + ": ping code '" + PING_CODE
		+ "', response time '" + RESPONSE_TIME + "' ms.";
    }
}

