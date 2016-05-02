/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openremote.controller.protocol.somfy.rts;

import gnu.io.SerialPort;
import java.security.InvalidParameterException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author mark
 * 
 * This test case will test the functionality of the SerialConnumicator.
 * It will test the following functions:
 * 
 * - open a port
 * - close a port
 * - register a observer for receiving data form the port
 * - unregister a observer
 * - write data to the port
 * 
 * = Open port and write data to the port
 * = close the port and write data 
 * = register observer and receive data
 * = unregister observer and check if no data is received anymore
 */
public class SerialCommunicatorTest {
    
    private static final String SERIAL_PORT = "/dev/ttyUSB0";
    private static final int BAUD_RATE = 4800;
    private static final int DATABITS = SerialPort.DATABITS_8;
    private static final int STOPBITS = SerialPort.STOPBITS_1;
    private static final int PARITY = SerialPort.PARITY_NONE;
    
    private SerialCommunicatorMock mCommunicator;
    private volatile byte[] mReceivedData;

    private final SerialDataReceiveObeserver mObserver = new SerialDataReceiveObeserver() {
        @Override
        public boolean onReceive(byte[] data) {
            Logger.getLogger(TimeOutTimerTest.class.getName()).log(Level.INFO, "Data received: {0}", data);
            mReceivedData = data;
            return true;
        }
    };

    
    public SerialCommunicatorTest() {
    }
    
    @Before
    public void setUp() {
        mReceivedData = new byte[0];
        mCommunicator = new SerialCommunicatorMock(BAUD_RATE, DATABITS, STOPBITS, PARITY);
        sleep(1000);
    }
    
    @After
    public void tearDown() {
        mCommunicator.terminate();
    }
    
    @Test(expected=InvalidParameterException.class) 
    public void test_invalid_port_parameter() {
        mCommunicator.openPort(null);
        mCommunicator.openPort("");
    }
    
    @Test
    public void test_open_port() {
        assertTrue("Opening port failed", mCommunicator.openPort(SERIAL_PORT));
    }

    @Test 
    public void test_close_port_after_opening() {
        assertTrue("Opening port failed", mCommunicator.openPort(SERIAL_PORT));
        assertTrue("Closing port failed", mCommunicator.closePort());
    }
    
    @Test
    public void test_register_observer() {
        assertTrue("Register obeserver failed", mCommunicator.registerReceiveObserver(mObserver));
        assertTrue("Observer is not in observer list", mCommunicator.containsObserver(mObserver));
        assertTrue("Number of observers is not as expected", mCommunicator.sizeOfObservers() == 1);
    }

    @Test
    public void test_unregister_observer() {
        // first register
        assertTrue("Register obeserver failed", mCommunicator.registerReceiveObserver(mObserver));
        assertTrue("Observer is not in observer list", mCommunicator.containsObserver(mObserver));
        
        // unregister
        assertTrue("unregister obeserver failed", mCommunicator.unregisterReceiveObserver(mObserver));
        assertFalse("Observer is not removed from observer list", mCommunicator.containsObserver(mObserver));
    }
    
    
    @Test
    public void test_open_port_and_write_data() {
        String data = "?";
        assertTrue("Opening port failed", mCommunicator.openPort(SERIAL_PORT));
        assertTrue("Writing to com port failed", mCommunicator.write(data));
    }
    
    @Test 
    public void test_open_port_and_receive_data() {
        String data = "?";
        String expectedData = "USB I/O 24R1";
        assertTrue("Opening port failed", mCommunicator.openPort(SERIAL_PORT));
        assertTrue("Register obeserver failed", mCommunicator.registerReceiveObserver(mObserver));
        // write data to get response from the serial device
        assertTrue("Writing to com port failed", mCommunicator.write(data));
        
        sleep(200);
        
        assertEquals("Data received is not like expected", expectedData, new String(mReceivedData).trim());
        
        assertTrue("Register obeserver failed", mCommunicator.unregisterReceiveObserver(mObserver));
    }

    @Test 
    public void test_open_port_write_bytes() {
        byte[] data = new byte[3];
        
        data[0] = (byte)'!';
        data[1] = (byte)'A';
        data[2] = 0x0F;
        
        assertTrue("Opening port failed", mCommunicator.openPort(SERIAL_PORT));
        assertTrue("Register obeserver failed", mCommunicator.registerReceiveObserver(mObserver));
        
        // write data to get response from the serial device
        assertTrue("Writing to com port failed", mCommunicator.write(data, 3));
        
        sleep(10000); 

        assertTrue("Register obeserver failed", mCommunicator.unregisterReceiveObserver(mObserver));
    }
    
    @Test 
    public void test_terminate_communicator() {
        SerialDataReceiveObeserver observer = new SerialDataReceiveObeserver() {  
            @Override
            public boolean onReceive(byte[] data) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };

        assertTrue("Opening port failed", mCommunicator.openPort(SERIAL_PORT));
        assertTrue("Register obeserver failed", mCommunicator.registerReceiveObserver(observer));
        assertTrue("Register obeserver failed", mCommunicator.registerReceiveObserver(observer));
        assertTrue("Number of observers is not as expected", mCommunicator.sizeOfObservers() == 2);
        assertTrue("Terminate communicator failed", mCommunicator.terminate());
        assertTrue("Number of observers is not as expected", mCommunicator.sizeOfObservers() == 0);
    }
    
    
    
    class SerialCommunicatorMock extends SerialCommunicator {

        public SerialCommunicatorMock(int baudRate, int databits, int stopbits, int parity) {
            super(baudRate, databits, stopbits, parity);
        }
        
        public boolean containsObserver(SerialDataReceiveObeserver observer) {
            return mOnRevieveObservers.contains(observer);
        }
        
        public int sizeOfObservers() {
            return mOnRevieveObservers.size();
        }
    }
    
    private void sleep(int millisec) {
        try {
            Thread.sleep(millisec);
        } catch (InterruptedException ex) {
            Logger.getLogger(TimeOutTimerTest.class.getName()).log(Level.SEVERE, "Something bad happend when thread was sleeping...", ex);
        }
    }
}

