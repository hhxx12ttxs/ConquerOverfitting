package weka.classifiers.bayes;

import weka.classifiers.Classifier;
import weka.classifiers.DistributionClassifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.Evaluation;
import weka.classifiers.UpdateableClassifier;
import java.io.*;
import java.util.*;
import weka.core.*;
import weka.estimators.*;

public class PBayes extends NaiveBayes{
	public String Prob;
	double [] m_probs;
  /**
   * Generates the classifier.
   * @param instances set of instances serving as training data 
   * @exception Exception if the classifier has not been generated successfully
   */
  public void buildClassifier(Instances instances) throws Exception {
    if(instances.checkForStringAttributes())throw new UnsupportedAttributeTypeException("Cannot handle string attributes!");
    if(instances.classAttribute().isNumeric())throw new UnsupportedClassTypeException("PBayes: Class is numeric!");
    m_NumClasses = instances.numClasses();
    if(m_NumClasses<0)throw new Exception ("Dataset has no class attribute");
    // Copy the instances
    m_Instances = new Instances(instances);
    // Reserve space for the distributions
    m_Distributions=new Estimator[m_Instances.numAttributes()-1][m_Instances.numClasses()];
    m_ClassDistribution=new DiscreteEstimator(m_Instances.numClasses(),true);
    int attIndex = 0;
    Enumeration enum = m_Instances.enumerateAttributes();
    while (enum.hasMoreElements()) {
      Attribute attribute = (Attribute) enum.nextElement();
      // If the attribute is numeric, determine the estimator 
      // numeric precision from differences between adjacent values
      double numPrecision = DEFAULT_NUM_PRECISION;
      if (attribute.type() == Attribute.NUMERIC) {
	m_Instances.sort(attribute);
	if ((m_Instances.numInstances() > 0)
	    && !m_Instances.instance(0).isMissing(attribute)) {
	  double lastVal = m_Instances.instance(0).value(attribute);
	  double currentVal, deltaSum = 0;
	  int distinct = 0;
	  for (int i = 1; i < m_Instances.numInstances(); i++) {
	    Instance currentInst = m_Instances.instance(i);
	    if (currentInst.isMissing(attribute)) {
	      break;
	    }
	    currentVal = currentInst.value(attribute);
	    if (currentVal != lastVal) {
	      deltaSum += currentVal - lastVal;
	      lastVal = currentVal;
	      distinct++;
	    }
	  }
	  if (distinct > 0) {
	    numPrecision = deltaSum / distinct;
	  }
	}
      }
      for (int j = 0; j < m_Instances.numClasses(); j++) {
	switch (attribute.type()) {
	case Attribute.NUMERIC: 
	  if(m_UseKernelEstimator)m_Distributions[attIndex][j]=new KernelEstimator(numPrecision);
	  else m_Distributions[attIndex][j]=new NormalEstimator(numPrecision);
	  break;
	case Attribute.NOMINAL:
	  m_Distributions[attIndex][j]=new DiscreteEstimator(attribute.numValues(),true);
	  break;
	default:
	  throw new Exception("Attribute type unknown to PBayes");
	}
      }
      attIndex++;
    }
    if(Prob.length()>0)updateClassifier(m_Instances,Prob);
    else updateClassifier(m_Instances);
    m_probs=new double[m_NumClasses];
    updateClassifier();
    // Save space
    m_Instances = new Instances(m_Instances, 0);
  }

  public void updateClassifier(Instances instances,String Prob)throws Exception{
    BufferedReader reader=new BufferedReader(new FileReader(Prob));
    Enumeration enumInsts=instances.enumerateInstances();
    Attribute classAttribute=instances.classAttribute();
    while (enumInsts.hasMoreElements()) {
      Instance instance = (Instance) enumInsts.nextElement();
      String line=reader.readLine();
      String [] lines=line.split("\\s+");
      updateClassifier(instance,classAttribute.index(lines[1]),Double.parseDouble(lines[2]));
    }
  }
  // NaiveBayes
  public void updateClassifier(Instances instances)throws Exception{
    Enumeration enumInsts=instances.enumerateInstances();
    Attribute classAttribute=instances.classAttribute();
    while (enumInsts.hasMoreElements()) {
      Instance instance = (Instance) enumInsts.nextElement();
      updateClassifier(instance);
    }
  }
  /**
   * Updates the classifier with the given instance.
   * @param instance the new training instance to include in the model 
   * @exception Exception if the instance could not be incorporated in the model.
   */
  public void updateClassifier(Instance instance,int classIndex,double prob)throws Exception{
	  int [] cs={classIndex,1-classIndex};
	  double [] ps={instance.weight()*prob,instance.weight()*(1-prob)};
	  Enumeration enumAtts=m_Instances.enumerateAttributes();
	  int attIndex=0;
	  while(enumAtts.hasMoreElements()){
		  Attribute attribute=(Attribute)enumAtts.nextElement();
		  if(!instance.isMissing(attribute)){
			  m_Distributions[attIndex][cs[0]].addValue(instance.value(attribute),ps[0]);
			  m_Distributions[attIndex][cs[1]].addValue(instance.value(attribute),ps[1]);
		  }
		  attIndex++;
	  }
	  m_ClassDistribution.addValue(cs[0],ps[0]);
	  m_ClassDistribution.addValue(cs[1],ps[1]);
  }
  // NaiveBayes
  public void updateClassifier(Instance instance)throws Exception{
	  updateClassifier(instance,(int)instance.classValue(),1);
  }
  public void updateClassifier(){
	  for(int j=0;j<m_NumClasses;j++)m_probs[j]=m_ClassDistribution.getProbability(j);
  }

