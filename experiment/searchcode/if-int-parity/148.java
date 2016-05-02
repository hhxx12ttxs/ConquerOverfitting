
package FI.realitymodeler;

import FI.realitymodeler.common.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.comm.*;

class MobileSocketShutdownHook extends Thread {

    public void run() {
        if (MobileSocketImpl.serialPort != null) MobileSocketImpl.serialPort.close();
    }

}

/** Datagram socket implementation which uses serial port to communicate with mobile phones.
    Implementation conforms to specifications GSM 03.38 version 5.3.0, GSM 03.40 version 5.3.0,
    GSM 03.41 version 5.3.0, GSM 03.42 version 7.1.1, GSM 07.05 version 5.5.0. */
public class MobileSocketImpl extends W3SocketImpl implements SerialPortEventListener {
    public static final int STORE_MESSAGE = 0x1, SEND_MESSAGE = 0x2, STORE_AND_SEND_MESSAGE = 0x3;
    public static final boolean DEBUG = true;

    static final int BLOCK_MODE = 0;
    static final int PDU_MODE = 1;
    static final int TEXT_MODE = 2;
    static final String TP_failureCauseMessages[] = {
        // 0x80
        "Telematic interworking not supported",
        // 0x81
        "Short message Type 0 not supported",
        // 0x82
        "Cannot replace short message",
        // 0x83 - 0x8e
        null, null, null, null, null, null, null, null, null, null, null, null,
        // 0x8f
        "Unspecified TP-PID error",
        // 0x90
        "Data coding scheme (alphabet) not supported",
        // 0x91
        "Message class not supported",
        // 0x92 - 0x9e
        null, null, null, null, null, null, null, null, null, null, null, null, null,
        // 0x9f
        "Unspecified TP-DCS error",
        // 0xa0
        "Command cannot be actioned",
        // 0xa1
        "Command unsupported",
        // 0xa2 - 0xae
        null, null, null, null, null, null, null, null, null, null, null, null, null,
        // 0xaf
        "Unspecified TP-Command error",
        // 0xb0
        "TPDU not supported",
        // 0xb1 - 0xbf
        null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
        // 0xc0
        "SC busy",
        // 0xc1
        "No SC subscription",
        // 0xc2
        "SC system failure",
        // 0xc3
        "Invalid SME address",
        // 0xc4
        "Destination SME barred",
        // 0xc5
        "SM Rejected-Duplicate SM",
        // 0xc6 - 0xcf
        null, null, null, null, null, null, null, null, null, null,
        // 0xd0
        "SIM SMS storage full",
        // 0xd1
        "No SMS storate capability in SIM",
        // 0xd2
        "Error in MS",
        // 0xd3
        "Memory capacity exceeded"
    };
    static final String causeMessages[] = {
        "No such message",
        "No memory",
        "No air interface",
        "Receiving entity busy",
        "Command not understood",
        "Incoming data call",
        "User invoked exit"
    };
    static final int SMS_DELIVER = 0x0,
        SMS_DELIVER_REPORT = 0x0,
        SMS_STATUS_REPORT = 0x2,
        SMS_COMMAND = 0x2,
        SMS_SUBMIT = 0x1,
        SMS_SUBMIT_REPORT = 0x1,
        UNKNOWN_NUMBER = 0x80,
        INTERNATIONAL_NUMBER = 0x90,
        NATIONAL_NUMBER = 0xa0,
        NETWORK_SPECIFIC_NUMBER = 0xb0,
        SUBSCRIBER_NUMBER = 0xc0,
        ALPHANUMERIC_NUMBER = 0xd0,
        ABBREVIATED_NUMBER = 0xe0,
        UNKNOWN_NUMBERING_PLAN = 0x0,
        ISDN_NUMBERING_PLAN = 0x1,
        DATA_NUMBERING_PLAN = 0x3,
        TELEX_NUMBERING_PLAN = 0x4,
        NATIONAL_NUMBERING_PLAN = 0x8,
        PRIVATE_NUMBERING_PLAN = 0x9,
        ERMES_NUMBERING_PLAN = 0xa,
        MORE_MESSAGES_TO_SEND = 0x4,
        VALIDITY_PERIOD_FORMAT = 0x10,
        STATUS_REPORT_INDICATION = 0x20,
        STATUS_REPORT_REQUEST = 0x20,
        REPLY_PATH = 0x80,
        USER_DATA_HEADER_INDICATOR = 0x40,
        REJECT_DUPLICATES = 0x4,
        STATUS_REPORT_QUALIFIER = 0x20,
        TE_LIST_REQUEST = 0x0,
        TE_GET_MESSAGE = 0x1,
        TE_GET_FIRST_MESSAGE = 0x2,
        TE_GET_NEXT_MESSAGE = 0x3,
        TE_TRANSFER_INC_SMS = 0x4,
        TE_INDICATE_INC_SMS = 0x5,
        TE_TRANSFER_INC_CBS = 0x6,
        TE_INSERT_SMS = 0x7,
        TE_DELETE_MESSAGE = 0x8,
        TE_UNABLE_TO_PROCESS = 0x9,
        TE_END_SMS_MODE = 0x1e,
        TE_ACKNOWLEDGE_MESSAGE = 0x1f,
        MT_MESSAGE_LIST = 0x20,
        MT_MESSAGE = 0x21,
        MT_GET_MESSAGE_FAILURE = 0x22,
        MT_INC_MESSAGE = 0x23,
        MT_MESSAGE_ARRIVED = 0x24,
        MT_INSERT_SMS_COMPLETE = 0x25,
        MT_INSERT_SMS_FAILURE = 0x26,
        MT_DELETE_MESSAGE_COMPLETE = 0x27,
        MT_DELETE_MESSAGE_FAILURE = 0x28,
        MT_UNABLE_TO_PROCESS = 0x29,
        MT_REQUEST_CONFIRMED = 0x2a,
        MT_END_SMS_MODE = 0x3f,
        GS_CELL_WIDE_IMMEDIATE = 0x0,
        GS_PLMN_WIDE_NORMAL = 0x1,
        GS_LOCATION_AREA_WIDE_NORMAL = 0x2,
        GS_CELL_WIDE_NORMAL = 0x3,
        CONFIRM_TRANSFER_SMS = 0x1,
        CONFIRM_TRANSFER_CBS = 0x2,
        CONFIRM_ARRIVAL = 0x3,
        CONFIRM_SEND = 0x4,
        SMS_TRANSFER_TYPE_INFO_ELEMENT_ID = 0x1,
        CBS_TRANSFER_TYPE_INFO_ELEMENT_ID = 0xa,
        INSERT_TYPE_INFO_ELEMENT_ID = 0x3,
        SHORT_MESSAGE_DATA_SMS_INFO_ELEMENT_ID = 0x6,
        SHORT_MESSAGE_DATA_CBS_INFO_ELEMENT_ID = 0x7,
        CAUSE_INFORMATION_ELEMENT_ID = 0x8,
        TP_FAILURE_CAUSE_INFORMATION_ELEMENT_ID = 0xe,
        SHORT_MESSAGE_REFERENCE_INFO_ELEMENT_ID = 0x0,
        EXTEND_CLS = 0x0,
        CHANGE_CHARACTER_SET = 0x1,
        CHANGE_UCS2_ROW = 0x2,
        CHANGE_HUFFMAN_INITIALIZATION = 0x3,
        CHANGE_KEYWORD_DICTIONARY = 0x4,
        CHANGE_PUNCTUATOR = 0x5,
        CHANGE_CHARACTER_GROUP = 0x6,
        NEW_7_BIT_CHARACTER = 256,
        NEW_8_BIT_CHARACTER = 257,
        KEYWORD = 258,
        NEW_UCS2_ROW = 266;

