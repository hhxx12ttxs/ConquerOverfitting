package de.bwaldvogel.liblinear;

import static de.bwaldvogel.liblinear.Linear.atof;
import static de.bwaldvogel.liblinear.Linear.atoi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.apache.mahout.classifier.evaluation.Auc;


public class Train1 {
	private double auc ;
    public static void main(String[] args) throws IOException, InvalidInputDataException {
        new Train1().run(args);
    }

    private double    bias             = 1;
    private boolean   cross_validation = false;
    private String    inputFilename;
    private String    modelFilename;
    private int       nr_fold;
    private Parameter param            = null;
    private Problem   prob             = null;
    
    private double do_cross_validation_AUC() {

        double[] target = new double[prob.l];

        long start, stop;
        start = System.currentTimeMillis();
        Linear.crossValidationWithProbabilistic(prob, param, nr_fold,target);
        stop = System.currentTimeMillis();
        
   
        //AUC
        Auc x1 = new Auc();
        x1.setProbabilityScore(false);
        for (int i  = 0; i  < prob.l; i ++){
        	 double score = target[i];
        	 int label = (int) prob.y[i];
        	 if (label==-1) {
				label=0;
			 }
        	 x1.add(label, score);
        }
        
        double auc = x1.aucShaoweng();
        StringBuilder sb = null;
        
        int ps = 0;
        if (auc < 0.1) {
        	sb = new StringBuilder();
        	 for (int i  = 0; i  < prob.l; i ++){
            	 double score = target[i];
            	 int label = (int) prob.y[i];
            	 if (label==0) {
    				ps++;
    			 }
            	 x1.add(label, score);
            	 sb.append(score).append("\t").append(label).append("\n");
            }
        	System.out.println("auc = " + auc + " negative = "+ ps + "  positive = "+(prob.y.length - ps) );
        	System.out.println(sb.toString());
		}
		return auc;
    }
    
    private void do_cross_validation() {

        double total_error = 0;
        double sumv = 0, sumy = 0, sumvv = 0, sumyy = 0, sumvy = 0;
        double[] target = new double[prob.l];

        long start, stop;
        start = System.currentTimeMillis();
        Linear.crossValidation(prob, param, nr_fold, target);
        stop = System.currentTimeMillis();
        System.out.println("time: " + (stop - start) + " ms");

        if (param.solverType.isSupportVectorRegression()) {
            for (int i = 0; i < prob.l; i++) {
                double y = prob.y[i];
                double v = target[i];
                total_error += (v - y) * (v - y);
                sumv += v;
                sumy += y;
                sumvv += v * v;
                sumyy += y * y;
                sumvy += v * y;
            }
            System.out.printf("Cross Validation Mean squared error = %g%n", total_error / prob.l);
            System.out.printf("Cross Validation Squared correlation coefficient = %g%n", //
                ((prob.l * sumvy - sumv * sumy) * (prob.l * sumvy - sumv * sumy)) / ((prob.l * sumvv - sumv * sumv) * (prob.l * sumyy - sumy * sumy)));
        } else {
            int total_correct = 0;
            for (int i = 0; i < prob.l; i++)
                if (target[i] == prob.y[i]) ++total_correct;

            System.out.printf("correct: %d%n", total_correct);
            System.out.printf("Cross Validation Accuracy = %g%%%n", 100.0 * total_correct / prob.l);
        }
    }

