package decisionTree;
/*
 * Team 13 Prodigy
 * Cross validation used to evaluate the performance of a classifier
 * Set the default seed for randomizing the instances.
 */

import java.util.ArrayList;
import java.util.Random;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class CrossValidation {
	private Instances dataSet; 
	private int k;           //folds
	private int sizeOfInput; //size of entire data size
	private ArrayList<Instances> bigArrayList;
	public static final double epsilon = 1E-17;
	
	public static void main(String[] args) {
		DataSource source = null;
		Instances data = null;
		try {		
			source = new DataSource("trainProdSelection.arff");		
			data = source.getDataSet();
			if (data.classIndex() == -1)
				data.setClassIndex(data.numAttributes() - 1);
			
			DecisionTree dt = new DecisionTree(2);
			dt.buildClassifier(data);
			dt.printTree(data);
			CrossValidation cv = new CrossValidation(data, 5);
			System.out.println("Final: " + cv.doCrossValidation(data, dt));
			
			//System.out.println(cv.doCrossValidation(data, j48));
		} catch (Exception e) {
			e.printStackTrace();
		}				
	}
	
	public CrossValidation(Instances dataSet, int k){
		this.dataSet = dataSet;
		this.sizeOfInput = dataSet.size();
		this.k = k;
		this.bigArrayList = generateFolds();
	}
	
	private int[] performPermutation() {
        int j = 0;
        int k; 
        int seed = 10 % sizeOfInput;
        //System.out.println(sizeOfInput);
        Random rand = new Random(seed);
        
        int[] dataNum = new int[sizeOfInput];
        for (int i = 0; i < sizeOfInput; i++) {
                dataNum[i] = i;
        }

        for (int i = sizeOfInput - 1; i > 0; i--) {
                j = rand.nextInt(i+1); // random integer between 0 and i
                k = dataNum[i];
                dataNum[i] = dataNum[j];
                dataNum[j] = k;
        }
        return dataNum;
	}
	
	public ArrayList<Instances> generateFolds(){
		bigArrayList = new ArrayList<Instances>();
        int[] dataNum = new int[sizeOfInput];
        dataNum = performPermutation();
        int sizePerFold = sizeOfInput/k;
        int reminder = sizeOfInput % k;
        
        if(reminder == 0){
        	sizePerFold = sizeOfInput/k;
        } 
        
        // use bigArrayList to store k folds
        for (int i=0; i<k; i++){
        	bigArrayList.add(new Instances(dataSet, sizeOfInput));
        }
        
        int count=0;
        for(int i=0; i<k; i++){
        	Instances folds = bigArrayList.get(i);
        	
        	if (i<reminder){
        		for(int j=count; j<sizePerFold*(i+1) + 1; j++){
            		folds.add(dataSet.get(dataNum[j]));
            	}
            	count += sizePerFold;
        	} else{
        		for(int j=count; j<sizePerFold*(i+1); j++){
            		folds.add(dataSet.get(dataNum[j]));
            	}
            	count += sizePerFold;
        	}	
        }

        return bigArrayList;
	}
	
	public Instances getTrainingData(int n){
		Instances trainingData = new Instances(dataSet, sizeOfInput);
		
		for(int i=0; i<k; i++){
			if(i == n) continue;
			Instances oneFold = bigArrayList.get(i);
			trainingData.addAll(oneFold);
		}
		
		return trainingData;
	}
	
	public double doCrossValidation(Instances dataSet, DecisionTree dt) throws Exception{
		double accuracy = 0.0;
		double[] accuracyPerFold = new double[k];
		int testFoldSize = 0;		

		for(int i=0; i<k; i++){
			dt.buildClassifier(this.getTrainingData(i));
			Instances testFold = bigArrayList.get(i);
			testFoldSize = testFold.size();
			int isTheSame = 0;
			
			for (int j = 0; j < testFoldSize; j++) {
				Instance in = testFold.get(j);

				if(dt.classifyInstance(in) == in.classValue()) {
					isTheSame++;
				}
			}
			accuracyPerFold[i] = (double)isTheSame/(double)testFoldSize;
		}
		
		//System.out.println(Arrays.toString(accuracyPerFold));
		
		for(int i=0; i<k; i++){
			accuracy += accuracyPerFold[i];
		}
		
		accuracy = accuracy/k;
		return accuracy;
	}
	
}

