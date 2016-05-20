package wekaloop;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.*;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Random;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.rules.DecisionTable;
import weka.classifiers.rules.NNge;
import weka.classifiers.rules.Ridor;
import weka.classifiers.trees.BFTree;
import weka.classifiers.trees.FT;
import weka.classifiers.trees.LMT;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.meta.Bagging;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.bayes.NaiveBayesSimple;
import weka.classifiers.bayes.NaiveBayesUpdateable;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.RBFNetwork;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.SimpleLogistic;
import weka.classifiers.lazy.IB1;
import weka.classifiers.lazy.IBk;
import weka.classifiers.lazy.KStar;
import weka.classifiers.lazy.LWL;
import weka.classifiers.meta.AttributeSelectedClassifier;
import weka.classifiers.meta.CVParameterSelection;
import weka.classifiers.meta.ClassificationViaClustering;
import weka.classifiers.meta.ClassificationViaRegression;
import weka.classifiers.meta.Dagging;
import weka.classifiers.meta.Decorate;
import weka.classifiers.meta.END;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.meta.Grading;
import weka.classifiers.meta.LogitBoost;
import weka.classifiers.meta.MultiBoostAB;
import weka.classifiers.meta.MultiClassClassifier;
import weka.classifiers.meta.MultiScheme;
import weka.classifiers.meta.OrdinalClassClassifier;
import weka.classifiers.meta.RacedIncrementalLogitBoost;
import weka.classifiers.meta.RandomCommittee;
import weka.classifiers.meta.RandomSubSpace;
import weka.classifiers.meta.Stacking;
import weka.classifiers.meta.StackingC;
import weka.classifiers.meta.Vote;
import weka.classifiers.misc.VFI;
import weka.classifiers.rules.ConjunctiveRule;
import weka.classifiers.rules.PART;
import weka.classifiers.rules.ZeroR;
import weka.classifiers.trees.DecisionStump;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.J48graft;
import weka.classifiers.trees.NBTree;
import weka.classifiers.trees.RandomTree;
import weka.classifiers.trees.SimpleCart;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

