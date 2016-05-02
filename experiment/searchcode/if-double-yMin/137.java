package wavedj;

import function.*;
import java.io.*;

public class StreamGenerator extends InputStream {
    
    int wP = 0, rP = 0, ds = 44 + 8000;
    byte[] data = new byte[ds];
    double[] ddata = new double[8000];
    FunctionParser parser;
    Function func;

    StreamGenerator(String strFunc) {
        parser = new FunctionParser(strFunc);
    }
    
    void generate() throws ParserException {
        func = parser.parse();
        genHeader();
        genSound();
    }
    
    void genHeader() {
        add("RIFF");
        addInt(36 + 8000);
        add("WAVE");
        
        add("fmt ");
        addInt(16);
        addShort((short) 1);
        addShort((short) 1);
        addInt(8000);
        addInt(8000);
        addShort((short) 1);
        addShort((short) 8);
        
        add("data");
        addInt(8000);
    }
    
    void genSound() {
        double t = 0.0, dt = 1.0 / 8000;
        double ymin = 0.0, ymax = 0.0;
        for (int i = 0; i < 8000; i++, t += dt) {
            double y = func.eval(t);
            ddata[i] = y;
            if (y < ymin)
                ymin = y;
            if (y > ymax)
                ymax = y;
        }
        double dy = ymax - ymin;
        for (int i = 0; i < 8000; i++) {
            double sy = ddata[i] - ymin;
            data[wP++] = (byte)(sy * 127 / dy);
        }
    }
    
    void add(String s) {
        for (int i = 0; i < s.length(); i++)
            addByte((byte) s.charAt(i));
    }
    
    void addByte(byte x) {
        data[wP++] = x;
    }
    
    void addShort(short x) {
        for (int j = 0; j < 16; j += 8)
            data[wP++] = (byte)(x >> j);
    }
    
    void addInt(int x) {
        for (int j = 0; j < 32; j += 8)
            data[wP++] = (byte)(x >> j);
    }

    public int read() {
        if (rP == ds)
            return -1;
        return data[rP++];
    }
    
}