    static DataInputStream serialIn = null;
    static DataOutputStream serialOut = null;
    static W3Lock sendLock = new W3Lock(true);
    static SerialPort serialPort = null;
    static String portName = "COM1";
    static Vector<MultiDatagramPacket> messages = new Vector<MultiDatagramPacket>();
    static boolean verbose = false;
    static int baudRate = 115200;
    static int dataBits = -1;
    static int flowControl = -1;
    static int parity = -1;
    static int stopBits = -1;
    //	static int insertType = SEND_MESSAGE;
    static int insertType = STORE_MESSAGE;

    int dataCoding = CellularSocket.ASCII_DATA_CODING;
    int headerCoding = CellularSocket.NO_HEADERS;
    //	int mode = BLOCK_MODE;
    int mode = PDU_MODE;
    //	int mode = TEXT_MODE;
    int notifyMode = 0;
    int pollingInterval = 10000;
    int timeToLive = 0;
    int timeout = 0;
    int causeValue = 0;
    int confirmType = 0;
    int RP_causeValue = 0;
    int TP_failureCause = 0;
    int shortMessageReference = 0;
    long readCheckSum = 0L, writeCheckSum = 0L;

    public static void setInsertType(int insertType) {
        MobileSocketImpl.insertType = insertType;
    }

    public static void setBaudRate(int baudRate) {
        MobileSocketImpl.baudRate = baudRate;
    }

    public static void setDataBits(int dataBits)
        throws IOException {
        switch (dataBits) {
        case 5:
            MobileSocketImpl.dataBits = SerialPort.DATABITS_5;
            break;
        case 6:
            MobileSocketImpl.dataBits = SerialPort.DATABITS_6;
            break;
        case 7:
            MobileSocketImpl.dataBits = SerialPort.DATABITS_7;
            break;
        case 8:
            MobileSocketImpl.dataBits = SerialPort.DATABITS_8;
            break;
        default:
            throw new IOException("Invalid number of data bits, must be 5-8");
        }
    }

    public static void setStopBits(int stopBits)
        throws IOException {
        switch (stopBits) {
        case 1:
            MobileSocketImpl.stopBits = SerialPort.STOPBITS_1;
            break;
        case 2:
            MobileSocketImpl.stopBits = SerialPort.STOPBITS_2;
            break;
        case 3:
            MobileSocketImpl.stopBits = SerialPort.STOPBITS_1_5;
            break;
        default:
            throw new IOException("Invalid number of stop bits, must be 1-3");
        }
    }

    public static void setParity(char parity)
        throws IOException {
        switch (parity) {
        case 'n':
            MobileSocketImpl.parity = SerialPort.PARITY_NONE;
            break;
        case 'o':
            MobileSocketImpl.parity = SerialPort.PARITY_ODD;
            break;
        case 'e':
            MobileSocketImpl.parity = SerialPort.PARITY_EVEN;
            break;
        case 'm':
            MobileSocketImpl.parity = SerialPort.PARITY_MARK;
            break;
        case 's':
            MobileSocketImpl.parity = SerialPort.PARITY_SPACE;
            break;
        default:
            throw new IOException("Invalid parity, must be n, o, e, m or s");
        }
    }

    public static void setFlowControl(char flowControl)
        throws IOException {
        switch (flowControl) {
        case 'n':
            MobileSocketImpl.flowControl = SerialPort.FLOWCONTROL_NONE;
            break;
        case 'r':
            MobileSocketImpl.flowControl = SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT;
            break;
        case 'x':
            MobileSocketImpl.flowControl = SerialPort.FLOWCONTROL_XONXOFF_IN | SerialPort.FLOWCONTROL_XONXOFF_OUT;
            break;
        default:
            throw new IOException("Invalid flow control, must be n, r or x");
        }
    }

