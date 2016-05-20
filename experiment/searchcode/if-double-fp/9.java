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
public class Main2 {

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
                String dataFile = "data" + id + ".arff";
                DataSource source = new DataSource("/home/kashyap/data/"+dataFile);
                Instances data = source.getDataSet();
                data.setClassIndex(data.numAttributes() - 1);

                Instances trainData = trainData(data);
                trainData.setClassIndex(trainData.numAttributes() - 1);
                // Instances testData = testData(data);

                double [][] simMatrix = readMatrix("/home/kashyap/data/similarity", NUM_CATS, NUM_CATS);
                double [][] simMatrix2 = readMatrix("/home/kashyap/data/similarity2", NUM_CATS, NUM_CATS);
                // Create classifiers
                 
                Classifier classifier = new RandomForest();
                double[][] confusion;
                double f1a, f1b;
                classify( classifier, trainData);

                confusion = evaluate( classifier, trainData, 5);
                f1a = getResults(confusion, simMatrix);
                f1b = getResults(confusion, simMatrix2);
                System.out.println("Using Cross Validation Function");
                System.out.printf("Normal Similarity Matrix ", f1a);
                System.out.printf("Modified Similarity Matrix", f1b);

                crossValidate(classifier, 5, trainData);

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

    static private void crossValidate(Classifier classifier, int numFold, Instances data) {
        Evaluation eval = null;
        for (int j = 0; j < numFold; j++) {
            try {
                eval = new Evaluation(data);
                Instances train = data.trainCV(numFold, j, new Random(1));
                Instances test = data.testCV(numFold, j);
                classifier.buildClassifier(train);
                eval.setPriors(train);
                eval.evaluateModel(classifier, test);
                System.out.printf("Fold %d\n", j);
                for (int i = 0 ; i < 5 ; i ++ )
                    System.out.println(eval.precision(i));
            } catch (Exception ex) {
                Logger.getLogger(Main2.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        double error =  eval.errorRate();
    }

    static private double[][] evaluate(Classifier c, Instances data, int folds)
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

        return eval.confusionMatrix();

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


}

