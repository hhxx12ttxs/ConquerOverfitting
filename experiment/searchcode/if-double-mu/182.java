//
// Berlin Brown
//
// $Id: NeuralTestE.java,v 1.1 2004/05/06 13:30:29 bigbinc Exp $
//
//
//
// [ Decimal Format ]
// "#,##,###,####" == "######,####" == "##,####,####".
//


package org.retro.gis;

import org.retro.neural.*;
import java.util.Random;

import java.util.Random;
import java.text.*;


public class NeuralTestE {

    public static void main(String [] args) {

        if (args.length < 4) {
            System.err.println("Usage: java NeuralTestE{Caudill} <nhid> <epochs> <eta> " + 
                               "<mu> [seed]");

            System.exit(1);
        }
        int nhid = Integer.parseInt(args[0]);
        int nep = Integer.parseInt(args[1]);
        double eta = Double.parseDouble(args[2]);
        double mu = Double.parseDouble(args[3]);

        Random r = args.length > 4 ? 
            new Random(Long.parseLong(args[4])) :
            new Random();

        double [][] inpat1 = {{1,0,0}, {0,1,0}, {0,0,1}};
        double [][] tgpat1 = {{0.25}, {0.0}, {1.0}};


	// [ establish neurons ]
        BPLayer inp = new BPLayer(3);
        BPLayer out = new BPLayer(1);
        BPLayer hid = new BPLayer(nhid);

        BPLayer hid2 = new BPLayer(nhid);
        BPLayer out2 = new BPLayer(1);

        hid.connect(inp);
        out.connect(hid);

        hid2.delay(hid);
        out2.delay(out);

        hid.connect(hid2);
        out.connect(out2);

        hid.randomize(r);
        out.randomize(r);


        hid.resetMu();
        out.resetMu();

        System.err.println("Epoch\tRMS Error\n");

        for (int i=0; i<nep; ++i) {

            hid.bpttResetEta();
            out.bpttResetEta();

            double rmserr = 0;

            rmserr += bptt_pattern(inp, out, inpat1, tgpat1);
            BPLayer.reportValue(i, nep, 1000, rmserr/2, System.out);

            out.bpttUpdate(eta, mu, 2);
            hid.bpttUpdate(eta, mu, 2);

        } // end of the for .................................

        System.out.println("\nTARGET\tACTUAL\n");
        test(inp, out, inpat1, tgpat1);

        System.out.println();
	System.out.println("---------------------------------------");
	System.out.println();

    } // end of the method ...

    private static double bptt_pattern(BPLayer inp, BPLayer out, 
                                       double [][] inpat, double [][] tgpat) {

        inp.attach(inpat);
        out.attach(tgpat);

        out.bpttPattern();
        
        return out.getRMSError();
    } // end of the method . .....

    private static void test(BPLayer inp, BPLayer out, 
                             double [][] inpat, double [][] tgpat) {

        inp.attach(inpat);
        double [][] outpat = out.test();

        for (int i=0; i<outpat.length; ++i) {

            for (int j=0; j<outpat[i].length; ++j) {
		
		double _d1 = -1;
		double _d2 = -1;

		_d1 = tgpat[i][j];
		_d2 = outpat[i][j];

		DecimalFormat df = new DecimalFormat("####.####");
		String _sd1 = df.format(_d1);
		String _sd2 = df.format(_d2);

		System.out.print(_sd1 + "\t" + _sd2);

            } // end of the for ...

            System.out.println();
        } // end of the for ...

    } // end of the method ....


} // end of the method .............