    synchronized void checkSend()
        throws IOException {
        try {
            sendLock.lock();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        if (causeValue > 0) {
            StringBuffer sb = new StringBuffer();
            sb.append(causeValue < 7 ? causeMessages[causeValue] : "Other error");
            if (TP_failureCause >= 0x80 && TP_failureCause <= 0xd3) {
                String message = TP_failureCauseMessages[TP_failureCause];
                if (message != null) sb.append(' ').append(message);
            }
            throw new IOException(sb.toString());
        }
    }

    synchronized void block()
        throws InterruptedException {
        wait(timeout);
    }

    /** Seeks next data frame from input stream. */
    void seekMessage()
        throws IOException {
        for (byte b;;) {
            b = serialIn.readByte();
            if (b == 0x10) {
                b = serialIn.readByte();
                if (b == 0x02) break;
            }
        }
        readCheckSum = 0L;
    }

    void checkMessage()
        throws IOException {
        int b = serialIn.readByte();
        int checkSum = b << 8;
        b = serialIn.readByte();
        checkSum |= b;
        if ((readCheckSum + checkSum) % 0x10000 != 0x0) throw new IOException("Invalid check sum");
    }

    void finishMessage()
        throws IOException {
        int b = serialIn.readByte();
        if (b != 0x10) throw new IOException();
        b = serialIn.readByte();
        if (b != 0x03) throw new IOException();
        checkMessage();
    }

    int peekByte()
        throws IOException {
        int b = serialIn.readByte();
        if (b == 0x10) {
            byte d = serialIn.readByte();
            if (d == 0x03) {
                checkMessage();
                return -1;
            }
            if (d != 0x0) throw new IOException();
        }
        readCheckSum += b;
        return b;
    }

    int readByte(InputStream in)
        throws IOException {
        int c;
        if ((c = in.read()) == -1) throw new EOFException();
        if (c == 0x10) {
            int d;
            if ((d = in.read()) == -1) throw new EOFException();
            if (d != 0x0) throw new IOException();
        }
        readCheckSum += c;
        return c;
    }

    int readShort(InputStream in)
        throws IOException {
        return (readByte(in) << 8) | readByte(in);
    }

    void readData(InputStream in, byte data[], boolean compressed, boolean sevenBits)
        throws IOException {
        if (compressed) {
            int header[] = new int[7], bits[] = new int[7], compressionHeader = readByte(in);
            header[EXTEND_CLS] = (compressionHeader >> 3) & 0xf;
            boolean headerContinues = (compressionHeader & 0x80) != 0x0,
                punctuationProcessingEnabled = (compressionHeader & 0x4) != 0x0,
                keywordProcessingEnabled = (compressionHeader & 0x2) != 0x0,
                characterGroupProcessingEnabled = (compressionHeader & 0x1) != 0x0;
            bits[0] = 3;
            while (headerContinues) {
                compressionHeader = readByte(in);
                headerContinues = (compressionHeader & 0x80) != 0x0;
                int extensionType = (compressionHeader >> 4) & 0x7,
                    extensionValue = compressionHeader & 0xf;
                if (extensionType < 0x7) {
                    header[extensionType] = (extensionValue << bits[extensionType]) | header[extensionType];
                    bits[extensionType] += 4;
                }
            }
        }
        if (sevenBits) {
            // 7-bit data coding
            int lastByte = 0, remainingBits = 0;
            for (int i = 0; i < data.length; i++) {
                int newByte = remainingBits < 7 ? readByte(in) : 0;
                data[i] = (byte)((lastByte >> (8 - remainingBits)) | ((newByte << remainingBits) & 0x7f));
                if (++remainingBits > 7) remainingBits = 0;
                lastByte = newByte;
            }
            return;
        }
        // 8-bit data coding
        for (int i = 0; i < data.length; i++) data[i] = (byte)readByte(serialIn);
    }

    void skip(InputStream in, int n)
        throws IOException {
        while (--n >= 0) readByte(in);
    }

    byte[] readBCD(InputStream in, int length)
        throws IOException {
        byte data[] = new byte[length];
        for (int i = 0; i < data.length;) {
            int c = readByte(in);
            if ((c & 0xf) == 0xf) break;
            data[i++] = (byte)('0' + (c & 0xf));
            if ((c & 0xf0) == 0xf0) break;
            data[i++] = (byte)('0' + ((c >> 4) & 0xf));
        }
        return data;
    }

    void skipMessage(InputStream in)
        throws IOException {
        for (int c;;) {
            if ((c = in.read()) == -1) throw new EOFException();
            if (c != 0x10) continue;
            if ((c = in.read()) == -1) throw new EOFException();
            if (c != 0x03) continue;
            return;
        }
    }

    void startMessage()
        throws IOException {
        if (DEBUG) System.out.println("startMessage");
        serialOut.writeByte(0x10);
        serialOut.writeByte(0x02);
        writeCheckSum = 0L;
    }

    void writeByte(int c)
        throws IOException {
        if (c == 0x10) {
            serialOut.writeByte(c);
            serialOut.writeByte(0x0);
        } else serialOut.writeByte(c);
        writeCheckSum += c;
    }

    void writeBytes(byte bytes[])
        throws IOException {
        for (int i = 0; i < bytes.length; i++) writeByte(bytes[i]);
    }

    void writeBCD(byte data[])
        throws IOException {
        for (int i = 0; i < data.length; i++) {
            int c = (data[i++] & 0xff) - '0';
            c |= i < data.length ? ((data[i] & 0xff) - '0') << 4 : 0xf0;
            writeByte(c);
        }
    }

    void writeBCD(OutputStream out, byte data[])
        throws IOException {
        for (int i = 0; i < data.length; i++) {
            int c = (data[i++] & 0xff) - '0';
            c |= i < data.length ? ((data[i] & 0xff) - '0') << 4 : 0xf0;
            out.write(c);
        }
    }

    void stopMessage()
        throws IOException {
        serialOut.writeByte(0x10);
        serialOut.writeByte(0x03);
        int checkSum = (int)((((writeCheckSum % 0x10000) ^ 0xffff) + 1) & 0xffff);
        if (DEBUG) {
            System.out.println("stopMessage, checkSum="+checkSum);
            System.out.println("result="+((checkSum+writeCheckSum)%0x10000));
        }
        serialOut.writeByte((checkSum >> 8) & 0xff);
        serialOut.writeByte(checkSum & 0xff);
        serialOut.flush();
    }

    public byte[] decodeHex(String hexData) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        int length = hexData.length();
        for (int i = 0; i < length; i += 2) bout.write(Integer.parseInt(hexData.substring(i, 2)));
        return bout.toByteArray();
    }