  /**
   * Calculates the class membership probabilities for the given test instance.
   * @param instance the instance to be classified
   * @return predicted class probability distribution
   * @exception Exception if there is a problem generating the prediction
   */
  public double [] distributionForInstance(Instance instance)throws Exception{
    double [] probs=new double[m_NumClasses];
    System.arraycopy(m_probs,0,probs,0,m_NumClasses);
    Enumeration enumAtts = instance.enumerateAttributes();
    int attIndex=0,atts=0;
	while(enumAtts.hasMoreElements()){
		Attribute attribute=(Attribute)enumAtts.nextElement();
		if(!instance.isMissing(attribute)){
			atts++;
			double temp,max=0;
			for(int j=0;j<m_NumClasses;j++){
				temp=Math.max(1e-75,m_Distributions[attIndex][j].getProbability(instance.value(attribute)));
				probs[j]*=temp;
				if(probs[j]>max)max=probs[j];
				if(Double.isNaN(probs[j])){
					throw new Exception("NaN returned from estimator for attribute "
							+attribute.name()+":\n"
							+m_Distributions[attIndex][j].toString());
				}
			}
			if((max>0)&&(max<1e-75)){//Danger of probability underflow
				for(int j=0;j<m_NumClasses;j++)probs[j]*=1e75;
			}
		}
		attIndex++;
	}
	double power=1.0/atts;
	//for(int i=0;i<m_NumClasses;i++)probs[i]=Math.pow(probs[i],power);
    Utils.normalize(probs);
    return probs;
  }

  /**
   * Returns an enumeration describing the available options.
   * @return an enumeration of all the available options.
   */
  public Enumeration listOptions() {
    Vector newVector = new Vector(2);
    newVector.addElement(
    new Option("\tUse kernel density estimator rather than normal\n"
	       +"\tdistribution for numeric attributes",
	       "K", 0,"-K"));
    newVector.addElement(new Option("\tSet Probability data file. Each line represent one instance's probabilities to be all classes seperated by space.","Prob",0,"-Prob"));
    return newVector.elements();
  }

  /**
   * Parses a given list of options. Valid options are:<p>
   *
   * -K <br>
   * Use kernel estimation for modelling numeric attributes rather than
   * a single normal distribution.<p>
   *
   * @param options the list of options as an array of strings
   * @exception Exception if an option is not supported
   */
  public void setOptions(String[] options) throws Exception {
    m_UseKernelEstimator = Utils.getFlag('K', options);
    Prob=Utils.getOption("Prob",options);
  }

  /**
   * Gets the current settings of the classifier.
   *
   * @return an array of strings suitable for passing to setOptions
   */
  public String [] getOptions() {
    String [] options = new String [2];
    int current = 0;
    options[current++]="-Prob";
    if (m_UseKernelEstimator) {
      options[current++] = "-K";
    }
    while (current < options.length) {
      options[current++] = "";
    }
    return options;
  }

  /**
   * Returns a description of the classifier.
   *
   * @return a description of the classifier as a string.
   */
  public String toString() {
    StringBuffer text = new StringBuffer();
    text.append("PBayes Classifier");
    if (m_Instances == null) {
      text.append(": No model built yet.");
    } else {
      try {
	for (int i = 0; i < m_Distributions[0].length; i++) {
	  text.append("\n\nClass " + m_Instances.classAttribute().value(i) +
		      ": Prior probability = " + Utils.
		      doubleToString(m_ClassDistribution.getProbability(i),
				     4, 2) + "\n\n");
	  Enumeration enumAtts = m_Instances.enumerateAttributes();
	  int attIndex = 0;
	  while (enumAtts.hasMoreElements()) {
	    Attribute attribute = (Attribute) enumAtts.nextElement();
	    text.append(attribute.name() + ":  " 
			+ m_Distributions[attIndex][i]);
	    attIndex++;
	  }
	}
      } catch (Exception ex) {
	text.append(ex.getMessage());
      }
    }
    return text.toString();
  }
  
  /**
   * Main method for testing this class.
   *
   * @param argv the options
   */
  public static void main(String [] argv) {
    try {
      System.out.println(Evaluation.evaluateModel(new PBayes(), argv));
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println(e.getMessage());
    }
  }
}

