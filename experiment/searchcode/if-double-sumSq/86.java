package neuralgui;


public class Neural {
    private double input [];

    private double wt_ih [][];
    private double hidden [];
    private double wt_ho [][];
    private double output [];
    private double desired[];

    private double delta_o[];
    private double delta_h[];

    private int INCNT=144;
    private int HIDCNT=40;
    private int OUTCNT=12;
    private double n_ETA=0.01;

    private int ITERATIONS=72;

    private Pattern[] pattern;
    private Pattern curPattern;

    int repUnmatched=0;
    int repMatched=0;
    int repMismatched=0;

    double RMS_LIMIT=0.08;

    public void train(boolean useIterations) {
        if (useIterations) {
            trainIterations();
        } else {
            trainRMS();
        }
    }

    //returns array with Matched/Total files/Unmatched/Mismatched
    public int[] test() {
        int []answer=new int[4];
        int i;
        repUnmatched=0;
        repMatched=0;
        repMismatched=0;
        for (i=0;i<pattern.length;i++) {
            setPattern(pattern[i]);
            remember();
            reportMatch();
        }
        answer[0]=repMatched;
        answer[1]=pattern.length;
        answer[2]=repUnmatched;
        answer[3]=repMismatched;
        return answer;
    }

    private void trainIterations() {
        int i;
        for (i=0;i<ITERATIONS;i++) {
            setPattern(pattern[i % 12]);
            learn();
        }
    }

    private void trainRMS() {
        //NOT DONE YET
        int i=-1;
        double currentRms;
        do {
            i++;
            setPattern(pattern[i % 12]);
            learn();
            currentRms=rms();
        }
        while (currentRms>RMS_LIMIT);
        //rem out line when working
        System.out.printf("took %d iterations to reach desired RMS of %.2f\n",
                    i,currentRms);
    }


    public void setIterations(int it) {ITERATIONS=it;}
    public int getIterations() {return ITERATIONS;}
    public void setHiddenCount(int hc) {
        HIDCNT=hc;
        initArrays();
        init_wt();
        if (curPattern !=null)
            setPattern(curPattern);
    }
    public int getHiddenCount() {return HIDCNT;}
    public void setRmsLimit(double aRmsLimit) { RMS_LIMIT=aRmsLimit; }
    public double getRmsLimit() {return RMS_LIMIT;}

/*
    public static void main(String args[]) {
        Neural m=new Neural();
        m.main2();
    }
*/
    
    public void main2() {
        /*int i;
        for (i=0;i<ITERATIONS;i++) {
            setPattern(pattern[i % 12]);
            learn();
        }
        repUnmatched=0;
        repMatched=0;
        repMismatched=0;
        for (i=0;i<pattern.length;i++) {
            setPattern(pattern[i]);
            remember();
            reportMatch();
        }
        System.out.printf("Matched=%d / %d\n",repMatched,pattern.length);
        System.out.printf("Unmatched=%d\n",repUnmatched);
        System.out.printf("Mismatched=%d\n",repMismatched);
        */
        int a[];
        train(false); //train by iterations==true
        a=test();
        System.out.printf("Matched=%d / %d\n",a[0],a[1]);
        System.out.printf("Unmatched=%d\n",a[2]);
        System.out.printf("Mismatched=%d\n",a[3]);
        System.out.printf("RMS=%.4f\n",rms() );
    }

    public Neural() {
        initArrays();
        init_wt();
        pattern=Pattern.defaultPatterns();
    }
    private void reportMatch() {
        int i;
        boolean m=false;
        for (i=0;i<output.length;i++) {
            if (i==curPattern.getMatch()) {
                if (output[i]>0.5) m=true;
            } else {
                if (output[i]>0.5) repMismatched++;
            }
        }
        if (m) repMatched++;
        else repUnmatched++;
    }


    private double squash(double x) {
        return 1.0/(1.0+Math.exp(-x));
    }

    private void init_wt() {
        int i,j;
        wt_ih=new double[INCNT][HIDCNT];
        for (i=0;i<INCNT;i++) {
            //wt_ih[i]=new double[HIDCNT];
            for (j=0;j<HIDCNT;j++) {
                wt_ih[i][j]=1.0*Math.random()-0.5;
            }
        }
        wt_ho=new double[HIDCNT][OUTCNT];
        for (i=0;i<HIDCNT;i++) {
            //wt_ho[i]=new double[OUTCNT];
            for (j=0;j<OUTCNT;j++) {
                wt_ho[i][j]=1.0*Math.random()-0.5;
            }
        }
    }


    private void initArrays() {
        input = new double [INCNT];

        hidden = new double [HIDCNT];
        output = new double [OUTCNT];

        delta_o = new double [OUTCNT];
        delta_h = new double [HIDCNT];
        desired = new double [OUTCNT];
    }


    private void forwardPropogate(double []layer1, double []layer2, double [][]wt) {
        int i,j;
        double t;
        for (i=0;i<layer2.length;i++) {
            t=0.0;
            for (j=0;j<layer1.length;j++) {
                t+=wt[j][i]*layer1[j];
            }
            layer2[i]=squash(t);
        }
    }

    private void backPropogate(double[] layer1, double[] delta2, double [][]wt) {
        int i;
        int j;
        for (i=0;i<layer1.length;i++) {
            for (j=0;j<delta2.length;j++) {
		//System.out.println(wt[i][j]);
                wt[i][j]+=n_ETA*layer1[i]*delta2[j];
		//System.out.println(delta2[j]);
		//System.out.println(wt[i][j]);
            }
        }
    }

    private void makeDeltaOut() {
        int i;
        for (i=0;i<delta_o.length;i++) {
            delta_o[i]=output[i]*(1.0-output[i])*(desired[i]-output[i]);
        }
    }

    private void makeDeltaHidden() {
        int i,j;
        double del;
        for (i=0;i<hidden.length;i++) {
            del=0.0;
            for (j=0;j<output.length;j++) {
                del+=wt_ho[i][j]*
                    delta_o[j];
            }
            delta_h[i]=del*hidden[j]*(1.0-hidden[j]);
        }
    }

    // 1 iteration to teach currently set pattern
    public void learn() {
        forwardPropogate(input, hidden, wt_ih);
        forwardPropogate(hidden, output, wt_ho);
        makeDeltaOut();
        makeDeltaHidden();
        backPropogate(hidden, delta_o, wt_ho);
        backPropogate(input, delta_h, wt_ih);
    }


    // try to remember currently set pattern
    public void remember() {
        forwardPropogate(input, hidden, wt_ih);
        forwardPropogate(hidden, output, wt_ho);
    }

    public double rms() {
        double sumsq=0.0;
        double delta;
        int i,j;
        for (i=0;i<12;i++) {
            setPattern(pattern[i]);
            remember();
            for (j=0;j<OUTCNT;j++) {
                delta=desired[j]-output[j];
                sumsq+=delta*delta;
            }
        }
        return Math.sqrt(sumsq/(12.0*OUTCNT));
    }


    public void setPattern(Pattern p) {
        boolean []b=p.getPixel();
        int id=p.getId();
        int i;
        curPattern=p;
        for (i=0;i<input.length;i++) {
            input[i]= b[i] ? 1.0 : 0.0;
        }
        for (i=0;i<desired.length;i++) {
            desired[i]=(i==id) ? 1.0 : 0.0;
        }
    }

}


/*
class Pattern {
    boolean pixel[];
    int id;
    String filename;

    int getId() {return id;}
    boolean[] getPixel() {return pixel;}

}
*/