    public void serialEvent(SerialPortEvent ev) {
        if (ev.getEventType() != ev.DATA_AVAILABLE) return;
        try {
            synchronized (serialIn) {
                switch (mode) {
                case TEXT_MODE: {
                    StringTokenizer command = new StringTokenizer(readResponse(), ":,");
                    String commandName = command.nextToken().trim();
                    if (commandName.equals("+CMT")) {
                        // Sms Deliver Message
                        String data = readResponse(),
                            originatingAddress = command.nextToken().trim(),
                            originatingAddressAlpha = command.nextToken().trim(),
                            serviceCentreTimeStamp = command.nextToken().trim(),
                            originatingAddressType = null,
                            firstOctet = null,
                            protocolIdentifer = null,
                            dataCodingScheme = null,
                            serviceCentreAddress = null,
                            serviceCentreAddressType = null,
                            messageBodyLength = null;
                        if (command.hasMoreTokens()) {
                            originatingAddressType = command.nextToken().trim();
                            firstOctet = command.nextToken().trim();
                            protocolIdentifer = command.nextToken().trim();
                            dataCodingScheme = command.nextToken().trim();
                            serviceCentreAddress = command.nextToken().trim();
                            serviceCentreAddressType = command.nextToken().trim();
                            messageBodyLength = command.nextToken().trim();
                        }
                        synchronized (messages) {
                            messages.addElement(new MultiDatagramPacket(data.getBytes(), data.length(), originatingAddress.getBytes(), -1));
                        }
                    } else if (commandName.equals("CBM+")) {
                        // Cell Broadcast Message
                        String data = readResponse(),
                            serialNumber = command.nextToken().trim(),
                            messageIdentifier = command.nextToken().trim(),
                            dataCodingScheme = command.nextToken().trim(),
                            pageParameterBits4_7 = command.nextToken().trim(),
                            pageParameterBits0_3 = command.nextToken().trim();
                        synchronized (messages) {
                            messages.addElement(new MultiDatagramPacket(data.getBytes(), data.length(), null, -1));
                        }
                    } else if (commandName.equals("CDS+")) {
                        // Sms Status Report
                        String firstOctet = command.nextToken().trim(),
                            messageReference = command.nextToken().trim(),
                            recipientAddress = command.nextToken().trim(),
                            recipientAddressType = command.nextToken().trim(),
                            serviceCentreTimeStamp = command.nextToken().trim(),
                            dischargeTime = command.nextToken().trim(),
                            status = command.nextToken().trim();
                        synchronized (messages) {
                            messages.addElement(new MultiDatagramPacket(messageReference.getBytes(), messageReference.length(), null, -1));
                        }
                    }
                    break;
                }
                case PDU_MODE: {
                    StringTokenizer command = new StringTokenizer(readResponse(), ":,");
                    String commandName = command.nextToken().trim();
                    if (commandName.equals("+CMT")) {
                        // Sms Deliver Message
                        String pdu = readResponse(),
                            originatingAddressAlpha = command.nextToken().trim(),
                            dataLength = command.nextToken().trim();
                        DataInputStream pduIn = new DataInputStream(new ByteArrayInputStream(decodeHex(pdu)));
                        int serviceCentreAddressLength = pduIn.readByte(),
                            serviceCentreAddressType = 0;
                        byte serviceCentreAddressValue[] = null;
                        if (serviceCentreAddressLength > 0) {
                            serviceCentreAddressType = pduIn.readByte();
                            serviceCentreAddressLength--;
                            serviceCentreAddressValue = readBCD(pduIn, serviceCentreAddressLength);
                        }
                        int firstOctet = pduIn.readByte(),
                            originatingAddressLength = pduIn.readByte(),
                            originatingAddressType = 0;
                        byte originatingAddressValue[] = null;
                        if (originatingAddressLength > 0) {
                            originatingAddressType = pduIn.readByte();
                            originatingAddressLength--;
                            originatingAddressValue = readBCD(pduIn, originatingAddressLength);
                        }
                        int protocolIdentifier = pduIn.readByte(),
                            dataCodingScheme = pduIn.readByte();
                        // Data Coding Scheme is examined following GSM 03.38
                        boolean compressed = false, sevenBits = false;
                        if ((dataCodingScheme & 0xc0) == 0) {
                            compressed = (dataCodingScheme & 0x20) != 0x0;
                            sevenBits = (dataCodingScheme & 0xc) == 0x0;
                        } else if ((dataCodingScheme & 0xf0) == 0xf0) sevenBits = (dataCodingScheme & 0x4) == 0x0;
                        byte serviceCentreTimeStamp[] = readBCD(pduIn, 7);
                        int userDataLength = pduIn.readByte();
                        byte userData[] = new byte[userDataLength];
                        readData(pduIn, userData, compressed, sevenBits);
                    } else if (commandName.equals("CBM+")) {
                        // Cell Broadcast Message
                        String pdu = readResponse(),
                            dataLength = command.nextToken().trim();
                        DataInputStream pduIn = new DataInputStream(new ByteArrayInputStream(decodeHex(pdu)));
                        int shortMessageReferenceValue = pduIn.readByte(),
                            serialNumber = pduIn.readShort(),
                            messageIdentifier = pduIn.readShort(),
                            dataCodingScheme = pduIn.readByte(),
                            pageParameter = pduIn.readByte(),
                            geographicalScope = (serialNumber >> 14) & 0x3,
                            messageCode = (serialNumber >> 4) & 0x3ff,
                            updateNumber = serialNumber & 0xf,
                            language = 0;
                        // Data Coding Scheme is examined following GSM 03.38
                        boolean compressed = false, sevenBits = false;
                        if ((dataCodingScheme & 0xf0) == 0x0) language = dataCodingScheme & 0xf;
                        else if ((dataCodingScheme & 0xc0) == 0x40) {
                            compressed = (dataCodingScheme & 0x20) != 0x0;
                            sevenBits = (dataCodingScheme & 0xc) == 0x0;
                        } else if ((dataCodingScheme & 0xf0) == 0xf0) sevenBits = (dataCodingScheme & 0x4) == 0x0;
                        byte contentData[] = new byte[82];
                        readData(pduIn, contentData, compressed, sevenBits);
                    } else if (commandName.equals("CDS+")) {
                        // Sms Status Report
                    }
                    break;
                }
                case BLOCK_MODE: {
                    seekMessage();
                    // Message Type
                    switch (readByte(serialIn)) {
                    case MT_INC_MESSAGE: {
                        // Short Message Info Element ID
                        switch (readByte(serialIn)) {
                        case SHORT_MESSAGE_DATA_SMS_INFO_ELEMENT_ID: {
                            int lengthOfShortMessageData = readByte(serialIn),
                                shortMessageReferenceValue = readByte(serialIn),
                                shortMessageStatus = readByte(serialIn),
                                serviceCentreAddressLength = readByte(serialIn),
                                serviceCentreAddressType = readByte(serialIn);
                            byte serviceCentreAddress[] = readBCD(serialIn, serviceCentreAddressLength);
                            int tpduType = readByte(serialIn);
                            switch (tpduType & 0x3) {
                            case SMS_DELIVER: {
                                int originatingAddressLength = readByte(serialIn),
                                    originatingAddressType = readByte(serialIn);
                                byte originatingAddress[] = readBCD(serialIn, originatingAddressLength);
                                int protocolIdentifier = readByte(serialIn),
                                    dataCodingScheme = readByte(serialIn);
                                // Data Coding Scheme is examined following GSM 03.38
                                boolean compressed = false, sevenBits = false;
                                if ((dataCodingScheme & 0xc0) == 0) {
                                    compressed = (dataCodingScheme & 0x20) != 0x0;
                                    sevenBits = (dataCodingScheme & 0xc) == 0x0;
                                } else if ((dataCodingScheme & 0xf0) == 0xf0) sevenBits = (dataCodingScheme & 0x4) == 0x0;
                                // Service Centre Time Stamp
                                skip(serialIn, 7);
                                int userDataLength = readByte(serialIn);
                                byte userData[] = new byte[userDataLength];
                                readData(serialIn, userData, compressed, sevenBits);
                                finishMessage();
                                synchronized (messages) {
                                    messages.addElement(new MultiDatagramPacket(userData, userData.length, originatingAddress, -1));
                                    notifyAll();
                                }
                                break;
                            }
                            case SMS_SUBMIT_REPORT: {
                                int failureCause = readByte(serialIn);
                                finishMessage();
                                break;
                            }
                            case SMS_STATUS_REPORT: {
                                int messageReference = readByte(serialIn),
                                    recipientAddressLength = readByte(serialIn),
                                    recipientAddressType = readByte(serialIn);
                                byte recipientAddress[] = readBCD(serialIn, recipientAddressLength);
                                // Service Center Time Stamp
                                skip(serialIn, 7);
                                // Discharge Time
                                skip(serialIn, 7);
                                int status = readByte(serialIn);
                                finishMessage();
                                break;
                            }
                            default:
                                skipMessage(serialIn);
                                break;
                            }
                            break;
                        }
                        case SHORT_MESSAGE_DATA_CBS_INFO_ELEMENT_ID: {
                            // Cell Broadcast message is examined following GSM 03.41
                            int shortMessageReferenceValue = readByte(serialIn),
                                serialNumber = readShort(serialIn),
                                messageIdentifier = readShort(serialIn),
                                dataCodingScheme = readByte(serialIn),
                                pageParameter = readByte(serialIn),
                                geographicalScope = (serialNumber >> 14) & 0x3,
                                messageCode = (serialNumber >> 4) & 0x3ff,
                                updateNumber = serialNumber & 0xf,
                                language = 0;
                            // Data Coding Scheme is examined following GSM 03.38
                            boolean compressed = false, sevenBits = false;
                            if ((dataCodingScheme & 0xf0) == 0x0) language = dataCodingScheme & 0xf;
                            else if ((dataCodingScheme & 0xc0) == 0x40) {
                                compressed = (dataCodingScheme & 0x20) != 0x0;
                                sevenBits = (dataCodingScheme & 0xc) == 0x0;
                            } else if ((dataCodingScheme & 0xf0) == 0xf0) sevenBits = (dataCodingScheme & 0x4) == 0x0;
                            byte contentData[] = new byte[82];
                            readData(serialIn, contentData, compressed, sevenBits);
                            finishMessage();
                            synchronized (messages) {
                                messages.addElement(new MultiDatagramPacket(contentData, contentData.length, null, -1));
                                notifyAll();
                            }
                            break;
                        }
                        default:
                            skipMessage(serialIn);
                            break;
                        }
                        break;
                    }
                    case MT_INSERT_SMS_COMPLETE: {
                        shortMessageReference = readByte(serialIn);
                        skipMessage(serialIn);
                        sendLock.release();
                        break;
                    }
                    case MT_INSERT_SMS_FAILURE: {
                        readByte(serialIn);
                        causeValue = readByte(serialIn);
                        RP_causeValue = (causeValue & 0x80) != 0 ? readByte(serialIn) : 0;
                        for (int c; (c = peekByte()) != -1;)
                            switch (c) {
                            case TP_FAILURE_CAUSE_INFORMATION_ELEMENT_ID: {
                                int length = readByte(serialIn);
                                TP_failureCause = 0;
                                while (length > 0) TP_failureCause = TP_failureCause << 8 | readByte(serialIn);
                                break;
                            }
                            case SHORT_MESSAGE_REFERENCE_INFO_ELEMENT_ID:
                                shortMessageReference = readByte(serialIn);
                                break;
                            case -1:
                                break;
                            }
                        sendLock.release();
                        break;
                    }
                    case MT_UNABLE_TO_PROCESS: {
                        readByte(serialIn);
                        causeValue = readByte(serialIn);
                        RP_causeValue = readByte(serialIn);
                        sendLock.release();
                        break;
                    }
                    case MT_REQUEST_CONFIRMED: {
                        readByte(serialIn);
                        confirmType = readByte(serialIn);
                        readByte(serialIn);
                        shortMessageReference = readByte(serialIn);
                        causeValue = RP_causeValue = 0;
                        sendLock.release();
                        break;
                    }
                    case MT_END_SMS_MODE:
                    default:
                        skipMessage(serialIn);
                        sendLock.release();
                    }
                }
                }
            }
        } catch (IOException ex) {
            if (verbose) ex.printStackTrace();
        }
    }

