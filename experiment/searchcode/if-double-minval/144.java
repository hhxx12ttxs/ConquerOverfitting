package schemamatchings.meta.algorithms;

import java.util.Iterator;
import java.util.Vector;

import schemamatchings.meta.agr.AbstractGlobalAggregator;
import schemamatchings.meta.agr.AbstractLocalAggregator;
import schemamatchings.meta.match.AbstractMapping;
import schemamatchings.meta.match.AbstractMatchMatrix;
import schemamatchings.meta.statistics.TAStatistics;

public class CrossThresholdAlgorithm
    extends MatrixDirectWithBoundingAlgorithm
    implements ThresholdAlgorithm, NonUniformMetaAlgorithm {

  protected CrossThresholdAlgorithm(int k, AbstractGlobalAggregator fGlobalArg,
                            AbstractLocalAggregator fLocalArg,
                            AbstractGlobalAggregator hGlobalArg,
                            AbstractLocalAggregator hLocalArg,
                            AbstractMatchMatrix combinedMatrix) {
    super(k, fGlobalArg, fLocalArg, hGlobalArg, hLocalArg, combinedMatrix);
    setAlgorithmName("CrossThreshold Algorithm");
  }

  public void runAlgorithm() throws MetaAlgorithmRunningException {
    try {
      if (isUsingStatistics())
        initStatistics();
      runMatchingAlgorithms(); //run algorithms A1,...Am
        //prepare the combined matrix
      createCombinedMatrix(hGlobalArg);
      if (threshold > 0 && mp != null)
         mp.applyThreshold(threshold);
      this.numOfMatchingAlgorithms++; //one more TKM that runs on M*
      addCombinedMatrix(combinedMatrix);
      if (isNormalizeMatrixes())
        matrixNormalization();
      if (nonUniform)
        initNonUniform();
      for (int i = 0; i < numOfMatchingAlgorithms; i++) {
        //load the TKM algorithm and set it to executing thread
        TKM tkm = (TKM) tkmClass.newInstance();
        maThreads[i].setTKM(tkm);
        maThreads[i].setMatchMatrix(matrixs[i]);
        maThreads[i].start();
      }
    }
    catch (Throwable e) {
      e.printStackTrace();
      abnormalTermination();
      throw new MetaAlgorithmRunningException(e.getMessage());
    }
  }

  public void init(Schema s1, Schema s2, int numOfMatchingAlgorithms,
                   MatchAlgorithm[] algorithms, Class tkmClass) throws
      MetaAlgorithmInitiationException {
    if (s1 == null || s2 == null || numOfMatchingAlgorithms < 1 || algorithms == null ||
        algorithms.length != numOfMatchingAlgorithms || tkmClass == null)
      throw new IllegalArgumentException(
          "Meta Algorithm initiation got illigal arguments");
    try {
      this.s1 = s1;
      this.s2 = s2;
      this.numOfMatchingAlgorithms = numOfMatchingAlgorithms;
      this.lastMappings = new AbstractMapping[numOfMatchingAlgorithms + 1];
      this.algorithms = algorithms;
      this.tkmClass = tkmClass;
      matrixs = new AbstractMatchMatrix[numOfMatchingAlgorithms];
      maThreads = new MetaAlgorithmThread[numOfMatchingAlgorithms + 1];
      for (int i = 0; i < numOfMatchingAlgorithms + 1; i++) {
        maThreads[i] = new MetaAlgorithmThread(this, i);
      }
      initiated = true;
    }
    catch (Throwable e) {
      throw new MetaAlgorithmInitiationException(e.getMessage());
    }
  }

  public void init(Schema s1, Schema s2, int numOfMatchingAlgorithms,
                   MatchAlgorithm[] algorithms, TKM tkm) throws
      MetaAlgorithmInitiationException {
    throw new MetaAlgorithmInitiationException(
        "This initiation is not supported by Hybrid Algorithm\n" +
        "Use instead: init(Schema s1,Schema s2,int numOfMatchingAlgorithms,MatchAlgorithm[] algorithms,Class  tkmClass)");
  }

  private void addCombinedMatrix(AbstractMatchMatrix combinedMatrix) {
    AbstractMatchMatrix[] temp = matrixs;
    matrixs = new AbstractMatchMatrix[numOfMatchingAlgorithms];
    for (int i = 0; i < temp.length; i++)
      matrixs[i] = temp[i];
    matrixs[temp.length] = combinedMatrix;
  }

  protected boolean canHalt() {
    //debug
    //System.out.println("checking if can halt..");
    //***
     double[] localMappingScores = new double[numOfMatchingAlgorithms - 1];
    for (int i = 0; i < numOfMatchingAlgorithms - 1; i++)
      localMappingScores[i] = localArg.clacArgValue( lastMappings[i], matrixs[i]);
    double thresholdTA = globalArg.clacArgValue(localMappingScores);
    double thresholdMD = hLocalArg.clacArgValue(lastMappings[
                                                numOfMatchingAlgorithms - 1],
                                                matrixs[numOfMatchingAlgorithms -
                                                1]);
    double minThreshold = Math.min(thresholdTA, thresholdMD);
    boolean canHalt = isExistKMappingWiteScore(minThreshold);
    if (isUsingStatistics())
      ( (TAStatistics) statistics).setCurrentTopKMappings(currentGeneratedTopK());
    return (minThreshold == 0 || canHalt || stopReached() || checkInfiniteTermination());
  }


  public int progressWith() {
    int with = 0;
    double minVal = Double.MAX_VALUE;
    for (int i = 0; i < numOfMatchingAlgorithms; i++) {
      if (heuristicValues[i] < minVal) {
        with = i;
        minVal = heuristicValues[i];
      }
    }
    if (debugMode){
       System.out.println("Progress with:"+with+" Heuristic value:"+heuristicValues[with]+"\n");
    }
    return with;
  }

  public synchronized void notifyNewMapping(int tid, AbstractMapping mapping) {
//    //perform local and global aggerators calculation
//    //first check if not seen yet this mappings in one of the sorted lists
    newMapping(tid,mapping);
    //*****
     lastMappings[tid] = mapping;
     //debug
     //System.out.println("calling "+tid+" to wait");
     //****
    maThreads[tid].waitForNextStep();
    synchronizer++;
    if (synchronizer == numOfMatchingAlgorithms) {
      synchronizer = 0;
      if (isUsingStatistics())
        statistics.increaseIterationsCount();
      currentStep++;
      //(b) check halt condition
      if (canHalt()) {
        for (int i = 0; i < numOfMatchingAlgorithms; i++) {
          maThreads[i].die();
        }
        //run halt
        finished();
      }
      else {
        for (int i = 0; i < numOfMatchingAlgorithms; i++) {
          //System.out.println("calling "+i+" to continue");
          maThreads[i].continueNextStep();
        }
      }
    }
  }



  public synchronized void notifyNewHeuristicMappings(int tid,AbstractMapping alpha, Vector betas) {
    double[] localMappingScores = new double[numOfMatchingAlgorithms];
    if (lastTidProgressedWith == tid){
      double alphaVal, maxBetaVal = Double.MIN_VALUE, localVal;
      alphaVal = localArg.clacArgValue(alpha, matrixs[tid]);
      Iterator it = betas.iterator();
      while (it.hasNext()) {
        localVal = localArg.clacArgValue((AbstractMapping) it.next(),
            matrixs[tid]);
        maxBetaVal = maxBetaVal > localVal ? maxBetaVal : localVal;
      }
      //System.out.println("tid:"+tid+" beta val:"+maxBetaVal);

      lastTidHeuristicXi[tid] = Math.max(maxBetaVal, alphaVal); //save heoristic valuation - epsilon

    }
    if (tid != numOfMatchingAlgorithms-1){//TA
      for (int i = 0; i < numOfMatchingAlgorithms-1; i++)
        localMappingScores[i] = (i == tid) ? lastTidHeuristicXi[tid] :localArg.clacArgValue(lastMappings[i], matrixs[i]);
      //added 8/2/04
      lastLocalXiScores = localMappingScores;
      //end added
      heuristicValues[tid] = globalArg.clacArgValue(lastLocalXiScores);
    }
    else{//MDB
      heuristicValues[tid] = lastTidHeuristicXi[tid];
    }

    //debug  debugString.append(
    //if (lastTidProgressedWith != -1)
    if (debugMode){
      System.out.print("tid:"+tid+" delta:"+(lastTidHeuristicXi[tid] - localArg.clacArgValue(lastMappings[tid],matrixs[tid]))+"\n");
      System.out.println("tid:"+tid+" TA value:"+heuristicValues[tid]);
    }
//      System.out.println("Winner:"+tid);
      //**
    //debug
       //System.out.println(tid+" heuristic value:"+heuristicValues[tid]);
       //****
    maThreads[tid].waitForNextStep();
    synchronizer++;
    if (synchronizer == numOfMatchingAlgorithms) {
      synchronizer = 0;
      if (isUsingStatistics())
        statistics.increaseIterationsCount();
      currentStep++;
      //progress non uniformly with one tkm
      int progressWith = progressWith();
      lastTidProgressedWith = progressWith;
      try {
        //debug
        //System.out.println("continue with:"+progressWith);
        //****
        lastMappings[progressWith] = maThreads[progressWith].continueInOneStep();
      }
      catch (Throwable e) {
        e.printStackTrace();
      }
      //(a)
      newMapping(tid,lastMappings[progressWith]);
      //(b) check halt condition
      if (canHalt()) {
        for (int i = 0; i < numOfMatchingAlgorithms; i++) {
          maThreads[i].die();
        }
        //run halt
        finished();
      }
      else {
        for (int i = 0; i < numOfMatchingAlgorithms; i++) {
          //System.out.println("calling "+i+" to continue");
          maThreads[i].continueNextStep();
        }
      }
    }
  }

  protected void initStatistics() {
    super.initStatistics();
    ( (TAStatistics) statistics).setThreadsCount(numOfMatchingAlgorithms + 1);
  }


  public  void useStatistics(){
    statistics = new TAStatistics(this,s1.getName(),s2.getName());
  }


  public void reset() {}

}