    private void exit_with_help() {
    	if(this.param != null)
    		System.out.println("======="+this.param.toString());
    	if(this.prob != null)
    		System.out.println("==========="+this.prob.toString());
        System.out.printf("Usage: train [options] training_set_file [model_file]%n" //
            + "options:%n"
            + "-s type : set type of solver (default 1)%n"
            + "  for multi-class classification%n"
            + "    0 -- L2-regularized logistic regression (primal)%n"
            + "    1 -- L2-regularized L2-loss support vector classification (dual)%n"
            + "    2 -- L2-regularized L2-loss support vector classification (primal)%n"
            + "    3 -- L2-regularized L1-loss support vector classification (dual)%n"
            + "    4 -- support vector classification by Crammer and Singer%n"
            + "    5 -- L1-regularized L2-loss support vector classification%n"
            + "    6 -- L1-regularized logistic regression%n"
            + "    7 -- L2-regularized logistic regression (dual)%n"
            + "  for regression%n"
            + "   11 -- L2-regularized L2-loss support vector regression (primal)%n"
            + "   12 -- L2-regularized L2-loss support vector regression (dual)%n"
            + "   13 -- L2-regularized L1-loss support vector regression (dual)%n"
            + "-c cost : set the parameter C (default 1)%n"
            + "-p epsilon : set the epsilon in loss function of SVR (default 0.1)%n"
            + "-e epsilon : set tolerance of termination criterion%n"
            + "   -s 0 and 2%n" + "       |f'(w)|_2 <= eps*min(pos,neg)/l*|f'(w0)|_2,%n"
            + "       where f is the primal function and pos/neg are # of%n"
            + "       positive/negative data (default 0.01)%n" + "   -s 11%n"
            + "       |f'(w)|_2 <= eps*|f'(w0)|_2 (default 0.001)%n"
            + "   -s 1, 3, 4 and 7%n" + "       Dual maximal violation <= eps; similar to libsvm (default 0.1)%n"
            + "   -s 5 and 6%n"
            + "       |f'(w)|_1 <= eps*min(pos,neg)/l*|f'(w0)|_1,%n"
            + "       where f is the primal function (default 0.01)%n"
            + "   -s 12 and 13\n"
            + "       |f'(alpha)|_1 <= eps |f'(alpha0)|,\n"
            + "       where f is the dual function (default 0.1)\n"
            + "-B bias : if bias >= 0, instance x becomes [x; bias]; if < 0, no bias term added (default -1)%n"
            + "-wi weight: weights adjust the parameter C of different classes (see README for details)%n"
            + "-v n: n-fold cross validation mode%n"
            + "-q : quiet mode (no outputs)%n");
        System.exit(1);
    }


    Problem getProblem() {
        return prob;
    }

    double getBias() {
        return bias;
    }

    Parameter getParameter() {
        return param;
    }

    void parse_command_line(String argv[]) {
        int i;

        // eps: see setting below
        param = new Parameter(SolverType.L2R_L2LOSS_SVC_DUAL, 1, Double.POSITIVE_INFINITY, 0.1);
        // default values
        bias = -1;
        cross_validation = false;

        // parse options
        for (i = 0; i < argv.length; i++) {
            if (argv[i].charAt(0) != '-') break;
            if (++i >= argv.length) exit_with_help();
            switch (argv[i - 1].charAt(1)) {
                case 's':
                    param.solverType = SolverType.getById(atoi(argv[i]));
                    break;
                case 'c':
                    param.setC(atof(argv[i]));
                    break;
                case 'p':
                    param.setP(atof(argv[i]));
                    break;
                case 'e':
                    param.setEps(atof(argv[i]));
                    break;
                case 'B':
                    bias = atof(argv[i]);
                    break;
                case 'w':
                    int weightLabel = atoi(argv[i - 1].substring(2));
                    double weight = atof(argv[i]);
                    param.weightLabel = addToArray(param.weightLabel, weightLabel);
                    param.weight = addToArray(param.weight, weight);
                    break;
                case 'v':
                    cross_validation = true;
                    nr_fold = atoi(argv[i]);
                    if (nr_fold < 2) {
                        System.err.println("n-fold cross validation: n must >= 2");
                        exit_with_help();
                    }
                    break;
                case 'q':
                    i--;
                    Linear.disableDebugOutput();
                    break;
                default:
                    System.err.println("unknown option");
                    exit_with_help();
            }
        }

        // determine filenames

//        if (i >= argv.length) exit_with_help();
//
//        inputFilename = argv[i];
//
//        if (i < argv.length - 1)
//            modelFilename = argv[i + 1];
//        else {
//            int p = argv[i].lastIndexOf('/');
//            ++p; // whew...
//            modelFilename = argv[i].substring(p) + ".model";
//        }

        if (param.eps == Double.POSITIVE_INFINITY) {
            switch (param.solverType) {
                case L2R_LR:
                case L2R_L2LOSS_SVC:
                    param.setEps(0.01);
                    break;
                case L2R_L2LOSS_SVR:
                    param.setEps(0.001);
                    break;
                case L2R_L2LOSS_SVC_DUAL:
                case L2R_L1LOSS_SVC_DUAL:
                case MCSVM_CS:
                case L2R_LR_DUAL:
                    param.setEps(0.1);
                    break;
                case L1R_L2LOSS_SVC:
                case L1R_LR:
                    param.setEps(0.01);
                    break;
                case L2R_L1LOSS_SVR_DUAL:
                case L2R_L2LOSS_SVR_DUAL:
                    param.setEps(0.1);
                    break;
                default:
                    throw new IllegalStateException("unknown solver type: " + param.solverType);
            }
        }
    }