    public String readResponse(boolean single)
        throws IOException {
        String response = serialIn.readLine();
        if (!single) response = serialIn.readLine();
        if (DEBUG) System.out.println("response=" + response);
        if (response.startsWith("ERROR") || response.startsWith("+CME ERROR")) throw new IOException(response);
        return response;
    }

    public final String readResponse()
        throws IOException {
        return readResponse(false);
    }

    public void writeCommand(String command)
        throws IOException {
        if (DEBUG) System.out.println("Sending command " + command);
        serialOut.writeBytes(command + "\r");
        serialOut.flush();
    }

    public void sendCommand(String command)
        throws IOException {
        writeCommand(command);
        readResponse();
    }

    public void create(boolean stream)
        throws IOException {
        create(stream, -1);
    }

    public void create(boolean stream, int af)
        throws IOException {
        if (serialPort != null) return;
        if (DEBUG) System.out.println("create " + stream);
        Runtime.getRuntime().addShutdownHook(new MobileSocketShutdownHook());
        Exception ex = null;
        try {
            CommPortIdentifier cpi = CommPortIdentifier.getPortIdentifier(portName);
            serialPort = (SerialPort)cpi.open(getClass().getName(), 5000);
            serialPort.setSerialPortParams(baudRate != -1 ? baudRate : serialPort.getBaudRate(),
                                           dataBits != -1 ? dataBits : serialPort.getDataBits(),
                                           stopBits != -1 ? stopBits : serialPort.getStopBits(),
                                           parity != -1 ? parity : serialPort.getParity());
            if (flowControl != -1) serialPort.setFlowControlMode(flowControl);
            if (DEBUG) System.out.println("baudRate=" + serialPort.getBaudRate() +
                                          ",dataBits=" + serialPort.getDataBits() + ",stopBits=" + serialPort.getStopBits() +
                                          ",parity=" + serialPort.getParity() + ",flowControl=" + serialPort.getFlowControlMode());
        } catch (NoSuchPortException ex1) {
            ex = ex1;
        } catch (PortInUseException ex1) {
            ex = ex1;
        } catch (UnsupportedCommOperationException ex1) {
            ex = ex1;
        }
        if (ex != null) throw new IOException(Support.stackTrace(ex));
        serialIn = new DataInputStream(serialPort.getInputStream());
        serialOut = new DataOutputStream(serialPort.getOutputStream());
        // Disable echoing
        sendCommand("ATE0");
        // Enable responses
        sendCommand("ATQ0");
        // Use textual responses
        sendCommand("ATV1");
        // Use verbose error report
        sendCommand("AT+CMEE=2");
        // Select preferred message storage to phone memory
        sendCommand("AT+CPMS=\"SM\",\"SM\",\"SM\"");
        switch (mode) {
        case BLOCK_MODE: {
            // Enter SMS Block Mode Protocol
            writeCommand("AT+CESP");
            try {
                Thread.sleep(5000L);
            } catch (InterruptedException ex1) {
                Thread.currentThread().interrupt();
                return;
            }
            readResponse();
            ex = null;
            try {
                serialPort.addEventListener(this);
            } catch (TooManyListenersException ex1) {
                ex = ex1;
            }
            if (ex != null) throw new IOException(Support.stackTrace(ex));
            startMessage();
            writeByte(TE_TRANSFER_INC_SMS);
            writeByte(SMS_TRANSFER_TYPE_INFO_ELEMENT_ID);
            writeByte(0x7);
            stopMessage();
            checkSend();
            startMessage();
            writeByte(TE_TRANSFER_INC_CBS);
            writeByte(CBS_TRANSFER_TYPE_INFO_ELEMENT_ID);
            writeByte(0x3);
            stopMessage();
            checkSend();
            break;
        }
        case PDU_MODE: {
            sendCommand("AT+CMGF=0");
            break;
        }
        case TEXT_MODE: {
            // Message Format text
            sendCommand("AT+CMGF=1");
            // New Message Acknoledgement to ME/TA
            sendCommand("AT+CNMA");
            // Show Text Mode Parameters
            sendCommand("AT+CSDH");
            break;
        }
        }
    }

