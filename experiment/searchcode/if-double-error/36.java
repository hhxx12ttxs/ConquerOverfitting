import java.io.*;
import java.util.*;

/*
 * The class containing most algorithms and functions that create
 * and operate on avPair trees and Instances
 */ 

public class TDIDT{
  
  /* Given a training file, a test file, and a stopping parameter,
   * generate ArrayLists of instances from the files, generate a
   * decision tree from the training file, return the rate of correct
   * answers of the test set on the tree. 
   * mi = true uses information gain to split;
   *    = false uses classification error minimization    */
  
  public double fullSuite (String trainfile, String testfile, int stoppingParameter, boolean mi){
    
    ArrayList<Instance> trainingSet = createFromFile(trainfile);
    ArrayList<Instance> testingSet  = createFromFile(testfile);
    return fullSuite(trainingSet,testingSet, stoppingParameter, mi);
    
  }
  
  /* Same as above, accept with two ArrayLists as inputs instead of filenames */
  
  public double fullSuite (ArrayList<Instance> tr, ArrayList<Instance> te, int stoppingParameter, boolean mi){
    
    ArrayList<Instance> trainingSet = tr;
    avPair decisionTree = buildDT(trainingSet, stoppingParameter, mi);
    return fullSuite(tr, te, stoppingParameter, decisionTree);
  }
  /* Given training set, test set, stopping parameter, and a decision tree, return the accuracy of the
   * test set. Passes root to enable recursive call for pruning.
   */ 
  public double fullSuite (ArrayList<Instance> tr, ArrayList<Instance> te, int stoppingParameter, avPair root){
    
    ArrayList<Instance> trainingSet = tr;
    ArrayList<Instance> testingSet  = te;
    avPair decisionTree = root;
    
    double n = testingSet.size();
    double corCount=0; 
    double incCount=0;
    
    for(int i = 0; i< n; i++){
      boolean myAnswer   = decisionTree.testInstance(testingSet.get(i));
      boolean realAnswer = testingSet.get(i).benign();
      if(myAnswer==realAnswer){corCount++;}
      else{incCount++;}
    }
    
    double correctRate = corCount/n;
    int nodeCount = decisionTree.nodeCount();
    int leafCount = decisionTree.leafCount();
    
    //System.out.println("Finished with " + correctRate + " correct rate.");
    //System.out.println("Tree has " + nodeCount + " nodes with " + leafCount + " leafs.");
    return corCount/n;                     
  }
  
  /* cross validation for part (d). given a training file and stopping parameter,
   * divides training set into ten parts and generates nine trees, testing with
   * the tenth set, for all ten parts, returning the average correct rate of all
   * ten permutations */
  
  public double crossValidate(String trainfile, int stoppingParameter, boolean mi){
    
    ArrayList<Instance> fullSet = createFromFile(trainfile);
    Collections.shuffle(fullSet); //shuffle for randomness
    
    double errorSum = 0;
    
    for(int i=0; i<10; i++){
      int validationStart = i*55;
      int validationFinish= validationStart + 55;
      
      ArrayList<Instance> validationSet = new ArrayList<Instance>();
      ArrayList<Instance> trainingSet   = new ArrayList<Instance>();
      
      int n = fullSet.size();
      for(int j=0;j< n; j++){
        if(j >= validationStart && j < validationFinish){
          validationSet.add(fullSet.get(j));
        }
        else{
          trainingSet.add(fullSet.get(j));
        }
      }
      
      double partialError = fullSuite(trainingSet, validationSet, stoppingParameter, mi);
      errorSum += partialError;
    }
    
    double avgError = errorSum/10;
    
    return avgError;
  }
  
  /*
   * Helper function for recursive reducedErrorPruning call below.
   * Given training and validation filenames, constructs training and validation
   * sets, then builds a decision tree from the training set. Enumerates
   * this tree into an ArrayList for iteration in next step. Then, 
   * passes the avPair list, sets, and stopping parameter to the recursive function
   */ 
  
