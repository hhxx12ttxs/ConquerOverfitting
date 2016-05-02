//
// Berlin Brown
//
// $Id: NeuralTestD.java,v 1.2 2004/05/05 12:34:45 bigbinc Exp $
//
//

package org.retro.gis;

import org.retro.neural.*;
import java.util.Random;

public class NeuralTestD {
     
    private BPLayer inp, hid, out;
    private int nUnits;

    public NeuralTestD(int nHid){
        this(nHid, new Random());
    } // end of the method .....
        

    public NeuralTestD(int nHid, long seed){
        this(nHid, new Random(seed));
    } // end of the method 
        
    public NeuralTestD(int nHid, Random r){
       
        nUnits = 1 << nHid;     

        inp = new BPLayer(nUnits);
        hid = new BPLayer(nHid);
        out = new BPLayer(nUnits);

        hid.connect(inp);
        out.connect(hid);

        hid.randomize(r);
        out.randomize(r);
    } // end of the method 

    public void train (int nIter, double eta, double mu) {

        double [][] pats = get_patterns();
        
        inp.attach(pats);
        out.attach(pats);
        out.batch(nIter, eta, mu);;
    } // end of the method 


    public void test(){

        double [][] pats = get_patterns();
        inp.attach(pats);
        out.attach(pats);
        double [][] outp = out.test();
        for (int i=0; i<outp.length; ++i) {
            for (int j=0; j<outp[i].length; ++j) {
                double o = outp[i][j];
                // threshold
                if (o < .2) {
                    o = 0;
                }
                else if (o > 0.8) {
                    o = 1;
                }
                System.out.print(o + " ");
            }
            System.out.println();
        }
    } // end of the method ....

    public double rmserr() {
        return out.getRMSError();
    }

    // set weights to specified values
    public void setWeights(double [][] who, double [][] wih,
                           double [] bh, double [] bo) {
        hid.setWeights(inp, wih);
        out.setWeights(hid, who);
        hid.setBias(bh);
        out.setBias(bo);
    }

    // return 1-in-N codes
    private double [][] get_patterns() {

        double [][] codes = new double [nUnits][nUnits];
        for (int i=0; i<nUnits; ++i) {
            codes[i][i] = 1;
        }
        return codes;
    }

    public static void main(String [] args) {

        if (args.length < 3) {
            System.err.println("Usage: java Autoasso <nHid> <nIter> " +
                               "<eta> <mu> [seed]");
            System.exit(1);
        }
        int nHid = Integer.parseInt(args[0]);
        int nIter = Integer.parseInt(args[1]);
        double eta = Double.parseDouble(args[2]);
        double mu = Double.parseDouble(args[3]);

        NeuralTestD auto = args.length > 4 ? 
            new NeuralTestD(nHid, Long.parseLong(args[4])) :
            new NeuralTestD(nHid);

        // train network
        auto.train(nIter, eta, mu);  
        
        // test network
        auto.test();
    } // end of the method 


} // end of the class ..