/**
 *
 * @author kashyap
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        String fileName;
        fileName = "/home/kashyap/data.csv";
        testClassifiers(fileName);
    }

    static private void testClassifiers(String fileName) {
        int NUM_CATS = 5;
        int NUM_DATA_FILES = 1;
        int NUM_CLASSIFIERS = 0;
        
        // load data
        try {
            /// The data processing part starts here
            for (int x = 1 ; x <= NUM_DATA_FILES ; x ++ ) {
                String id = Integer.valueOf(x).toString();
                String dataFile = "data2.arff";
                DataSource source = new DataSource("/home/kashyap/data/old/"+dataFile);

                Instances data = source.getDataSet();
                data.setClassIndex(data.numAttributes() - 1);

                Instances trainData = trainData(data);
                trainData.setClassIndex(trainData.numAttributes() - 1);
                // Instances testData = testData(data);

                //double [][] simMatrix = readMatrix("/home/kashyap/data/old/similarity", NUM_CATS, NUM_CATS);
                //double [][] simMatrix2 = readMatrix("/home/kashyap/data/old/similarity2", NUM_CATS, NUM_CATS);
                // Create classifiers
                Classifier[] classifiers = new Classifier[80];
                double[] f1as = new double[80];
                double[] f1bs = new double[80];
                int i = 0;

                //classifiers[i++]= new NaiveBayes();
                //classifiers[i++]= new NaiveBayesSimple();
                //classifiers[i++]= new NaiveBayesUpdateable();
                //classifiers[i++]= new Logistic();
                //classifiers[i++]= new MultilayerPerceptron();
                //classifiers[i++]= new RBFNetwork();
                //classifiers[i++]= new SimpleLogistic();
                //classifiers[i++]= new SMO();
                //classifiers[i++]= new IB1();
                //classifiers[i++]= new IBk();
                //classifiers[i++]= new KStar();
                //classifiers[i++]= new LWL();
                //classifiers[i++]= new AttributeSelectedClassifier();
                //classifiers[i++]= new Bagging();
                //classifiers[i++]= new ClassificationViaClustering();
                //classifiers[i++]= new ClassificationViaRegression();
                //classifiers[i++]= new Dagging();
                //classifiers[i++]= new Decorate();
                //classifiers[i++]= new END();
                //classifiers[i++]= new FilteredClassifier();
                //classifiers[i++]= new LogitBoost();
                //classifiers[i++]= new MultiBoostAB();
                //classifiers[i++]= new MultiClassClassifier();
                //classifiers[i++]= new VFI();
                //classifiers[i++]= new BFTree();
                //classifiers[i++]= new DecisionStump();
                //classifiers[i++]= new FT();
                //classifiers[i++]= new J48();
                //classifiers[i++]= new J48graft();
                //classifiers[i++]= new LMT();
                //classifiers[i++]= new NBTree();
                //classifiers[i++]= new RandomTree();
                //classifiers[i++]= new RandomForest();
                //classifiers[i++]= new SimpleCart();
                //classifiers[i++]= new ConjunctiveRule();
                //classifiers[i++]= new DecisionTable();
                //classifiers[i++]= new PART();
                //classifiers[i++]= new Ridor();
                //classifiers[i++]= new ZeroR();
                //classifiers[i++]= new NNge();
                classifiers[0] = new LibSVM();
                NUM_CLASSIFIERS = 1;

                double[][] confusion;
                // Category - Elaboration - Precondition - Spurious
                for (i = 0 ; i < NUM_CLASSIFIERS ; i ++) {
                    for ( int j = 0 ; j < 10 ; j ++ )
                    double f1a, f1b;
                    classify( classifiers[i], trainData);
                    System.out.println(classifiers[i].toString().split("\n")[0]);

                    double f1 = evaluate(classifiers[i], trainData, 5);
                }

                for (i = 0 ; i < NUM_CLASSIFIERS ; i ++ ) {
                    System.out.print(f1as[i]);
                    System.out.print(" ");
                    System.out.println(f1bs[i]);
                }
            
            }
        } catch(Exception e){
            e.printStackTrace();

        }
    }
    static private void classify(Classifier c, Instances data)
    {
        try {
            c.buildClassifier(data);
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
    static private void printMatrix(double[][] matrix)
    {
        for ( int i = 0 ; i < matrix.length ; i ++ ) {
            for ( int j = 0 ; j < matrix[i].length ; j ++ ) {
                System.out.print(matrix[i][j]);
                System.out.print(" ");
            }
            System.out.println();
        }
    }

    static private double evaluate(Classifier c, Instances data, int folds)
    {
        Evaluation eval = null;
        try {
            // System.out.println();
            eval = new Evaluation(data);
            eval.crossValidateModel(c, data, folds, new Random(1));
            // System.out.println(eval.toMatrixString());
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        // return eval.confusionMatrix();
        return eval.fMeasure(1);
        
    }

    private static Instances trainData(Instances data) {
        Remove filter = new Remove();
        Instances trainData = null;
        try {            
            filter.setAttributeIndices("1");
            filter.setInputFormat(data);
            trainData = Filter.useFilter(data, filter);
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        return trainData;
    }

    private static double[][] readMatrix(String fileName, int M, int N) {
        double [][] matrix = new double[M][N];
        try {
            FileInputStream fstream = new FileInputStream(fileName);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            for ( int i = 0 ; i < M ; i ++ ) {
                String stringInt = br.readLine();
                String[] list = stringInt.split(" ");
                for ( int j = 0 ; j < N ; j ++ ) {            
                    matrix[i][j] = Double.parseDouble(list[j]);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        return matrix;
    }

    private static double getResults(double[][] confusion, double[][] simMatrix) {
        double[] TP = new double[confusion.length];
        double[] FP = new double[confusion.length];
        double[] FN = new double[confusion.length];
        for (int i = 0 ; i < confusion.length ; i ++ ) {
            TP[i] = 0.0;
            FP[i] = 0.0;
            FN[i] = 0.0;
        }
        for (int i = 0 ; i < confusion.length ; i ++ ) {
            for (int j = 0 ; j < confusion.length ; j ++ ) {
                if (i==j) {
                    TP[i] += confusion[i][j];
                } else {
                    FP[i] += confusion[i][j] * (1.0-simMatrix[i][j]);
                    FN[j] += confusion[i][j] * (1.0-simMatrix[i][j]);
                    // Partial credit
                    TP[i] += confusion[i][j] * simMatrix[i][j];
                }
            }
        }
        // Micro avereage precision and recall
        double totalTP = 0.0, totalFP = 0.0, totalFN = 0.0;
        for (int i = 0 ; i < confusion.length ; i ++ ) {
            totalTP += TP[i];
            totalFP += FP[i];
            totalFN += FN[i];
        }
        double map, mar;
        map = totalTP/(totalTP+totalFP);
        mar = totalTP/(totalTP+totalFN);
        // System.out.printf("Micro average precision %f\n", map);
        // System.out.printf("Micro average recall %f\n", mar);
        // Micro average precision and recall

        double totalPrecision = 0.0, totalRecall = 0.0;
        for (int i = 0 ; i < confusion.length ; i ++ ) {
            totalPrecision += TP[i]/(TP[i]+FP[i]);
            totalRecall += TP[i]/(TP[i]+FN[i]);
        }
        double Map, Mar;
        Map = totalPrecision / confusion.length;
        Mar = totalRecall /= confusion.length;
        //System.out.printf("Macro average precision %f\n",Map);
        //System.out.printf("Macro average recall %f\n", Mar);

        double f1 = 2 * Map * Mar/ (Map+Mar);
        return f1;
    }

    private static float getF1(Classifier classifier, Instances trainData, int i) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


}