    /**
     * reads a problem from LibSVM format
     * @param file the SVM file
     * @throws IOException obviously in case of any I/O exception ;)
     * @throws InvalidInputDataException if the input file is not correctly formatted
     */
    public static Problem readProblem(File file, double bias) throws IOException, InvalidInputDataException {
        BufferedReader fp = new BufferedReader(new FileReader(file));
        List<Double> vy = new ArrayList<Double>();
        List<Feature[]> vx = new ArrayList<Feature[]>();
        int max_index = 0;

        int lineNr = 0;

        try {
            while (true) {
                String line = fp.readLine();
                if (line == null) break;
                lineNr++;

                StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");
                String token;
                try {
                    token = st.nextToken();
                } catch (NoSuchElementException e) {
                    throw new InvalidInputDataException("empty line", file, lineNr, e);
                }

                try {
                    vy.add(atof(token));
                } catch (NumberFormatException e) {
                    throw new InvalidInputDataException("invalid label: " + token, file, lineNr, e);
                }

                int m = st.countTokens() / 2;
                Feature[] x;
                if (bias >= 0) {
                    x = new Feature[m + 1];
                } else {
                    x = new Feature[m];
                }
                int indexBefore = 0;
                for (int j = 0; j < m; j++) {

                    token = st.nextToken();
                    int index;
                    try {
                        index = atoi(token);
                    } catch (NumberFormatException e) {
                        throw new InvalidInputDataException("invalid index: " + token, file, lineNr, e);
                    }

                    // assert that indices are valid and sorted
                    if (index < 0) throw new InvalidInputDataException("invalid index: " + index, file, lineNr);
                    if (index <= indexBefore) throw new InvalidInputDataException("indices must be sorted in ascending order", file, lineNr);
                    indexBefore = index;

                    token = st.nextToken();
                    try {
                        double value = atof(token);
                        x[j] = new FeatureNode(index, value);
                    } catch (NumberFormatException e) {
                        throw new InvalidInputDataException("invalid value: " + token, file, lineNr);
                    }
                }
                if (m > 0) {
                    max_index = Math.max(max_index, x[m - 1].getIndex());
                }

                vx.add(x);
            }

            return constructProblem(vy, vx, max_index, bias);
        }
        finally {
            fp.close();
        }
    }
    
    /**
     * reads a problem from LibSVM format
     * @param file the SVM file
     * @throws IOException obviously in case of any I/O exception ;)
     * @throws InvalidInputDataException if the input file is not correctly formatted
     */
    public static Problem readProblem1(String[] input, double bias) throws IOException, InvalidInputDataException {
        List<Double> vy = new ArrayList<Double>();
        List<Feature[]> vx = new ArrayList<Feature[]>();
        int max_index = 0;

        int lineNr = 0;

            for(String line : input){
                if (line == null) break;
                lineNr++;

                StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");
                String token;
                try {
                    token = st.nextToken();
                } catch (NoSuchElementException e) {
                    throw new InvalidInputDataException("empty line", "", lineNr, e);
                }

                try {
                    vy.add(atof(token));
                } catch (NumberFormatException e) {
                    throw new InvalidInputDataException("invalid label: " + token, "", lineNr, e);
                }

                int m = st.countTokens() / 2;
                Feature[] x;
                if (bias >= 0) {
                    x = new Feature[m + 1];
                } else {
                    x = new Feature[m];
                }
                int indexBefore = 0;
                for (int j = 0; j < m; j++) {

                    token = st.nextToken();
                    int index;
                    try {
                        index = atoi(token);
                    } catch (NumberFormatException e) {
                        throw new InvalidInputDataException("invalid index: " + token, "", lineNr, e);
                    }

                    // assert that indices are valid and sorted
                    if (index < 0) throw new InvalidInputDataException("invalid index: " + index, "", lineNr);
                    if (index <= indexBefore) throw new InvalidInputDataException("indices must be sorted in ascending order", "", lineNr);
                    indexBefore = index;

                    token = st.nextToken();
                    try {
                        double value = atof(token);
                        x[j] = new FeatureNode(index, value);
                    } catch (NumberFormatException e) {
                        throw new InvalidInputDataException("invalid value: " + token, "", lineNr);
                    }
                }
                if (m > 0) {
                    max_index = Math.max(max_index, x[m - 1].getIndex());
                }

                vx.add(x);
            }

            return constructProblem(vy, vx, max_index, bias);
    }

