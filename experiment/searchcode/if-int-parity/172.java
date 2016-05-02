package de.blitzcoder.telemetry.data.serial;

import de.blitzcoder.telemetry.data.DataStore;
import de.blitzcoder.telemetry.data.dbc.CANMessage;
import de.blitzcoder.telemetry.data.dbc.CANSignal;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.BitSet;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
/**
 *
 * @author blitzcoder
 */
public class SerialInput extends Thread {
    
    private static final int DATABITS       = SerialPort.DATABITS_8;
    private static final int STOPBITS       = SerialPort.STOPBITS_1;
    private static final int PARITY         = SerialPort.PARITY_NONE;
    private static final int BAUD           = 9600;
    private static final int FLOWCONTROL    = SerialPort.FLOWCONTROL_NONE;
    
    private static final int MASK_MODE              = 0b11111000;
    private static final int MASK_TAIL              = 0b00000111;
    
    private static final int MODE_8BYTES            = 0b00000;
    private static final int MODE_3BYTES            = 0b00001;
    private static final int MODE_LOGGER_8BYTES     = 0b00010;
    private static final int MODE_LOGGER_3BYTES     = 0b00011;
    private static final int MODE_ERROR             = 0b00100;
    private static final int MODE_CHANGED           = 0b01000;
    
    private static final int ERROR_NOCAN            = 0b000;
    private static final int ERROR_CANERRORS        = 0b001;
    private static final int ERROR_RESERVED1        = 0b010;
    private static final int ERROR_RESERVED2        = 0b011;
    private static final int ERROR_RESERVED3        = 0b111;
    private static final int ERROR_MODE_SAME        = 0b101;
    private static final int ERROR_MODE_FORBIDDEN   = 0b110;
    private static final int ERROR_CMD              = 0b100;
    
    private static final int HWMODE_LIVE            = 0b000;
    private static final int HWMODE_LOGGER          = 0b001;
    private static final int HWMODE_GATEWAY         = 0b010;
    private static final int HWMODE_REQUEST         = 0b011;
    private static final int HWMODE_OFF             = 0b100;
    
    private InputStream input;
    private DataStore store;
    private HashMap<Integer, CANMessage> messages;
    
    public SerialInput(CommPortIdentifier id, DataStore store, LinkedList<CANMessage> messages) {
        
        this.store = store;
        
        this.messages = new HashMap<>();
        ListIterator<CANMessage> it = messages.listIterator();
        while (it.hasNext()) {
            CANMessage m = it.next();
            this.messages.put((int)m.getId(), m);
        }
        
        try {
            
            SerialPort port = (SerialPort)id.open("FaSTTUBE Telemetry", Integer.MAX_VALUE);
            port.setSerialPortParams(BAUD, DATABITS, STOPBITS, PARITY);
            port.setFlowControlMode(FLOWCONTROL);
            
            input = port.getInputStream();
            
        } catch (UnsupportedCommOperationException | IOException | PortInUseException ex) {
            throw new Error("Error while opening the port", ex);
        }
        
        
        
    }
    
    @Override
    public void run() {
        try {
        while (true) {
            if (input.available() > 0) {
                if (input.read() == 0xFF)
                    parseHeader();
            } else {
                _wait();
            }
        }
        } catch (IOException ex) {
            throw new Error(ex);
        }
    }
    
    private void parseHeader() throws IOException {
        while (true) {
            if (input.available() > 0) {
                
                int header = input.read();
                
                int mode = (header & MASK_MODE) >> 3;
                
                switch (mode) {
                    case (MODE_8BYTES):
                        parsePacket(8, header, false);
                        break;
                    case (MODE_3BYTES):
                        parsePacket(3, header, false);
                        break;
                    case (MODE_LOGGER_3BYTES):
                        parsePacket(3, header, true);
                        break;
                    case (MODE_LOGGER_8BYTES):
                        parsePacket(8, header, true);
                        break;
                    case (MODE_ERROR):
                        parseError(header);
                        break;
                    case (MODE_CHANGED):
                        parseModeChange(header);
                        break;
                }
                return;
                
            } else {
                _wait();
            }
        }
    }
    