  public ArrayList<avPair> reducedErrorPruning(String trainfile, String validationfile, int stoppingParameter, boolean mi){
    
    ArrayList<Instance> trainingSet = createFromFile(trainfile);
    ArrayList<Instance> validationSet  = createFromFile(validationfile);
    avPair decisionTree = buildDT(trainingSet, stoppingParameter, mi);
    
    
    ArrayList<avPair> myAVList = decisionTree.enumeratePairs();
    
    return reducedErrorPruning(trainingSet, validationSet, stoppingParameter, myAVList,mi);
    
  }
  /* Recursive pruning call. Determines initial correct rate; then, iteratively
   * "prunes" every node in the decision tree and tests the accuracy of the new tree
   * before changing node back. If any pruned node increased accuracy, changes this node
   * permanently and calls the function again with the new tree. Otherwise, returns
   * the ArrayList of nodes
   */ 
  public ArrayList<avPair> reducedErrorPruning(ArrayList<Instance> tSet, ArrayList<Instance> vSet, int stoppingParameter, ArrayList<avPair> avl, boolean mi){
    
    ArrayList<Instance> trainingSet = tSet;
    ArrayList<Instance> validationSet  = vSet;
    avPair decisionTree = buildDT(trainingSet, stoppingParameter, mi);
    
    ArrayList<avPair> myAVList = avl;
    double initialCorrect = fullSuite(trainingSet,validationSet,stoppingParameter,myAVList.get(0));
    
    double bestCorrect = initialCorrect;
    int candidate = -1;
    
    // iteratively prune every node in list and test new tree. If change shows improvement,
    // store index in 'candidate' and change current best accuracy
    int k = myAVList.size();
    for(int i=0;i<k;i++){
      double newCorrect = 0;
      avPair myAV = myAVList.get(i);
      myAV.setMajorityTrue();
      newCorrect = fullSuite(trainingSet,validationSet,stoppingParameter,myAVList.get(0));
      //System.out.println("Correct rate " + i + " is " + newCorrect);
      if(newCorrect > bestCorrect){
        bestCorrect = newCorrect;
        candidate = i;
      }
      myAV.endMajority();
    }
    // if any node improved over baseline, change permanently and recurse
    if(candidate >= 0){
      myAVList.get(candidate).removeChildren();
      return reducedErrorPruning(tSet, vSet, stoppingParameter, myAVList,mi);
    }
    else{
      return myAVList;
    }
  }
  /* Basic function for creating a new set of instances from a file */
  
