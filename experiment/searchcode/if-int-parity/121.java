import javax.comm.SerialPort;

/*
 * Created on 10/03/2004
 */

/**
 * Classe para configura??o da Porta Serial Seguindo exemplo de SerialParameters
 * do SerialDemo
 */
public class SerialParameters {

    private String portName;

    private int bitsPorSegundo;

    private int bitsDeDados;

    private int paridade;

    private int stopBit;

    private int controleFluxo;

    /**
     * Configura??o padr?o
     */
    public SerialParameters() {
        this.portName = "COM1";
        this.bitsPorSegundo = 9600;
        this.bitsDeDados = SerialPort.DATABITS_8;
        this.paridade = SerialPort.PARITY_NONE;
        this.stopBit = SerialPort.STOPBITS_1;
        this.controleFluxo = SerialPort.FLOWCONTROL_XONXOFF_OUT;
    }

    public SerialParameters(String name, int baud, int data, int parity,
            int stop, int fluxo) {
        this.portName = name;
        this.bitsPorSegundo = baud;
        this.bitsDeDados = data;
        this.paridade = parity;
        this.stopBit = stop;
        this.controleFluxo = fluxo;
    }

    public int getBitsDeDados() {
        return bitsDeDados;
    }

    public int getBitsPorSegundo() {
        return bitsPorSegundo;
    }

    public int getControleFluxo() {
        return controleFluxo;
    }

    public int getParidade() {
        return paridade;
    }

    public String getPortName() {
        return portName;
    }

    public int getStopBit() {
        return stopBit;
    }

    public void setBitsDeDados(int i) {
        bitsDeDados = i;
    }

    public void setBitsPorSegundo(int i) {
        bitsPorSegundo = i;
    }

    public void setControleFluxo(int i) {
        controleFluxo = i;
    }

    public void setControleFluxo(String s) {
        controleFluxo = stringToFlow(s);
    }

    public void setParidade(int i) {
        paridade = i;
    }

    public void setPortName(String string) {
        portName = string;
    }

    public void setStopBit(int i) {
        stopBit = i;
    }

    private int stringToFlow(String flowControl) {
        if (flowControl.equals("None")) { return SerialPort.FLOWCONTROL_NONE; }
        if (flowControl.equals("Xon/Xoff Out")) { return SerialPort.FLOWCONTROL_XONXOFF_OUT; }
        if (flowControl.equals("Xon/Xoff In")) { return SerialPort.FLOWCONTROL_XONXOFF_IN; }
        if (flowControl.equals("RTS/CTS In")) { return SerialPort.FLOWCONTROL_RTSCTS_IN; }
        if (flowControl.equals("RTS/CTS Out")) { return SerialPort.FLOWCONTROL_RTSCTS_OUT; }
        return SerialPort.FLOWCONTROL_NONE;
    }

    String flowToString(int flowControl) {
        switch (flowControl) {
        case SerialPort.FLOWCONTROL_NONE:
            return "None";
        case SerialPort.FLOWCONTROL_XONXOFF_OUT:
            return "Xon/Xoff Out";
        case SerialPort.FLOWCONTROL_XONXOFF_IN:
            return "Xon/Xoff In";
        case SerialPort.FLOWCONTROL_RTSCTS_IN:
            return "RTS/CTS In";
        case SerialPort.FLOWCONTROL_RTSCTS_OUT:
            return "RTS/CTS Out";
        default:
            return "None";
        }
    }

    public void setParity(String parity) {
        if (parity.equals("None")) {
            this.paridade = SerialPort.PARITY_NONE;
        }
        if (parity.equals("Par")) {
            this.paridade = SerialPort.PARITY_EVEN;
        }
        if (parity.equals("Impar")) {
            this.paridade = SerialPort.PARITY_ODD;
        }
    }

    public String getParityString() {
        switch (paridade) {
        case SerialPort.PARITY_NONE:
            return "None";
        case SerialPort.PARITY_EVEN:
            return "Par";
        case SerialPort.PARITY_ODD:
            return "Impar";
        default:
            return "None";
        }
    }
}