    void readProblem(String filename) throws IOException, InvalidInputDataException {
        prob = Train1.readProblem(new File(filename), bias);
    }
    
    void readProblem1(String[] input) throws IOException, InvalidInputDataException {
    	 prob = Train1.readProblem1(input, bias);
	}

    private static int[] addToArray(int[] array, int newElement) {
        int length = array != null ? array.length : 0;
        int[] newArray = new int[length + 1];
        if (array != null && length > 0) {
            System.arraycopy(array, 0, newArray, 0, length);
        }
        newArray[length] = newElement;
        return newArray;
    }

    private static double[] addToArray(double[] array, double newElement) {
        int length = array != null ? array.length : 0;
        double[] newArray = new double[length + 1];
        if (array != null && length > 0) {
            System.arraycopy(array, 0, newArray, 0, length);
        }
        newArray[length] = newElement;
        return newArray;
    }

    private static Problem constructProblem(List<Double> vy, List<Feature[]> vx, int max_index, double bias) {
        Problem prob = new Problem();
        prob.bias = bias;
        prob.l = vy.size();
        prob.n = max_index;
        if (bias >= 0) {
            prob.n++;
        }
        prob.x = new Feature[prob.l][];
        for (int i = 0; i < prob.l; i++) {
            prob.x[i] = vx.get(i);

            if (bias >= 0) {
                assert prob.x[i][prob.x[i].length - 1] == null;
                prob.x[i][prob.x[i].length - 1] = new FeatureNode(max_index + 1, bias);
            }
        }

        prob.y = new double[prob.l];
        for (int i = 0; i < prob.l; i++)
            prob.y[i] = vy.get(i).doubleValue();

        return prob;
    }

    private void run(String[] args) throws IOException, InvalidInputDataException {
        parse_command_line(args);
        readProblem(inputFilename);
        if (cross_validation)
        	//do_cross_validation();
        	learnBestC(args);
        	//do_cross_validation_AUC();
        else {
            Model model = Linear.train(prob, param);
            Linear.saveModel(new File(modelFilename), model);
        }
    }
    public Model run1(String[] args,String[] input) throws IOException, InvalidInputDataException {
        parse_command_line(args);
        readProblem1(input);
        return Linear.train(prob, param);
    }
    public Model runAUC(String[] args,String[] input) throws IOException, InvalidInputDataException {
    	readProblem1(input);
    	
    	args = learnBestC(args);
    	
    	parse_command_line(args);
    	
        return  Linear.train(prob, param);
    }
    /*
     * 计算AUC 选择最优的C参数
     */
    public String[] learnBestC(String[] args) throws IOException, InvalidInputDataException {
        
    	if (prob.l<=9) {
			setAuc(0);
			return args;
		}

        //计算交叉预测的AUC
        double j = 0;
        double auc = 0;
        double temp = 0;
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
        	j = Math.pow(2, i);
        	args = ("-s 7 -v 3 -c " + j +" "+inputFilename).split(" ");
        	parse_command_line(args);
        	temp = do_cross_validation_AUC();
        	//System.out.println("--------------------------C = "+j + "  Auc = "+temp+"--------------------------");
        	if (auc != 0 &&  (temp-auc)/auc < 0.01 ) {
        		if (temp < auc) {
        			args = ("-s 7 -c " + (j/2) +" "+inputFilename).split(" ");
            		//parse_command_line(args);
            		//System.out.println("--------------------------"+"Best_C = "+ j/2 + ", Best_AUC = "+auc+"--------------------------");
				}else {
					args = ("-s 7 -c " + j +" "+inputFilename).split(" ");
            		//parse_command_line(args);
            		//System.out.println("--------------------------"+"Best_C = "+ j + ", Best_AUC = "+temp+"--------------------------");
				}
        		
				break;
			}
        	
			auc = temp;
			setAuc(auc);
		}
        
       
        return args;
    }

	public double getAuc() {
		return auc;
	}

	public void setAuc(double auc) {
		this.auc = auc;
	}

}