  public ArrayList<Instance> createFromFile(String filename){
    
    ArrayList<Instance> a = new ArrayList<Instance>();
    
    try{
      BufferedReader bufferedReader = new BufferedReader(new FileReader(filename));
      String line = null;
      
      while ((line = bufferedReader.readLine()) != null) {
        
        String curr = line;
        String raw0 = curr.substring(curr.indexOf("1:")+2,curr.indexOf("2:"));
        double val0 = Double.parseDouble(raw0);
        String raw1 = curr.substring(curr.indexOf("2:")+2,curr.indexOf("3:"));
        double val1 = Double.parseDouble(raw1);
        String raw2 = curr.substring(curr.indexOf("3:")+2,curr.indexOf("4:"));
        double val2 = Double.parseDouble(raw2);
        String raw3 = curr.substring(curr.indexOf("4:")+2,curr.indexOf("5:"));
        double val3 = Double.parseDouble(raw3);
        String raw4 = curr.substring(curr.indexOf("5:")+2,curr.indexOf("6:"));
        double val4 = Double.parseDouble(raw4);
        String raw5 = curr.substring(curr.indexOf("6:")+2,curr.indexOf("7:"));
        double val5 = Double.parseDouble(raw5);
        String raw6 = curr.substring(curr.indexOf("7:")+2,curr.indexOf("8:"));
        double val6 = Double.parseDouble(raw6);
        String raw7 = curr.substring(curr.indexOf("8:")+2,curr.indexOf("9:"));
        double val7 = Double.parseDouble(raw7);
        String raw8 = curr.substring(curr.indexOf("9:")+2,curr.indexOf("#"));
        double val8 = Double.parseDouble(raw8);
        
        String rVal = curr.substring(0, curr.indexOf("1:"));
        boolean bVal;
        if(rVal.indexOf("B")>=0){bVal=true;}
        else{bVal=false;}
        
        Instance e = new Instance(val0,val1,val2,val3,val4,val5,val6,val7,val8,bVal);
        a.add(e);
        
      }
    }
    catch(FileNotFoundException e) { 
      System.out.println("File Not Found");
    } catch(ArrayIndexOutOfBoundsException e) { 
      System.out.println("Usage: ShowFile File");
      System.err.println(e);
      System.err.println(Arrays.toString(e.getStackTrace()));
    } 
    catch(IOException e) { 
      System.out.println("File Error"); 
    } 
    return a;
  }
  // return log base 2 of argument
  public double log2(double argument){
    if(argument==0){return 0;}
    return Math.log(argument)/Math.log(2);
  }
  //return Entropy of set S
  public double calculateEntropy(ArrayList<Instance> S){
    int n = S.size();
    double bCount = 0;
    double mCount = 0;
    for(int i =0; i<n;i++){
      if(S.get(i).benign()){
        bCount++;
      }
      else{
        mCount++;
      }
    }
    double count = bCount + mCount;
    double bProb = bCount/count;
    double mProb = mCount/count;
    //System.out.println("bCount is : " + bCount + ", mCount is : " + mCount);
    double ent = (-1.0 * bProb) * log2(bProb) + (-1.0 * mProb) * log2(mProb);
    return ent;
  }
  
  /* Split S into two new sets along attribute-value pair. Calculate the initial entropy
   * and entropy of split sets; then, return the differences */
  
  public double calculateEntropyGain(ArrayList<Instance> S, int attribute, int value){
    double lCount=0;
    double hCount=0;
    ArrayList<Instance> lowerS = new ArrayList<Instance>();
    ArrayList<Instance> higherS= new ArrayList<Instance>();
    
    int n = S.size();
    for(int i=0;i<n;i++){
      if(S.get(i).get(attribute)<=value){
        //System.out.println("Lower add");
        lCount++;
        lowerS.add(S.get(i));
      }
      else{
        //System.out.println("Upper add");
        hCount++;
        higherS.add(S.get(i));
      }
    }
    
    double count = lCount+hCount;
    double lProb = lCount/count;
    double hProb = hCount/count;
    
    double entropyHigher = calculateEntropy(higherS);
    double entropyLower  = calculateEntropy(lowerS);
    
    double entropyInitial = calculateEntropy(S);
    double entropyChange  = lProb * entropyLower + hProb * entropyHigher;
    
    return entropyInitial - entropyChange;
  }
  
  /* Iterate over every possible attribute-value pair, using 
   * calculateEntropy() to determine entropy gain from splitting
   * along each pair. Return avPair with greatest information gain
   */ 
  
  public avPair maximizeInformationGain(ArrayList<Instance> S){
    double currentMaximum = 0;
    double candidateMaximum = 0;
    avPair bestPair = new avPair(0,0);
    for(int i=0;i<9;i++){
      for(int j=2;j<10;j++){
        candidateMaximum = calculateEntropyGain(S,i,j);
        if(candidateMaximum > currentMaximum){
          currentMaximum = candidateMaximum;
          bestPair.setPair(i,j);
        }
      }
    }
    //System.out.println(bestPair);
    return bestPair;
  }
  
  /* Return the missclassification error of S; ie, the number of 
   * minority entires */
  
  public double calculateMisclassificationError(ArrayList<Instance> S){
    double bCount=0;
    double mCount=0;
    int n = S.size();
    for(int i=0;i<n;i++){
      if(S.get(i).benign()){bCount++;}
      else{mCount++;}
    }
    if(bCount < mCount){return bCount;}
    else{return mCount;}
  }
  