    private void parseModeChange(int header) {
        int tail = header & MASK_TAIL;
        switch (tail) {
            case HWMODE_LIVE:
                System.out.println("Switched to live mode");
                break;
            case HWMODE_LOGGER:
                System.out.println("Switched to logger mode");
                break;
            case HWMODE_GATEWAY:
                System.out.println("Switched to gateway mode");
                break;
            case HWMODE_OFF:
                System.out.println("Switched to off mode");
                break;
        }
    }
    
    private void parseError(int header) {
        int tail = header & MASK_TAIL;
        switch (tail) {
            case ERROR_RESERVED1:
            case ERROR_RESERVED2:
            case ERROR_RESERVED3:
                System.out.println("Reserved error");
                break;
            case ERROR_NOCAN:
                System.out.println("No CAN messages");
                break;
            case ERROR_CANERRORS:
                System.out.println("Bad CAN messaged");
                break;
            case ERROR_MODE_FORBIDDEN:
                System.out.println("mode change forbidden");
                break;
            case ERROR_MODE_SAME:
                System.out.println("Already in that mode");
                break;
            case ERROR_CMD:
                System.out.println("bad command");
                break;
        }
    }
    
    private void parsePacket(int bytes, int header, boolean logger) throws IOException {
        while (true) {
            if (input.available() > bytes) {
                
                int second = input.read();
                
                byte[] b = new byte[bytes];
                input.read(b);
                
                int id = (header & MASK_TAIL)<<8;
                id = id | second;
                
                if (logger)
                    processLoggerMessage(id,b);
                else
                    processMessage(id,b);
                
                return;
            } else {
                _wait();
            }
        }
    }
    
    private void processMessage(int id, byte[] b) {
        System.out.println("Message recieved. id: "+id+" len: "+b.length);
        
        CANMessage message = messages.get(id);
        if (message != null) {
            
            double[] arr = parseCAN(message, b);
            for (int i=0;i<arr.length;i++)
                store.addValue(message.getSignals()[i], arr[i], System.currentTimeMillis());
            
        }
        
    }
    
    private void processLoggerMessage(int id, byte[] b) throws IOException {
        
        int count;
        
        while (true) {
            if (input.available() > 0) {
                count = input.read();
                break;
            } else {
                _wait();
            }
        }
        
        System.out.println("Logger message recieved. id: "+id+" len: "+b.length+" count: "+count);
    }
    
    private double[] parseCAN(CANMessage msg, byte[] data) {
        
        double[] output = new double[msg.getSignals().length];
        
        BitSet set = BitSet.valueOf(data);
        for (int i=0;i<msg.getSignals().length;i++) {
            
            CANSignal s = msg.getSignals()[i];
            BitSet signal = set.get(s.getStartBit(), s.getStartBit()+s.getLength());
            
            long[] arr = signal.toLongArray();
            if (arr.length != 0) {
                
                double value = arr[0] * s.getFactor() + s.getOffset();
                
                output[i] = value;
                
            } else {
                System.out.println("Parsing failed for "+msg.getName()+"\\"+s.getName());
            }
            
        }
        
        
        return output;
    }
    
    private void _wait() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException ex) {}
    }
    
    public static void test() {
        Enumeration e = CommPortIdentifier.getPortIdentifiers();
        while (e.hasMoreElements()) {

            CommPortIdentifier port = (CommPortIdentifier) e.nextElement();

            System.out.println(port.getName());
            
            if (port.getName().equals("/dev/tty.usbserial-A601EI0X")) {
               
                test2(port);
            }
        }
        
    }
        
    private static void test2(CommPortIdentifier port) {
        //new SerialInput(port).start();
    }
    
}