    public void connect(String host, int port)
        throws IOException {
    }

    public void connect(InetAddress address, int port)
        throws IOException {
    }

    public void connect(SocketAddress address, int timeout)
        throws IOException {
    }

    public void connect(byte addr[], int scopeId, int port)
        throws IOException {
    }

    public void connect(byte addr[], int scopeId, int port, int timeout)
        throws IOException {
    }

    public void bind(InetAddress address, int port)
        throws IOException {
    }

    public void bind(byte addr[], int scopeId, int port)
        throws IOException {
    }

    public void listen(int backlog)
        throws IOException {
    }

    public void accept(SocketImpl socketImpl)
        throws IOException {
    }

    public InputStream getInputStream()
        throws IOException {
        return null;
    }

    public OutputStream getOutputStream()
        throws IOException {
        return null;
    }

    public int read(byte buf[], int off, int len)
        throws IOException {
        return 0;
    }

    public void write(byte buf[], int off, int len)
        throws IOException {
    }

    public void receive(MultiDatagramPacket pack)
        throws IOException {
        switch (mode) {
        case BLOCK_MODE:
            synchronized (messages) {
                if (messages.isEmpty())
                    try {
                        wait(timeout);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                if (messages.isEmpty()) throw new InterruptedIOException();
                MultiDatagramPacket mdp = messages.firstElement();
                pack.setData(mdp.getData());
                pack.setLength(mdp.getLength());
                pack.setAddress(mdp.getAddress());
                messages.removeElementAt(0);
                break;
            }
        case PDU_MODE:
            synchronized (messages) {
                Vector<Integer> indices = new Vector<Integer>();
                if (messages.isEmpty())
                    for (boolean looped = false;; looped = true) {
                        writeCommand("AT+CMGL");
                        while (serialIn.available() > 0) {
                            String line = Support.readLine(serialIn);
                            if (!line.startsWith("+CMGL:")) break;
                            StringTokenizer st = new StringTokenizer(line, ":,");
                            st.nextToken();
                            indices.addElement(new Integer(st.nextToken()));
                            String pdu = Support.readLine(serialIn);
                            ByteArrayOutputStream bout = new ByteArrayOutputStream();
                            int l = pdu.length();
                            for (int i = 0; i < l; i += 2) bout.write(Integer.parseInt(pdu.substring(i, 2), 16));
                            ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
                            int serviceCentreAddressLength = readByte(bin),
                                serviceCentreAddressType = readByte(bin);
                            byte serviceCentreAddress[] = readBCD(bin, serviceCentreAddressLength);
                            int tpduType = readByte(bin);
                            switch (tpduType & 0x03) {
                            case SMS_DELIVER: {
                                int originatingAddressLength = readByte(bin),
                                    originatingAddressType = readByte(bin);
                                byte originatingAddress[] = readBCD(bin, originatingAddressLength);
                                int protocolIdentifier = readByte(bin),
                                    dataCodingScheme = readByte(bin);
                                // Data Coding Scheme is examined following GSM 03.38
                                boolean compressed = false, sevenBits = false;
                                if ((dataCodingScheme & 0xc0) == 0) {
                                    compressed = (dataCodingScheme & 0x20) != 0x0;
                                    sevenBits = (dataCodingScheme & 0xc) == 0x0;
                                } else if ((dataCodingScheme & 0xf0) == 0xf0) sevenBits = (dataCodingScheme & 0x4) == 0x0;
                                // Service Centre Time Stamp
                                skip(bin, 7);
                                int userDataLength = readByte(bin);
                                byte userData[] = new byte[userDataLength];
                                readData(bin, userData, compressed, sevenBits);
                                synchronized (messages) {
                                    messages.addElement(new MultiDatagramPacket(userData, userData.length, originatingAddress, -1));
                                }
                                break;
                            }
                            }
                        }
                        if (!messages.isEmpty()) break;
                        if (looped && timeout > 0) throw new InterruptedIOException();
                        try {
                            Thread.sleep(timeout > 0 ? timeout : pollingInterval);
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                Enumeration indexItems = indices.elements();
                while (indexItems.hasMoreElements()) sendCommand("AT+CMGD=" + indexItems.nextElement());
                MultiDatagramPacket mdp = messages.firstElement();
                pack.setData(mdp.getData());
                pack.setLength(mdp.getLength());
                pack.setAddress(mdp.getAddress());
                messages.removeElementAt(0);
                break;
            }
        case TEXT_MODE:
            synchronized (messages) {
                Vector<Integer> indices = new Vector<Integer>();
                if (messages.isEmpty())
                    for (boolean looped = false;; looped = true) {
                        if (DEBUG) System.out.println("Listing messages");
                        writeCommand("AT+CMGL=\"ALL\"");
                        while (serialIn.available() > 0) {
                            String response = readResponse();
                            if (DEBUG) System.out.println("response=" + response);
                            byte originatingAddress[] = null;
                            boolean inUserData = false;
                            StringTokenizer st = new StringTokenizer(response, "\r\n");
                            while (st.hasMoreTokens()) {
                                String line = st.nextToken();
                                if (DEBUG) System.out.println("line=" + line);
                                if (inUserData) {
                                    byte userData[] = line.getBytes();
                                    synchronized (messages) {
                                        messages.addElement(new MultiDatagramPacket(userData, userData.length, originatingAddress, -1));
                                    }
                                    inUserData = false;
                                } else {
                                    if (!line.startsWith("+CMGL:")) break;
                                    StringTokenizer st1 = new StringTokenizer(line, ":,");
                                    // header
                                    st1.nextToken();
                                    indices.addElement(new Integer(st1.nextToken().trim()));
                                    // stat
                                    st1.nextToken();
                                    originatingAddress = st1.nextToken().getBytes();
                                    inUserData = true;
                                }
                            }
                        }
                        if (!messages.isEmpty()) break;
                        if (looped && timeout > 0) throw new InterruptedIOException();
                        if (DEBUG) System.out.println("Sleeping for " + (timeout > 0 ? timeout : pollingInterval));
                        try {
                            Thread.sleep(timeout > 0 ? timeout : pollingInterval);
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                Enumeration indexItems = indices.elements();
                while (indexItems.hasMoreElements()) sendCommand("AT+CMGD=" + indexItems.nextElement());
                MultiDatagramPacket mdp = messages.firstElement();
                pack.setData(mdp.getData());
                pack.setLength(mdp.getLength());
                pack.setAddress(mdp.getAddress());
                messages.removeElementAt(0);
                break;
            }
        }
    }

    public void send(MultiDatagramPacket pack)
        throws IOException {
        int validityPeriod = -1;
        if (timeToLive > 0) {
            int minutes = timeToLive / 6000;
            if (minutes <= 720) validityPeriod = (minutes -= 5) > 0 ? minutes / 5 : -1;
            else if (minutes <= 1440) validityPeriod = 144 + ((minutes -= 750) > 0 ? minutes / 30 : 0);
            else if (minutes <= 43200) validityPeriod = 168 + ((minutes -= 2880) > 0 ? minutes / 1440 : 0);
            else validityPeriod = minutes <= 635040 ? 197 + ((minutes -= 50400) > 0 ? minutes / 10080 : 0) : 255;
        }
        switch (mode) {
        case BLOCK_MODE:
            synchronized (serialOut) {
                if (DEBUG) System.out.println("send " + pack);
                startMessage();
                writeByte(TE_INSERT_SMS);
                writeByte(INSERT_TYPE_INFO_ELEMENT_ID);
                // Send the short message over the air and/or store it in the phone
                writeByte(insertType);
                // Destination Address (SMSC), use default
                writeByte(0x0);
                // Message Type
                writeByte(SMS_SUBMIT | (validityPeriod != -1 ? VALIDITY_PERIOD_FORMAT : 0) |
                          (headerCoding == CellularSocket.BINARY_HEADERS ? USER_DATA_HEADER_INDICATOR : 0) |
                          (notifyMode == 1 ? STATUS_REPORT_REQUEST : 0));
                // Message Reference
                writeByte(0);
                // Destination Address Length
                writeByte(pack.getAddress().length);
                // Destination Address Type
                writeByte(INTERNATIONAL_NUMBER | ISDN_NUMBERING_PLAN);
                // Destination Address
                writeBCD(pack.getAddress());
                // Protocol Identifier
                writeByte(0x0);
                // Data Coding Scheme
                switch (dataCoding) {
                case CellularSocket.UNICODE_DATA_CODING:
                    writeByte(0x8);
                    break;
                case CellularSocket.BINARY_DATA_CODING:
                    writeByte(0x4);
                    break;
                default:
                    writeByte(0x0);
                    break;
                }
                // Validity Period
                if (validityPeriod != -1) writeByte(validityPeriod);
                int length = pack.getLength();
                // User Data Length
                writeByte(length);
                byte data[] = pack.getData();
                // User Data
                if (dataCoding == CellularSocket.ASCII_DATA_CODING) {
                    int lastChar = 0, remainingBits = 0;
                    for (int i = 0; i <= length; i++) {
                        int newChar = i < length ? data[i] & 0xff : 0;
                        if (remainingBits > 0) {
                            writeByte((lastChar >> (7 - remainingBits)) | (newChar << remainingBits));
                            remainingBits--;
                        } else remainingBits = 7;
                        lastChar = newChar;
                    }
                } else for (int i = 0; i < length; i++) writeByte(data[i]);
                stopMessage();
                checkSend();
                break;
            }
        case PDU_MODE:
            synchronized (serialOut) {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                // Destination Address (SMSC), use default
                bout.write(0x0);
                // Message Type
                bout.write(SMS_SUBMIT | (validityPeriod != -1 ? VALIDITY_PERIOD_FORMAT : 0) |
                           (headerCoding == CellularSocket.BINARY_HEADERS ? USER_DATA_HEADER_INDICATOR : 0) |
                           (notifyMode == 1 ? STATUS_REPORT_REQUEST : 0));
                // Message Reference
                bout.write(0);
                // Destination Address Length
                bout.write(pack.getAddress().length);
                // Destination Address Type
                bout.write(INTERNATIONAL_NUMBER | ISDN_NUMBERING_PLAN);
                // Destination Address
                writeBCD(bout, pack.getAddress());
                // Protocol Identifier
                bout.write(0x0);
                // Data Coding Scheme
                switch (dataCoding) {
                case CellularSocket.UNICODE_DATA_CODING:
                    bout.write(0x8);
                    break;
                case CellularSocket.BINARY_DATA_CODING:
                    bout.write(0x4);
                    break;
                default:
                    bout.write(0x0);
                    break;
                }
                // Validity Period
                if (validityPeriod != -1) bout.write(validityPeriod);
                int length = pack.getLength();
                // User Data Length
                bout.write(length);
                byte data[] = pack.getData();
                // User Data
                if (dataCoding == CellularSocket.ASCII_DATA_CODING) {
                    int lastChar = 0, remainingBits = 0;
                    for (int i = 0; i <= length; i++) {
                        int newChar = i < length ? data[i] & 0xff : 0;
                        if (remainingBits > 0) {
                            bout.write((lastChar >> (7 - remainingBits)) | (newChar << remainingBits));
                            remainingBits--;
                        } else remainingBits = 7;
                        lastChar = newChar;
                    }
                } else for (int i = 0; i < length; i++) bout.write(data[i]);
                data = bout.toByteArray();
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < data.length; i++)
                    sb.append(Character.toUpperCase(Character.forDigit(data[i] >> 4 & 0xf, 16))).append(Character.toUpperCase(Character.forDigit(data[i] & 0xf, 16)));
                writeCommand("AT+CMGW=" + (data.length - 1));
                if (DEBUG) System.out.println("trying to send");
                readResponse(true);
                String pdu = sb.toString();
                if (DEBUG) System.out.println("pdu=" + pdu);
                serialOut.writeBytes(pdu);
                serialOut.write(0x1a);
                serialOut.flush();
                readResponse();
                readResponse();
                if (DEBUG) System.out.println("sent");
                break;
            }
        case TEXT_MODE: {
            writeCommand("AT+CMGW=\"" + new String(pack.getAddress()) + "\"");
            if (DEBUG) System.out.println("trying to send");
            readResponse(true);
            serialOut.write(pack.getData());
            serialOut.write(0x1a);
            serialOut.flush();
            readResponse();
            readResponse();
            if (DEBUG) System.out.println("sent");
            break;
        }
        }
    }

    public int available()
        throws IOException {
        return 0;
    }

    public void flush()
        throws IOException {
    }

    public void setTimeToLive(int ttl) {
        timeToLive = ttl;
    }

    public int getTimeToLive() {
        return timeToLive;
    }

    public void close()
        throws IOException {
    }

    public void shutdownInput()
        throws SocketException {
    }

    public void shutdownOutput()
        throws SocketException {
    }

    public void sendUrgentData(int data)
        throws IOException {
    }

    public void setOption(int optID, Object value)
        throws SocketException {
        switch (optID) {
        case SO_TIMEOUT:
            if (value == null || !(value instanceof Integer))
                throw new SocketException("Bad parameter for SO_TIMEOUT");
            timeout = ((Integer)value).intValue();
            return;
        }
    }

    public Object getOption(int optID)
        throws SocketException {
        switch (optID) {
        case SO_TIMEOUT:
            return new Integer(timeout);
        }
        return null;
    }

    public void setIOControl(int code, int value)
        throws SocketException {
        switch (code) {
        case MultiSocketImpl.NBS_NOTIFYMODE:
            notifyMode = value;
            return;
        case MultiSocketImpl.NBS_DATACODING:
            dataCoding = value;
            return;
        case MultiSocketImpl.NBS_VALIDITY:
            timeToLive = value;
            return;
        case MultiSocketImpl.NBS_SETHEADERCODING:
            headerCoding = value;
            return;
        }
        throw new SocketException("Unsupported IO control");
    }

    public int getIOControl(int code)
        throws SocketException {
        switch (code) {
        case MultiSocketImpl.NBS_NOTIFYMODE:
            return notifyMode;
        case MultiSocketImpl.NBS_DATACODING:
            return dataCoding;
        case MultiSocketImpl.NBS_VALIDITY:
            return timeToLive;
        case MultiSocketImpl.NBS_SETHEADERCODING:
            return headerCoding;
        }
        throw new SocketException("Unsupported IO control");
    }

    public boolean isClosed() {
        return false;
    }

    public boolean isConnected() {
        return true;
    }

    protected void finalize() {
        try {
            close();
        } catch (IOException ex) {}
    }

    public static void main(String argv[])
        throws Exception {
        String recipientAddress = null, message = null;
        boolean binaryHeaders = false, invalidOption = false, simulation = false;
        int port = 0, dataCoding = CellularSocket.ASCII_DATA_CODING, timeToLive = 0, timeout = 0;
        MobileSocketImpl.verbose = true;
        for (int argn = 0; argn < argv.length; argn++) {
            String arg = argv[argn];
            if (arg.charAt(0) == '-')
                for (int i = 1; i < arg.length(); i++)
                    switch (Character.toLowerCase(arg.charAt(i))) {
                    case 'c':
                        MobileSocketImpl.portName = argv[++argn];
                        continue;
                    case 'b':
                        MobileSocketImpl.setBaudRate(Integer.parseInt(argv[++argn]));
                        continue;
                    case 'd':
                        MobileSocketImpl.setDataBits(Integer.parseInt(argv[++argn]));
                        continue;
                    case 's':
                        MobileSocketImpl.setStopBits(Integer.parseInt(argv[++argn]));
                        continue;
                    case 'p':
                        MobileSocketImpl.setParity(argv[++argn].charAt(0));
                        continue;
                    case 'f':
                        MobileSocketImpl.setFlowControl(argv[++argn].charAt(0));
                        continue;
                    case 'h':
                        binaryHeaders = true;
                        continue;
                    case 't':
                        timeToLive = Integer.parseInt(argv[++argn]);
                        continue;
                    case 'o':
                        timeout = Integer.parseInt(argv[++argn]);
                        continue;
                    case 'u':
                        dataCoding = CellularSocket.UNICODE_DATA_CODING;
                        continue;
                    case 'y':
                        dataCoding = CellularSocket.BINARY_DATA_CODING;
                        continue;
                    case 'r':
                        port = Integer.parseInt(argv[++argn]);
                        continue;
                    case 'm':
                        simulation = true;
                        continue;
                    default:
                        i = arg.length();
                        argn = argv.length;
                        invalidOption = true;
                        break;
                    } else if (recipientAddress == null) recipientAddress = arg;
            else message = arg;
        }
        if (recipientAddress == null || invalidOption) {
            System.out.println("Parameters: recipientAddress [message]\n" +
                               "Message can be given also from standard input.\n" +
                               "Available options: (default value in parentheses, alternatives after colon):\n" +
                               "-c <comm port name> (COM1)\n" +
                               "-b <baud rate> (115200)\n" +
                               "-d <number of data bits> (8): 5, 6, 7, 8\n" +
                               "-s <number of stop bits> (1): 1, 2, 3\n" +
                               "-p <parity> (none): n(one), o(dd), e(ven), m(ark), s(pace)\n" +
                               "-f <flow control> (none): n(one), r(ts/cts), x(on/xoff)\n" +
                               "-h: use binary headers\n" +
                               "-t <time to live in ms> (0, no limit)\n" +
                               "-o <timeout in ms> (0, no limit)\n" +
                               "-u: use unicode data coding\n" +
                               "-y: use binary data coding\n" +
                               "-r <port> (0)\n" +
                               "-m: simulation using standard I/O");
            System.exit(1);
        }
        if (DEBUG) System.out.println("message="+message);
        if (message == null) {
            StringBuffer sb = new StringBuffer();
            for (int c; (c = System.in.read()) != -1;) sb.append((char)c);
            message = sb.toString();
        }
        byte data[] = message.getBytes();
        ByteArrayOutputStream bout = null;
        MultiDatagramPacket packet = new MultiDatagramPacket(data, data.length, recipientAddress.getBytes(), port);
        MobileSocketImpl mobile = new MobileSocketImpl();
        if (DEBUG) mobile.insertType = STORE_MESSAGE;
        if (simulation) {
            serialIn = new DataInputStream(System.in);
            serialOut = new DataOutputStream(bout = new ByteArrayOutputStream());
        } else mobile.create(false);
        if (timeToLive > 0) mobile.setTimeToLive(timeToLive);
        if (timeout > 0) mobile.setOption(SO_TIMEOUT, new Integer(timeout));
        mobile.setIOControl(MultiSocketImpl.NBS_DATACODING, dataCoding);
        if (binaryHeaders) mobile.setIOControl(MultiSocketImpl.NBS_SETHEADERCODING, CellularSocket.BINARY_HEADERS);
        mobile.send(packet);
        System.out.println("Waiting short messages");
        HexDumpEncoder hexDump = new HexDumpEncoder();
        if (simulation) hexDump.encodeStream(bout.toByteArray(), System.out);
        for (;;) {
            mobile.receive(packet);
            System.out.println("Received packet from " + new String(packet.getAddress()));
            hexDump.encodeStream(packet.getData(), System.out);
        }
    }

}