  /* Split set S along attribute-value pair and create two new sets. Calculate
   * misclassification error of both, and return their sum */
  
  public double calculateMisclassificationError(ArrayList<Instance> S, int attribute, int value){
    ArrayList<Instance> lSet = new ArrayList<Instance>();
    ArrayList<Instance> rSet = new ArrayList<Instance>();
    int n = S.size();
    for(int i=0;i<n;i++){
      Instance myI = S.get(i);
      if(myI.get(attribute)<=value){
        lSet.add(myI);
      }
      else{
        rSet.add(myI);
      }
    }
    double lError = calculateMisclassificationError(lSet);
    double rError = calculateMisclassificationError(rSet);
    return (lError+rError);
  }
  
  /* Iterate over every attribute-value pair and use 
   * calculateMisclassificationError() to determine the error two sets
   * created by splitting S along these pairs. Then, return avPair
   * that causes lowest misclassification error
   */ 
  
  public avPair minimizeMisclassificationError(ArrayList<Instance> S){
    double currentMinimum=1000;
    double candidateMinimum=0;
    avPair bestPair = new avPair(-1,-1);
    for(int i=0;i<9;i++){
      for(int j=0;j<10;j++){
        candidateMinimum = calculateMisclassificationError(S,i,j);
        if(candidateMinimum < currentMinimum){
          currentMinimum = candidateMinimum;
          bestPair.setPair(i,j);
        }
      }
    }
    return bestPair;
  }
  
  /* Core decision tree building algorithm. If mi=true, use information
   * maximization as splitting criteria; otherwise, use misclassification error.
   * Build a decision tree using training data S, stopping parameter, and 
   * specified splitting criteria
   */ 
  
  public avPair buildDT(ArrayList<Instance> S, int stoppingParameter, boolean mi){
    avPair av = new avPair(-1,-1);
    //Determine best pair to split set
    if(mi){
      av = maximizeInformationGain(S);
    }
    else{
      av = minimizeMisclassificationError(S);
    }
    int n = S.size();
    //if set if empty, return 
    if(n==0){return av;}
 
    /* if entropy/error is zero, set is pure; set node
     * to have that value and return as leaf */
    if(mi){
      if(calculateEntropy(S)==0){
        //
        av.setLeaf(S.get(0).benign());
        return av;
      }
    }
    else{
      if(calculateMisclassificationError(S)==0){
      av.setLeaf(S.get(0).benign());
      return av;
      }
    }
    // if n is less than the stopping criteria or no progress was made,
    // set node to have classification of the majority of instances
    if(n < stoppingParameter || (av.getValue()==0 && av.getAttribute()==0)){
      int bCount = 0;
      int mCount = 0;
      int m = S.size();
      for(int i=0; i<m; i++){
        Instance myI = S.get(i);
        if(myI.benign()){bCount++;}
        else{mCount++;}
      }
      av.setLeaf(bCount>mCount);
      return av;
    }
    
    ArrayList<Instance> lowerS = new ArrayList<Instance>();
    ArrayList<Instance> higherS= new ArrayList<Instance>();
    
    // Otherwise, split into two sets along optimal pair
    
    for(int i=0;i<n;i++){
      if(S.get(i).get((int)av.getAttribute())<=av.getValue()){
        lowerS.add(S.get(i));
        //System.out.println("Lower add " + i);
      }
      else{
        higherS.add(S.get(i));
        //System.out.println("Upper add " + i);
      }
    }
    
    int bCount = 0;
    int mCount = 0;
    int m = S.size();
    for(int i=0; i<m; i++){
      Instance myI = S.get(i);
      if(myI.benign()){bCount++;}
      else{mCount++;}
    }
    // FOR PRUNING: record majority classification of instances in av subtree
    av.setMajority(bCount>mCount);
    // Recursively build subtree
    av.setLeftChild(buildDT(lowerS, stoppingParameter,mi));
    av.setRightChild(buildDT(higherS, stoppingParameter,mi));
    return av;
    
  }  
}

