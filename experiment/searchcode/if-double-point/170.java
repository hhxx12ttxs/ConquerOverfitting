<<<<<<< HEAD
package ncsa.d2k.modules.core.prediction.decisiontree.c45;

import java.beans.PropertyVetoException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import ncsa.d2k.core.modules.ReentrantComputeModule;
import ncsa.d2k.modules.core.datatype.parameter.ParameterPoint;
import ncsa.d2k.modules.core.datatype.table.ExampleTable;
import ncsa.d2k.modules.core.datatype.table.Table;
import ncsa.d2k.modules.core.datatype.table.util.TableUtilities;
import ncsa.d2k.modules.core.prediction.decisiontree.CategoricalDecisionTreeNode;
import ncsa.d2k.modules.core.prediction.decisiontree.DecisionTreeNode;
import ncsa.d2k.modules.core.prediction.decisiontree.NumericDecisionTreeNode;

/**
 Build a C4.5 decision tree.  The tree is build recursively, always choosing
 the attribute with the highest information gain as the root.  The
 gain ratio is used, whereby the information gain is divided by the
 information given by the size of the subsets that each branch creates.
 This prevents highly branching attributes from always becoming the root.
 The minimum number of records per leaf can be specified.  If a leaf is
 created that has less than the minimum number of records per leaf, the
 parent will be turned into a leaf itself.
 @author David Clutter
 */
public class C45TreeBuilderOPT
    extends ReentrantComputeModule {

  public static void main(String[] args) {
    //double d1 = (double)9/(double)14;
    //double d2 = (double)5/(double)14;
    //double dd[] = {d1, d2};
    //System.out.println(lg(2));
    //System.out.println(entropy(dd));
    int[] tals = {
        9, 5};
    System.out.println(info(tals));
  }

  /**
   Calculate the entropy given probabilities.  The entropy is the amount
   of information conveyed by a potential split.
   entropy(p1, p2,...pn) = -p1*lg(p1) - p2*lg(p2) -...-pn*lg(pn)
   @param data the probabilities
   @return the information conveyed by the probabilities
    /
    private static final double entropy(double[] data) {
   double retVal = 0;
   for(int i = 0; i < data.length; i++) {
    retVal += -1*data[i]*lg(data[i]);
   }
   return retVal;
    }*/

  /**
   Return the binary log of a number.  This is defined as
   x such that 2^x = d
   @param d the number to take the binary log of
   @return the binary log of d
   */
  private static final double lg(double d) {
    return Math.log(d) / Math.log(2.0);
  }

  private static final double info(int[] tallies) {
    int total = 0;
    for (int i = 0; i < tallies.length; i++) {
      total += tallies[i];

    }
    double dtot = (double) total;

    double retVal = 0;
    for (int i = 0; i < tallies.length; i++) {
      retVal -= ( (tallies[i] / dtot) * lg(tallies[i] / dtot));

    }
    return retVal;
  }

  /**
   * Exapand the size of an array by one.  Creates a new array and copies
   * all the old entries.
   * @param orig
   * @return an array of size orig.length+1 with all the entries from orig copied over
   */
  private static int[] expandArray(int[] orig) {
    int[] newarray = new int[orig.length + 1];
    System.arraycopy(orig, 0, newarray, 0, orig.length);
    return newarray;
  }

  /**
   The threshold for information gain
    /
    private double infoGainThreshold = 0.005;
    public void setInfoGainThreshold(double d) {
   infoGainThreshold = d;
    }
    public double getInfoGainThreshold() {
   return infoGainThreshold;
    }*/

  /**
   * Turns debugging statements on or off.
   */
  private boolean debug = false;

  protected void setDebug(boolean b) {
    debug = b;
  }

  protected boolean getDebug() {
    return debug;
  }

  /*private boolean useGainRatio = true;
    public void setUseGainRatio(boolean b) {
   useGainRatio = b;
    }
    public boolean getUseGainRatio() {
   return useGainRatio;
    }*/

//  private int minimumRecordsPerLeaf = 2;
  private double minimumRatioPerLeaf = 0.001;

  /*public void setMinimumRecordsPerLeaf(int num) {
   minimumRecordsPerLeaf = num;
    }*/

/*  public int getMinimumRecordsPerLeaf() {
    return minimumRecordsPerLeaf;
  }*/

    protected void setMinimumRatioPerLeaf(double d) throws PropertyVetoException {
	if( d < 0 || d > 1)
	    throw new PropertyVetoException("minimumRatioPerLeaf must be between 0 and 1",null);
	minimumRatioPerLeaf = d;
    }

  protected double getMinimumRatioPerLeaf() {
    return minimumRatioPerLeaf;
  }

  private static NumberFormat nf;
  private static final String LESS_THAN = " < ";
  private static final String GREATER_THAN_EQUAL_TO = " >= ";
  private static final int NUMERIC_VAL_COLUMN = 0;
  private static final int NUMERIC_OUT_COLUMN = 1;

  /**
   Calculate the average amount of information needed to identify a class
   of the output column for a numeric attribute.  This is the sum of
   the information given by the examples less than the split value and the
   information given by the examples greater than or equal to the split
   value.
   @param vt the data set
   @param splitVal the split
   @param examples the list of examples, which correspond to rows of
    the table
   @param attCol the column we are interested in
   @param outCol the output column
   @return the information given by a numeric attribute with the given
    split value
   */
  private double numericAttributeInfo(Table t, double splitVal,
                                      int[] examples, int attCol, int outCol) {

    int lessThanTot = 0;
    int greaterThanTot = 0;

    int[] lessThanTally = new int[0];
    int[] greaterThanTally = new int[0];
    HashMap lessThanIndexMap = new HashMap();
    HashMap greaterThanIndexMap = new HashMap();

    // for each example, check if it is less than or greater than/equal to
    // the split point.
    // increment the proper tally
    for (int i = 0; i < examples.length; i++) {
      int idx = examples[i];

      double val = t.getDouble(idx, attCol);
      String out = t.getString(idx, outCol);

      int loc;
      if (val < splitVal) {
        //Integer in = (Integer)lessThanIndexMap.get(out);
        if (lessThanIndexMap.containsKey(out)) {
          //if(in != null) {
          Integer in = (Integer) lessThanIndexMap.get(out);
          loc = in.intValue();
          lessThanTally[loc]++;
        }
        // found a new one..
        else {
          lessThanIndexMap.put(out, new Integer(lessThanIndexMap.size()));
          lessThanTally = expandArray(lessThanTally);
          lessThanTally[lessThanTally.length - 1] = 1;
        }
        lessThanTot++;
      }
      else {
        //Integer in = (Integer)greaterThanIndexMap.get(out);
        if (greaterThanIndexMap.containsKey(out)) {
          //if(in != null) {
          Integer in = (Integer) greaterThanIndexMap.get(out);
          loc = in.intValue();
          greaterThanTally[loc]++;
        }
        // found a new one..
        else {
          greaterThanIndexMap.put(out, new Integer(greaterThanIndexMap.size()));
          greaterThanTally = expandArray(greaterThanTally);
          greaterThanTally[greaterThanTally.length - 1] = 1;
        }
        greaterThanTot++;
      }
    }

    // now that we have tallies of the outputs for this att value
    // we can calculate the information value.

    double linfo = info(lessThanTally);
    double ginfo = info(greaterThanTally);

    return (lessThanTot / (double) examples.length) * linfo +
        (greaterThanTot / (double) examples.length) * ginfo;

    // get the probablities for the examples less than the split
    //double[] lesserProbs = new double[lessThanTally.length];
    //for(int i = 0; i < lessThanTally.length; i++)
    //	lesserProbs[i] = ((double)lessThanTally[i])/((double)lessThanTot);

    //double[] greaterProbs = new double[greaterThanTally.length];
    //for(int i = 0; i < greaterThanTally.length; i++)
    //	greaterProbs[i] = ((double)greaterThanTally[i])/((double)greaterThanTot);

    // return the sum of the information given on each side of the split
    //return (lessThanTot/(double)examples.length)*entropy(lesserProbs) +
    //	(greaterThanTot/(double)examples.length)*entropy(greaterProbs);
  }

  /**
   Find the best split value for the given column with the given examples.
   The best split value will be the one that gives the maximum information.
   This is found by sorting the set of examples and testing each possible
   split point.  (The possible split points are located halfway between
   unique values in the set of examples)  The information on each possible
   split is then calculated.
   @param t the table
   @param attCol the index of the attribute column
   @param outCol the index of the output column
   @param examples the list of examples, which correspond to rows of the
    table
   @return the split value for this attribute that gives the maximum
    information
   */
  private EntrSplit findSplitValue(Table t, int attCol, int outCol,
                                   int[] examples) {
    // copy the examples into a new table
    //DoubleColumn dc = new DoubleColumn(examples.length);
    //StringColumn sc = new StringColumn(examples.length);

    int[] cols = {
        attCol};

    int[] examCopy = new int[examples.length];
    System.arraycopy(examples, 0, examCopy, 0, examples.length);
    int[] sortedExamples = TableUtilities.multiSort(t, cols, examCopy);

    //double mean = TableUtilities.mean(t, attCol);

    /*for(int i = 0; i < examples.length; i++) {
              int rowIdx = examples[i];
      dc.setDouble(t.getDouble(rowIdx, attCol), i);
     sc.setString(t.getString(rowIdx, outCol), i);
       }
       Column[] cols = {dc, sc};
       MutableTableImpl vt = (MutableTableImpl)DefaultTableFactory.getInstance().createTable(cols);
       // sort the table
       vt.sortByColumn(0);
     */

    // each row of the new table is an example
    /*int[] exams = new int[vt.getNumRows()];
       for(int i = 0; i < vt.getNumRows(); i++)
        exams[i] = i;
     */

    // now test the possible split values.  these are the half-way point
    // between two adjacent values.  keep the highest.
    double splitValue;
    double highestGain = Double.NEGATIVE_INFINITY;

    // this is the return value
    EntrSplit split = new EntrSplit();

    double lastTest = /*vt*/t.getDouble(sortedExamples[0], attCol);
    boolean allSame = true;
    double baseGain = outputInfo(t, outCol, examples);
    //double baseGain = outputInfo(t, outCol, sortedExamples);

/*    double gain = baseGain = numericAttributeInfo(t, mean,
                                                  examples, attCol, outCol);

    double spliter = splitInfo(t, attCol, mean, examples);
    gain /= spliter;

    split.gain = gain;
    split.splitValue = mean;
    return split;
 */

    // test the halfway point between the last value and the current value
    for(int i = 1; i < sortedExamples.length; i++) {
     double next = t.getDouble(sortedExamples[i], attCol);
     if(next != lastTest) {
      allSame = false;
      double testSplitValue = ((next-lastTest)/2)+lastTest;
      // count up the number greater than and the number less than
      // the split value and calculate the information gain
      //double gain = outputEntropy(table, outputs[0], examples);
      //double gain = baseGain - numericAttributeInfo(vt, testSplitValue,
      //	exams, NUMERIC_VAL_COLUMN, NUMERIC_OUT_COLUMN);
      double gain = baseGain - numericAttributeInfo(t, testSplitValue,
       sortedExamples, attCol, outCol);
      //double spliter = splitInfo(vt, NUMERIC_VAL_COLUMN, testSplitValue, exams);
      double spliter = splitInfo(t, attCol, testSplitValue, sortedExamples);
      gain /= spliter;
      lastTest = next;
      //double gain = numericAttributeEntropy(vt, testSplitValue,
      //	exams, NUMERIC_VAL_COLUMN, NUMERIC_OUT_COLUMN);
      // if the gain is better than what we have already seen, save
      // it and the splitValue
      if(gain >= highestGain) {
       highestGain = gain;
       splitValue = testSplitValue;
       split.gain = gain;
       split.splitValue = testSplitValue;
      }
     }
       }
       if(allSame)
     return null;
       return split;
  }

  /**
   Find the information gain for a numeric attribute.  The best split
   value is found, and then the information gain is calculated using
   the split value.
   @param t the table
   @param attCol the index of the attribute column
   @param outCol the index of the output column
   @param examples the list of examples, which correspond to rows of
    the table
   @return an object holding the gain and the best split value of
    the column
   */
  private EntrSplit numericGain(Table t, int attCol, int outCol, int[] examples) {
    //if(debug)
    //	System.out.println("Calc numericGain: "+t.getColumnLabel(attCol)+" size: "+examples.length+" out: "+t.getColumnLabel(outCol));
    double gain = outputInfo(t, outCol, examples);

    //double splitVal = findSplitValue(attCol, examples);
    EntrSplit splitVal = findSplitValue(t, attCol, outCol, examples);
    //double numEntr = numericAttributeEntropy(table, splitVal.splitValue, examples,
    //	attCol, outputs[0]);

    //gain -= splitVal.gain;
    //if(useGainRatio) {
    //double spliter = splitInfo(table, attCol, splitVal.splitValue, examples);
    //gain /= spliter;
    //}

    return splitVal;
  }

  /**
   A simple structure to hold the gain and split value of a column.
   */
  private final class EntrSplit {
    double splitValue;
    double gain;

    EntrSplit() {}

    EntrSplit(double s, double g) {
      splitValue = s;
      gain = g;
    }
  }

  /**
   Find the information gain for a categorical attribute.  The gain
   ratio is used, where the information gain is divided by the
   split information.  This prevents highly branching attributes
   from becoming dominant.
   @param t
   @param attCol the index of the attribute column
   @param examples the list of examples, which correspond to rows of
    the table
   @return the gain of the column
   */
  private double categoricalGain(Table t, int attCol, int outCol,
                                 int[] examples) {
    //if(debug)
    //	System.out.println("Calc categoricalGain: "+table.getColumnLabel(attCol)+" size: "+examples.length);

    // total entropy of the class column -
    // entropy of each of the possibilities of the attribute
    // (p =#of that value, n=#rows)

    // entropy of the class col
    double gain = outputInfo(t, outCol, examples);

    // now subtract the entropy for each unique value in the column
    // ie humidity=high, count # of yes and no
    // humidity=low, count # of yes and no
    String[] vals = uniqueValues(t, attCol, examples);
    for (int i = 0; i < vals.length; i++) {
      gain -= categoricalAttributeInfo(t, attCol, outCol, vals[i], examples);

    }
    double sInfo = splitInfo(t, attCol, 0, examples);
    // divide the information gain by the split info
    gain /= sInfo;
    return gain;
  }

  /**
   Calculate the entropy of a specific value of an attribute.
   @param colNum the index of the attribute column
   @param attValue the value of the attribute we are interested in
   @param examples the list of examples, which correspond to rows of
    the table
   @return the information given by attValue
   */
  private double categoricalAttributeInfo(Table t, int colNum, int outCol,
                                          String attValue, int[] examples) {

    double tot = 0;
    int[] outtally = new int[0];
    HashMap outIndexMap = new HashMap();

    for (int i = 0; i < examples.length; i++) {
      int idx = examples[i];

      String s = t.getString(idx, colNum);

      if (s != null && s.equals(attValue)) {
        String out = t.getString(idx, outCol);

        if (outIndexMap.containsKey(out)) {
          Integer in = (Integer) outIndexMap.get(out);
          outtally[in.intValue()]++;
        }
        else {
          outIndexMap.put(out, new Integer(outIndexMap.size()));
          outtally = expandArray(outtally);
          outtally[outtally.length - 1] = 1;
        }
        tot++;
      }
    }

    return (tot / (double) examples.length) * info(outtally);
  }

  /**
   Get the unique values of the examples in a column.
   @param colNum the index of the attribute column
   @param examples the list of examples, which correspond to rows of
    the table
   @return an array containing all the unique values for examples in
    this column
   */
  private static String[] uniqueValues(Table t, int colNum, int[] examples) {
    int numRows = examples.length;

    // count the number of unique items in this column
    HashSet set = new HashSet();
    for (int i = 0; i < numRows; i++) {
      int rowIdx = examples[i];
      String s = t.getString(rowIdx, colNum);
      if (!set.contains(s)) {
        set.add(s);
      }
    }

    String[] retVal = new String[set.size()];
    int idx = 0;
    Iterator it = set.iterator();
    while (it.hasNext()) {
      retVal[idx] = (String) it.next();
      idx++;
    }
    return retVal;
  }

  /**
   Determine the split info.  This is the information given by the
   number of branches of a node.  The size of the subsets that each
   branch creates is calculated and then the information is calculated
   from that.
   @param colNum the index of the attribute column
   @param splitVal the split value for a numeric attribute
   @param examples the list of examples, which correspond to rows of
    the table
   @return the information value of the branchiness of this attribute
   */
  private double splitInfo(Table t, int colNum, double splitVal, int[] examples) {
    int numRows = examples.length;
    double[] probs;

    int[] tallies;

    // if it is a numeric column, there will be two branches.
    // count up the number of examples less than and greater
    // than the split value
    if (t.isColumnScalar(colNum)) {
      int lessThanTally = 0;
      int greaterThanTally = 0;

      for (int i = 0; i < numRows; i++) {
        int rowIdx = examples[i];
        double d = t.getDouble(rowIdx, colNum);
        if (d < splitVal) {
          lessThanTally++;
        }
        else {
          greaterThanTally++;
        }
      }
      tallies = new int[2];
      tallies[0] = lessThanTally;
      tallies[1] = greaterThanTally;
    }
    // otherwise it is nominal.  count up the number of
    // unique values, because there will be a branch for
    // each unique value
    else {
      HashMap map = new HashMap();
      /*int[]*/tallies = new int[0];
      for (int i = 0; i < numRows; i++) {
        int rowIdx = examples[i];
        String s = t.getString(rowIdx, colNum);
        if (!map.containsKey(s)) {
          map.put(s, new Integer(tallies.length));
          tallies = expandArray(tallies);
          tallies[tallies.length - 1] = 1;
        }
        else {
          Integer ii = (Integer) map.get(s);
          tallies[ii.intValue()]++;
        }
      }
    }

    // calculate the information given by the branches
    return info(tallies);
  }

  /**
   Determine the entropy of the output column.
   @param colNum the index of the attribute column
   @param examples the list of examples, which correspond to rows of
    the table
   @return the entropy of the output column
   */
  private double outputInfo(Table t, int colNum, int[] examples) {
    double numRows = (double) examples.length;

    // count the number of unique items in this column
    int[] tallies = new int[0];
    HashMap map = new HashMap();
    for (int i = 0; i < numRows; i++) {
      int rowIdx = examples[i];
      String s = t.getString(rowIdx, colNum);
      if (map.containsKey(s)) {
        Integer in = (Integer) map.get(s);
        int loc = in.intValue();
        tallies[loc]++;
      }
      else {
        map.put(s, new Integer(tallies.length));
        tallies = expandArray(tallies);
        tallies[tallies.length - 1] = 1;
      }
    }

    return info(tallies);
  }

  /**
   Return the column number of the attribute with the highest gain from
   the available columns.  If none of the attributes has a gain higher
   than the threshold, return null
   @param attributes the list of available attributes, which correspond to
    columns of the table
   @param examples the list of examples, which correspond to rows of
    the table
   @return an object containing the index of the column with the highest
    gain and (if numeric) the best split for that column
   */
  private ColSplit getHighestGainAttribute(Table t, int[] attributes,
                                           int outCol, int[] examples) {
    if (attributes.length == 0 || examples.length == 0) {
      return null;
    }
    ArrayList list = new ArrayList();

    int topCol = 0;
    double highestGain = Double.NEGATIVE_INFINITY;

    ColSplit retVal = new ColSplit();
    // for each available column, calculate the entropy
    for (int i = 0; i < attributes.length; i++) {
      int col = attributes[i];
      // nominal data
      if (t.isColumnNominal(col)) {
        double d = categoricalGain(t, col, outCol, examples);
        if (d > highestGain) {
          highestGain = d;
          retVal.col = col;
        }
      }
      // numeric column
      else {
        EntrSplit sce = numericGain(t, col, outCol, examples);
        if (sce != null && sce.gain > highestGain) {
          highestGain = sce.gain;
          retVal.col = col;
          retVal.splitValue = sce.splitValue;
        }
      }
    }

    return retVal;
  }

  /**
   A simple structure to hold a column index and the best split value of
   an attribute.
   */
  private final class ColSplit {
    int col;
    double splitValue;
  }

  // the table that contains the data set
  protected transient ExampleTable table;
  // the indices of the columns with output variables
  protected transient int[] outputs;

  protected transient int numExamples;

  public String getModuleInfo() {
/*    String s = "Build a C4.5 decision tree.  The tree is build recursively, ";
    s += "always choosing the attribute with the highest information gain ";
    s += "as the root.  The gain ratio is used, whereby the information ";
    s += "gain is divided by the information given by the size of the ";
    s += "subsets that each branch creates.  This prevents highly branching ";
    s += "attributes from always becoming the root.  A threshold can be ";
    s += "used to prevent the tree from perfect fitting the training data.  ";
    s += "If the information gain ratio is not above the threshold, a leaf ";
    s += "will be created instead of a node.  This will may cause some ";
    s += "incorrect classifications, but will keep the tree from overfitting ";
    s += "the data.  The threshold should be set low.  The default value ";
    s += "should suffice for most trees.  The threshold is a property of ";
    s += "this module.";
    return s;
 */
    String s = "<p>Overview: Builds a decision tree.  The tree is built "+
        "recursively using the information gain metric to choose the root."+
        "<p>Detailed Description: Builds a decision tree using the C4.5 "+
        "algorithm.  The decision tree is built recursively, choosing the "+
        "attribute with the highest information gain as the root.  For "+
        "a nominal input, the node will have branches for each unique value "+
        "in the nominal column.  For scalar inputs, a binary node is created "+
        "with a split point chosen that offers the greatest information gain. "+
        "The complexity of building the entire tree is O(mn log n) where m is "+
        " the number of inputs and n is the number of examples. "+
        "The choosing of split points for a scalar input is potentially an "+
        "O(n log n) operation at each node of the tree."+
        "<p>References: C4.5: Programs for Machine Learning by J. Ross Quinlan"+
        "<p>Data Type Restrictions: This module will only classify examples with "+
        "nominal outputs."+
        "<p>Data Handling: This module does not modify the input data."+
        "<p>Scalability: The selection of split points for scalar inputs is "+
        "potentially an O(n log n) operation at each node of the tree.  The "+
        "selection of split points for nominal inputs is an O(n) operation.";
        return s;
  }

  // C4.5:Programs for Machine Learning J. Ross Quinlan

  public String getModuleName() {
    return "Optimized C4.5 Tree Builder";
  }

  public String getInputInfo(int i) {
    String in = "An ExampleTable to build a decision tree from. ";
    in += "Only one output feature is used.";
    if (i == 0)
	return in;
    else
	return "Point in Parameter Space to control the minimum leaf ratio.";
  }

  public String getInputName(int i) {
      if( i == 0)
	  return "Example Table";
      else
	  return "Minimum Leaf Ratio";
  }

  public String getOutputInfo(int i) {
    if (i == 0) {
      return "The root of the decision tree built by this module.";
    }
    else {
      return " ";
    }
  }

  public String getOutputName(int i) {
    if (i == 0) {
      return "Decision Tree Root";
    }
    else {
      return "";
    }
  }

  public String[] getInputTypes() {
    String[] in = {
        "ncsa.d2k.modules.core.datatype.table.ExampleTable",
        "ncsa.d2k.modules.core.datatype.parameter.ParameterPoint"};
    return in;
  }

  public String[] getOutputTypes() {
    String[] out = {
        "ncsa.d2k.modules.core.prediction.decisiontree.DecisionTreeNode" };

    return out;
  }

  public void endExecution() {
    super.endExecution();
    table = null;
    outputs = null;
  }

  static {
    nf = NumberFormat.getInstance();
    nf.setMaximumFractionDigits(5);
  }

  /**
   Build the decision tree
   */
  public void doit() throws Exception {
    table = (ExampleTable) pullInput(0);
    numExamples = table.getNumRows();
    ParameterPoint pp = (ParameterPoint)pullInput(1);

    if(pp == null)
      throw new Exception(getAlias()+": Parameter Point was not found!");

/*    HashMap nameToIndexMap = new HashMap();

    for(int i = 0; i < pp.getNumParameters(); i++) {
      String name = pp.getName(i);
      nameToIndexMap.put(name, new Integer(i));
    }*/

/*    int idx = (int)pp.getValue(C45ParamSpaceGenerator.MIN_RECORDS);
    if(idx == null)
      throw new Exception(getAlias()+": Minimum Number of records per leaf not defined!");
 */
    //this.minimumRecordsPerLeaf = pp.getValue(C45ParamSpaceGenerator.MIN_RATIO);
    setMinimumRatioPerLeaf(pp.getValue(C45ParamSpaceGenerator.MIN_RATIO));
    //if(minimumRecordsPerLeaf < 1)
    //  throw new Exception(getAlias()+": Must be at least one record per leaf!");

    int[] inputs = table.getInputFeatures();

    if (inputs == null || inputs.length == 0) {
      throw new Exception(getAlias()+": No inputs were defined!");
    }

    outputs = table.getOutputFeatures();

    if (outputs == null || outputs.length == 0) {
      throw new Exception("No outputs were defined!");
    }

    if (outputs.length > 1) {
      System.out.println("Only one output feature is allowed.");
      System.out.println("Building tree for only the first output variable.");
    }

    if (table.isColumnScalar(outputs[0])) {
      throw new Exception(getAlias()+" C4.5 Decision Tree can only predict nominal values.");
    }

    // the set of examples.  the indices of the example rows
    int[] exampleSet;
    // use all rows as examples at first
    exampleSet = new int[table.getNumRows()];
    for (int i = 0; i < table.getNumRows(); i++) {
      exampleSet[i] = i;

      // use all columns as attributes at first
    }
    int[] atts = new int[inputs.length];
    for (int i = 0; i < inputs.length; i++) {
      atts[i] = inputs[i];

    }
    DecisionTreeNode rootNode = buildTree(exampleSet, atts);
    pushOutput(rootNode, 0);
  }

  /**
   Build a decision tree.
   let examples(v) be those examples with A = v.
   if examples(v) is empty, make the new branch a leaf labelled with
   the most common value among examples.
   else let the new branch be the tree created by
   buildTree(examples(v), target, attributes-{A})
   @param examples the indices of the rows to use
   @param attributes the indices of the columns to use
   @return a tree
   */
  protected DecisionTreeNode buildTree(int[] examples, int[] attributes) throws
      MinimumRecordsPerLeafException {

    //debug("BuildTree with "+examples.length+" examples and "+attributes.length+" attributes.");

    if (this.isAborting()) {
      return null;
    }

    if ( (((double)examples.length)/(double)numExamples) < getMinimumRatioPerLeaf()) {
      throw new MinimumRecordsPerLeafException();
    }

    DecisionTreeNode root = null;
    String s;

    // if all examples have the same output value, give the root this
    // label-- this node is a leaf.
    boolean allSame = true;
    int counter = 0;
    s = table.getString(examples[counter], outputs[0]);
    counter++;
    while (allSame && counter < examples.length) {
      String t = table.getString(examples[counter], outputs[0]);
      if (!t.equals(s)) {
        allSame = false;
      }
      counter++;
    }
    // create the leaf
    if (allSame) {
      if (debug) {
        System.out.println("***The values were all the same: " + s);
      }
      root = new CategoricalDecisionTreeNode(s);
      return root;
    }

    // if attributes is empty, label the root according to the most common
    // value this will result in some incorrect classifications...
    // this node is a leaf.
    if (attributes.length == 0) {
      String mostCommon = mostCommonOutputValue(table, outputs[0], examples);
      // make a leaf
      if (debug) {
        System.out.println(
            "***Attributes empty.  Creating new Leaf with most common output value: " +
            mostCommon);
      }
      root = new CategoricalDecisionTreeNode(mostCommon);
      return root;
    }

    // otherwise build the subtree rooted at this node

    // calculate the information gain for each attribute
    // select the attribute, A, with the lowest average entropy, make
    // this be the one tested at the root
    ColSplit best = getHighestGainAttribute(table, attributes, outputs[0],
                                            examples);

    // if there was a column
    if (best != null) {
      int col = best.col;

      // categorical data
      if (!table.isColumnScalar(col)) {
        // for each possible value v of this attribute in the set
        // of examples add a new branch below the root,
        // corresponding to A = v
        try {
          String[] branchVals = uniqueValues(table, col, examples);
          root = new CategoricalDecisionTreeNode(table.getColumnLabel(col));
          for (int i = 0; i < branchVals.length; i++) {
            int[] branchExam = narrowCategoricalExamples(col,
                branchVals[i], examples);
            int[] branchAttr = narrowAttributes(col, attributes);
            //if (branchExam.length >= getMinimumRecordsPerLeaf() &&
            if ( (((double)branchExam.length)/(double)numExamples) > getMinimumRatioPerLeaf() &&
                branchAttr.length != 0) {
              root.addBranch(branchVals[i], buildTree(branchExam,
                  branchAttr));
            }

            // if examples(v) is empty, make the new branch a leaf
            // labelled with the most common value among examples
            else {
              String val = mostCommonOutputValue(table, outputs[0], examples);
              DecisionTreeNode nde = new CategoricalDecisionTreeNode(val);
              root.addBranch(val, nde);
            }
          }
        }
        catch (MinimumRecordsPerLeafException e) {
//          e.printStackTrace();
          String val = mostCommonOutputValue(table, outputs[0], examples);
          DecisionTreeNode nde = new CategoricalDecisionTreeNode(val);
          root.addBranch(val, nde);
        }
        catch (Exception e) {
//          e.printStackTrace();
          String val = mostCommonOutputValue(table, outputs[0], examples);
          DecisionTreeNode nde = new CategoricalDecisionTreeNode(val);
          root.addBranch(val, nde);

        }
      }

      // else if numeric find the binary split point and create two branches
      else {
        try {
          DecisionTreeNode left;
          DecisionTreeNode right;
          root = new NumericDecisionTreeNode(table.getColumnLabel(col));

          // create the less than branch
          int[] branchExam = narrowNumericExamples(col,
              best.splitValue, examples, false);
          //if(branchExam.length >= minimumRecordsPerLeaf) {
          left = buildTree(branchExam, attributes);
          //}

          // else if examples(v) is empty, make the new branch a leaf
          // labelled with the most common value among examples
          /*else {
           if(debug)
            System.out.println("Making a new Left Branch for a numeric with the most common output.");
           String val = mostCommonOutputValue(table, outputs[0], examples);
           left = new CategoricalDecisionTreeNode(val);
               }*/

          // create the greater than branch
          branchExam = narrowNumericExamples(col, best.splitValue,
                                             examples, true);
          //if(branchExam.length >= minimumRecordsPerLeaf) {
          right = buildTree(branchExam, attributes);
          //}

          // else if examples(v) is empty, make the new branch a leaf
          // labelled with the most common value among examples
          /*else {
           if(debug)
            System.out.println("Making a new Right branch for a numeric with the most common output.");
           String val = mostCommonOutputValue(table, outputs[0], examples);
           right = new CategoricalDecisionTreeNode(val);
               }*/

          // add the branches to the root
          StringBuffer lesser = new StringBuffer(table.getColumnLabel(col));
          lesser.append(LESS_THAN);
          lesser.append(nf.format(best.splitValue));

          StringBuffer greater = new StringBuffer(table.getColumnLabel(col));
          greater.append(GREATER_THAN_EQUAL_TO);
          greater.append(nf.format(best.splitValue));
          root.addBranches(best.splitValue, lesser.toString(), left,
                           greater.toString(), right);
        }
        catch (MinimumRecordsPerLeafException e) {
          String val = mostCommonOutputValue(table, outputs[0], examples);
          return new CategoricalDecisionTreeNode(val);
        }
      }
    }

    // otherwise we could not find a suitable column.  create
    // a new node with the most common output
    else {
      String val = mostCommonOutputValue(table, outputs[0], examples);
      root = new CategoricalDecisionTreeNode(val);
      if (debug) {
        System.out.println("creating new CategoricalDTN: " + val);
      }
    }

    return root;
  }

  private class MinimumRecordsPerLeafException
      extends Exception {}

  /**
   Find the most common output value from a list of examples.
   @param examples the list of examples
   @return the most common output value from the examples
   */
  private static String mostCommonOutputValue(Table t, int outCol,
                                              int[] examples) {
    HashMap map = new HashMap();
    int[] tallies = new int[0];
    for (int i = 0; i < examples.length; i++) {
      String s = t.getString(examples[i], outCol);
      if (map.containsKey(s)) {
        Integer loc = (Integer) map.get(s);
        tallies[loc.intValue()]++;
      }
      else {
        map.put(s, new Integer(map.size()));
        tallies = expandArray(tallies);
        tallies[tallies.length - 1] = 1;
      }
    }

    int highestTal = 0;
    String mostCommon = null;

    Iterator i = map.keySet().iterator();
    while (i.hasNext()) {
      String s = (String) i.next();
      Integer loc = (Integer) map.get(s);
      if (tallies[loc.intValue()] > highestTal) {
        highestTal = tallies[loc.intValue()];
        mostCommon = s;
      }
    }
    return mostCommon;
  }

  /**
   Create a subset of the examples.  Only those examples where the value
   is equal to val will be in the subset.
   @param col the column to test
   @param the value to test
   @param exam the list of examples to narrow
   @return a subset of the original list of examples
   */
  private int[] narrowCategoricalExamples(int col, String val, int[] exam) {
    int numNewExamples = 0;
    boolean[] map = new boolean[exam.length];

    for (int i = 0; i < exam.length; i++) {
      String s = table.getString(exam[i], col);
      if (s.equals(val)) {
        numNewExamples++;
        map[i] = true;
      }
      else {
        map[i] = false;
      }
    }
    int[] examples = new int[numNewExamples];
    int curIdx = 0;
    for (int i = 0; i < exam.length; i++) {
      if (map[i]) {
        examples[curIdx] = exam[i];
        curIdx++;
      }
    }
    return examples;
  }

  /**
   Create a subset of the examples.  If greaterThan is true, only those
   rows where the value is greater than than the splitValue will be in
   the subset.  Otherwise only the rows where the value is less than the
   splitValue will be in the subset.
   @param col the column to test
   @param splitValue the value to test
   @param exam the list of examples to narrow
   @param greaterThan true if values greater than the split value should
    be in the new list of examples, false if values less than the split
    value should be in the list of examples
   @return a subset of the original list of examples
   */
  private int[] narrowNumericExamples(int col, double splitValue, int[] exam,
                                      boolean greaterThan) {

    int numNewExamples = 0;
    boolean[] map = new boolean[exam.length];

    for (int i = 0; i < exam.length; i++) {
      double d = table.getDouble(exam[i], col);
      if (greaterThan) {
        if (d >= splitValue) {
          numNewExamples++;
          map[i] = true;
        }
        else {
          map[i] = false;
        }
      }
      else {
        if (d < splitValue) {
          numNewExamples++;
          map[i] = true;
        }
        else {
          map[i] = false;
        }
      }
    }

    int[] examples = new int[numNewExamples];
    int curIdx = 0;
    for (int i = 0; i < exam.length; i++) {
      if (map[i]) {
        examples[curIdx] = exam[i];
        curIdx++;
      }
    }

    return examples;
  }

  /**
   Remove the specified column from list of attributes.
   @param col the column to remove
   @param attr the list of attributes
   @return a subset of the original list of attributes
   */
  private int[] narrowAttributes(int col, int[] attr) {
    int[] retVal = new int[attr.length - 1];
    int curIdx = 0;
    for (int i = 0; i < attr.length; i++) {
      if (attr[i] != col) {
        retVal[curIdx] = attr[i];
        curIdx++;
      }
    }
    return retVal;
  }
}


/**
 * basic 4 qa comments.
 *
 * 02-02-04: vered
 * changed the name of this module to differ from C45TreeBuilder's.
 * changed name and description of second input to be more specific.
 * as this input is not just any parameter point, but one that is used to
 * control the min leaf ratio.
 */
=======
/* This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>. */

package org.opentripplanner.graph_builder.impl.osm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opentripplanner.common.StreetUtils;
import org.opentripplanner.common.TurnRestriction;
import org.opentripplanner.common.TurnRestrictionType;
import org.opentripplanner.common.geometry.DistanceLibrary;
import org.opentripplanner.common.geometry.GeometryUtils;
import org.opentripplanner.common.geometry.PackedCoordinateSequence;
import org.opentripplanner.common.model.P2;
import org.opentripplanner.graph_builder.impl.osm.OSMPlainStreetEdgeFactory;
import org.opentripplanner.graph_builder.impl.extra_elevation_data.ElevationPoint;
import org.opentripplanner.graph_builder.impl.extra_elevation_data.ExtraElevationData;
import org.opentripplanner.graph_builder.services.GraphBuilder;
import org.opentripplanner.graph_builder.services.osm.CustomNamer;
import org.opentripplanner.openstreetmap.model.OSMLevel;
import org.opentripplanner.openstreetmap.model.OSMLevel.Source;
import org.opentripplanner.openstreetmap.model.OSMNode;
import org.opentripplanner.openstreetmap.model.OSMRelation;
import org.opentripplanner.openstreetmap.model.OSMRelationMember;
import org.opentripplanner.openstreetmap.model.OSMTag;
import org.opentripplanner.openstreetmap.model.OSMWay;
import org.opentripplanner.openstreetmap.model.OSMWithTags;
import org.opentripplanner.openstreetmap.services.OpenStreetMapContentHandler;
import org.opentripplanner.openstreetmap.services.OpenStreetMapProvider;
import org.opentripplanner.routing.algorithm.GenericDijkstra;
import org.opentripplanner.routing.algorithm.strategies.SkipEdgeStrategy;
import org.opentripplanner.routing.core.GraphBuilderAnnotation;
import org.opentripplanner.routing.core.GraphBuilderAnnotation.Variety;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.edgetype.EdgeWithElevation;
import org.opentripplanner.routing.edgetype.ElevatorAlightEdge;
import org.opentripplanner.routing.edgetype.ElevatorBoardEdge;
import org.opentripplanner.routing.edgetype.ElevatorHopEdge;
import org.opentripplanner.routing.edgetype.FreeEdge;
import org.opentripplanner.routing.edgetype.PlainStreetEdge;
import org.opentripplanner.routing.edgetype.RentABikeOffEdge;
import org.opentripplanner.routing.edgetype.RentABikeOnEdge;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.edgetype.StreetTraversalPermission;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.patch.Alert;
import org.opentripplanner.routing.patch.TranslatedString;
import org.opentripplanner.routing.spt.GraphPath;
import org.opentripplanner.routing.spt.ShortestPathTree;
import org.opentripplanner.routing.util.ElevationUtils;
import org.opentripplanner.routing.vertextype.BikeRentalStationVertex;
import org.opentripplanner.routing.vertextype.ElevatorOffboardVertex;
import org.opentripplanner.routing.vertextype.ElevatorOnboardVertex;
import org.opentripplanner.routing.vertextype.IntersectionVertex;
import org.opentripplanner.util.MapUtils;
import org.opentripplanner.visibility.Environment;
import org.opentripplanner.visibility.Point;
import org.opentripplanner.visibility.Polygon;
import org.opentripplanner.visibility.VisibilityGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;

/**
 * Builds a street graph from OpenStreetMap data.
 * 
 */
public class OpenStreetMapGraphBuilderImpl implements GraphBuilder {

    private static Logger _log = LoggerFactory.getLogger(OpenStreetMapGraphBuilderImpl.class);

    private List<OpenStreetMapProvider> _providers = new ArrayList<OpenStreetMapProvider>();

    private Map<Object, Object> _uniques = new HashMap<Object, Object>();

    private WayPropertySet wayPropertySet = new WayPropertySet();

    private CustomNamer customNamer;
    
    private ExtraElevationData extraElevationData = new ExtraElevationData();

    private boolean noZeroLevels = true;

    private boolean staticBikeRental;

    private OSMPlainStreetEdgeFactory edgeFactory = new DefaultOSMPlainStreetEdgeFactory();

    public List<String> provides() {
        return Arrays.asList("streets", "turns");
    }

    public List<String> getPrerequisites() {
        return Collections.emptyList();
    }
    
    /**
     * The source for OSM map data
     */
    public void setProvider(OpenStreetMapProvider provider) {
        _providers.add(provider);
    }

    /**
     * Multiple sources for OSM map data
     */
    public void setProviders(List<OpenStreetMapProvider> providers) {
        _providers.addAll(providers);
    }

    /**
     * Allows for alternate PlainStreetEdge implementations; this is intended
     * for users who want to provide more info in PSE than OTP normally keeps
     * around.
     */
    public void setEdgeFactory(OSMPlainStreetEdgeFactory edgeFactory) {
        this.edgeFactory = edgeFactory;
    }

    /**
     * Set the way properties from a {@link WayPropertySetSource} source.
     * 
     * @param source the way properties source
     */
    public void setDefaultWayPropertySetSource(WayPropertySetSource source) {
        wayPropertySet = source.getWayPropertySet();
    }

    /**
     * If true, disallow zero floors and add 1 to non-negative numeric floors, as is generally done
     * in the United States. This does not affect floor names from level maps. Default: true.
     */
    public void setNoZeroLevels(boolean nz) {
        noZeroLevels = nz;
    }

    @Override
    public void buildGraph(Graph graph, HashMap<Class<?>, Object> extra) {
        Handler handler = new Handler();
        for (OpenStreetMapProvider provider : _providers) {
            _log.debug("gathering osm from provider: " + provider);
            provider.readOSM(handler);
        }
        _log.debug("building osm street graph");
        handler.buildGraph(graph, extra);
    }

    @SuppressWarnings("unchecked")
    private <T> T unique(T value) {
        Object v = _uniques.get(value);
        if (v == null) {
            _uniques.put(value, value);
            v = value;
        }
        return (T) v;
    }

    public void setWayPropertySet(WayPropertySet wayDataSet) {
        this.wayPropertySet = wayDataSet;
    }

    public WayPropertySet getWayPropertySet() {
        return wayPropertySet;
    }

    /**
     * Whether bike rental stations should be loaded from OSM, rather
     * than periodically dynamically pulled from APIs.
     */
    public void setStaticBikeRental(boolean b) {
        this.staticBikeRental = b;
    }
    
    public boolean getStaticBikeRental() {
        return staticBikeRental;
    }

    private class Handler implements OpenStreetMapContentHandler {

        private static final double VISIBILITY_EPSILON = 0.000000001;
        private Map<Long, OSMNode> _nodes = new HashMap<Long, OSMNode>();
        private Map<Long, OSMWay> _ways = new HashMap<Long, OSMWay>();
        private List<Area> _areas = new ArrayList<Area>();
        private Set<Long> _areaWayIds = new HashSet<Long>();
        private Map<Long, OSMWay> _areaWaysById = new HashMap<Long, OSMWay>();
        private Map<Long, Set<OSMWay>> _areasForNode = new HashMap<Long, Set<OSMWay>>();
        private List<OSMWay> _singleWayAreas = new ArrayList<OSMWay>();

        private Map<Long, OSMRelation> _relations = new HashMap<Long, OSMRelation>();
        private Set<OSMWithTags> _processedAreas = new HashSet<OSMWithTags>();
        private Set<Long> _nodesWithNeighbors = new HashSet<Long>();
        private Set<Long> _areaNodes = new HashSet<Long>();

        private Map<Long, List<TurnRestrictionTag>> turnRestrictionsByFromWay =
                new HashMap<Long, List<TurnRestrictionTag>>();

        private Map<Long, List<TurnRestrictionTag>> turnRestrictionsByToWay =
                new HashMap<Long, List<TurnRestrictionTag>>();

        private Map<TurnRestrictionTag, TurnRestriction> turnRestrictionsByTag =
                new HashMap<TurnRestrictionTag, TurnRestriction>();

        class Ring {
            public List<OSMNode> nodes;

            public Polygon geometry;

            public List<Ring> holes = new ArrayList<Ring>();

            public Ring(List<Long> osmNodes) {
                ArrayList<Point> vertices = new ArrayList<Point>();
                nodes = new ArrayList<OSMNode>(osmNodes.size());
                for (long nodeId : osmNodes) {
                    OSMNode node = _nodes.get(nodeId);
                    if (nodes.contains(node)) {
                        // hopefully, this only happens in order to
                        // close polygons
                        continue;
                    }
                    Point point = new Point(node.getLon(), node.getLat());
                    nodes.add(node);
                    vertices.add(point);
                }
                geometry = new Polygon(vertices);
            }
        }

        /**
         * Stores information about an OSM area needed for visibility graph construction. Algorithm
         * based on http://wiki.openstreetmap.org/wiki/Relation:multipolygon/Algorithm but generally
         * done in a quick/dirty way.
         */
        class Area {

            public class AreaConstructionException extends RuntimeException {
                private static final long serialVersionUID = 1L;
            }
            OSMWithTags parent; // this is the way or relation that has the relevant tags for the
                                // area

            List<Ring> outermostRings = new ArrayList<Ring>();

            Area(OSMWithTags parent, List<OSMWay> outerRingWays, List<OSMWay> innerRingWays) {
                this.parent = parent;
                // ring assignment
                List<List<Long>> innerRingNodes = constructRings(innerRingWays);
                List<List<Long>> outerRingNodes = constructRings(outerRingWays);
                if (innerRingNodes == null || outerRingNodes == null) {
                    throw new AreaConstructionException();
                }
                ArrayList<List<Long>> allRings = new ArrayList<List<Long>>(innerRingNodes);
                allRings.addAll(outerRingNodes);

                List<Ring> innerRings = new ArrayList<Ring>();
                List<Ring> outerRings = new ArrayList<Ring>();
                for (List<Long> ring : innerRingNodes) {
                    innerRings.add(new Ring(ring));
                }
                for (List<Long> ring : outerRingNodes) {
                    outerRings.add(new Ring(ring));
                }

                // now, ring grouping
                // first, find outermost rings
                OUTER: for (Ring outer : outerRings) {
                    for (Ring possibleContainer : outerRings) {
                        if (outer != possibleContainer && 
                                outer.geometry.hasPointInside(possibleContainer.geometry)) {
                            continue OUTER;
                        }
                    }
                    outermostRings.add(outer);

                    // find holes in this ring
                    for (Ring possibleHole : innerRings) {
                        if (possibleHole.geometry.hasPointInside(outer.geometry)) {
                            outer.holes.add(possibleHole);
                        }
                    }
                }
            }

            public List<List<Long>> constructRings(List<OSMWay> ways) {
                if (ways.size() == 0) {
                    // no rings is no rings
                    return Collections.emptyList();
                }

                HashMap<Long, List<OSMWay>> waysByEndpoint = new HashMap<Long, List<OSMWay>>();
                for (OSMWay way : ways) {
                    List<Long> refs = way.getNodeRefs();
                    MapUtils.addToMapList(waysByEndpoint, refs.get(0), way);
                    MapUtils.addToMapList(waysByEndpoint, refs.get(refs.size() - 1), way);
                }

                List<List<Long>> closedRings = new ArrayList<List<Long>>();
                // precheck for impossible situations and precompute one-way rings

                List<Long> toRemove = new ArrayList<Long>();
                for (Map.Entry<Long, List<OSMWay>> entry : waysByEndpoint.entrySet()) {
                    Long key = entry.getKey();
                    List<OSMWay> list = entry.getValue();
                    if (list.size() % 2 == 1) {
                        return null;
                    }
                    OSMWay way1 = list.get(0);
                    OSMWay way2 = list.get(1);
                    if (list.size() == 2 && way1 == way2) {
                        ArrayList<Long> ring = new ArrayList<Long>(way1.getNodeRefs());
                        closedRings.add(ring);
                        toRemove.add(key);
                    }
                }
                for (Long key : toRemove) {
                    waysByEndpoint.remove(key);
                }

                List<Long> partialRing = new ArrayList<Long>();
                if (waysByEndpoint.size() == 0) {
                    return closedRings;
                }

                long firstEndpoint = 0, otherEndpoint = 0;
                OSMWay firstWay = null;
                for (Map.Entry<Long, List<OSMWay>> entry : waysByEndpoint.entrySet()) {
                    firstEndpoint = entry.getKey();
                    List<OSMWay> list = entry.getValue();
                    firstWay = list.get(0);
                    List<Long> nodeRefs = firstWay.getNodeRefs();
                    partialRing.addAll(nodeRefs);
                    firstEndpoint = nodeRefs.get(0);
                    otherEndpoint = nodeRefs.get(nodeRefs.size() - 1);
                    break;
                }
                waysByEndpoint.get(firstEndpoint).remove(firstWay);
                waysByEndpoint.get(otherEndpoint).remove(firstWay);
                if (constructRingsRecursive(waysByEndpoint, partialRing, closedRings, firstEndpoint)) {
                    return closedRings;
                } else {
                    return null;
                }
            }

            private boolean constructRingsRecursive(HashMap<Long, List<OSMWay>> waysByEndpoint,
                    List<Long> ring, List<List<Long>> closedRings, long endpoint) {

                List<OSMWay> ways = new ArrayList<OSMWay>(waysByEndpoint.get(endpoint));

                for (OSMWay way : ways) {
                    // remove this way from the map
                    List<Long> nodeRefs = way.getNodeRefs();
                    long firstEndpoint = nodeRefs.get(0);
                    long otherEndpoint = nodeRefs.get(nodeRefs.size() - 1);
                    MapUtils.removeFromMapList(waysByEndpoint, firstEndpoint, way);
                    MapUtils.removeFromMapList(waysByEndpoint, otherEndpoint, way);

                    ArrayList<Long> newRing = new ArrayList<Long>(ring.size() + nodeRefs.size());
                    long newFirstEndpoint;
                    if (firstEndpoint == endpoint) {
                        for (int j = nodeRefs.size() - 1; j >= 1; --j) {
                            newRing.add(nodeRefs.get(j));
                        }
                        newRing.addAll(ring);
                        newFirstEndpoint = otherEndpoint;
                    } else {
                        newRing.addAll(nodeRefs.subList(0, nodeRefs.size() - 1));
                        newRing.addAll(ring);
                        newFirstEndpoint = firstEndpoint;
                    }
                    if (newRing.get(newRing.size() - 1).equals(newRing.get(0))) {
                        // ring closure
                        closedRings.add(newRing);
                        // if we're out of endpoints, then we have succeeded
                        if (waysByEndpoint.size() == 0) {
                            return true; // success
                        }

                        // otherwise, we need to start a new partial ring
                        newRing = new ArrayList<Long>();
                        OSMWay firstWay = null;
                        for (Map.Entry<Long, List<OSMWay>> entry : waysByEndpoint.entrySet()) {
                            firstEndpoint = entry.getKey();
                            List<OSMWay> list = entry.getValue();
                            firstWay = list.get(0);
                            nodeRefs = firstWay.getNodeRefs();
                            newRing.addAll(nodeRefs);
                            firstEndpoint = nodeRefs.get(0);
                            otherEndpoint = nodeRefs.get(nodeRefs.size() - 1);
                            break;
                        }
                        MapUtils.removeFromMapList(waysByEndpoint, firstEndpoint, firstWay);
                        MapUtils.removeFromMapList(waysByEndpoint, otherEndpoint, firstWay);
                        if (constructRingsRecursive(waysByEndpoint, newRing, closedRings,
                                firstEndpoint)) {
                            return true;
                        }
                        MapUtils.addToMapList(waysByEndpoint, firstEndpoint, firstWay);
                        MapUtils.addToMapList(waysByEndpoint, otherEndpoint, firstWay);
                    } else {
                        // continue with this ring
                        if (waysByEndpoint.get(newFirstEndpoint) != null) {
                            if (constructRingsRecursive(waysByEndpoint, newRing, closedRings,
                                    newFirstEndpoint)) {
                                return true;
                            }
                        }
                    }
                    if (firstEndpoint == endpoint) {
                        MapUtils.addToMapList(waysByEndpoint, otherEndpoint, way);
                    } else {
                        MapUtils.addToMapList(waysByEndpoint, firstEndpoint, way);
                    }
                }
                return false;
            }
        }

        private Graph graph;

        /** The bike safety factor of the safest street */
        private double bestBikeSafety = 1;

        // track OSM nodes which are decomposed into multiple graph vertices because they are
        // elevators. later they will be iterated over to build ElevatorEdges between them.
        private HashMap<Long, HashMap<OSMLevel, IntersectionVertex>> multiLevelNodes =
                new HashMap<Long, HashMap<OSMLevel, IntersectionVertex>>();

        // track OSM nodes that will become graph vertices because they appear in multiple OSM ways
        private Map<Long, IntersectionVertex> intersectionNodes =
                new HashMap<Long, IntersectionVertex>();

        // track vertices to be removed in the turn-graph conversion.
        // this is a superset of intersectionNodes.values, which contains
        // a null vertex reference for multilevel nodes. the individual vertices
        // for each level of a multilevel node are includeed in endpoints.
        private ArrayList<IntersectionVertex> endpoints = new ArrayList<IntersectionVertex>();

        // track which vertical level each OSM way belongs to, for building elevators etc.
        private Map<OSMWay, OSMLevel> wayLevels = new HashMap<OSMWay, OSMLevel>();
        private HashSet<OSMNode> _bikeRentalNodes = new HashSet<OSMNode>();

        public void buildGraph(Graph graph, HashMap<Class<?>, Object> extra) {
            this.graph = graph;

            // handle turn restrictions, road names, and level maps in relations
            processRelations();

            if (staticBikeRental) {
                processBikeRentalNodes();
            }

            // Remove all simple islands
            HashSet<Long> _keep = new HashSet<Long>(_nodesWithNeighbors);
            _keep.addAll(_areaNodes);
            _nodes.keySet().retainAll(_keep);

            // figure out which nodes that are actually intersections
            initIntersectionNodes();

            buildBasicGraph();
            buildAreas();

            buildElevatorEdges(graph);

            /* unify turn restrictions */
            Map<Edge, TurnRestriction> turnRestrictions = new HashMap<Edge, TurnRestriction>();
            for (TurnRestriction restriction : turnRestrictionsByTag.values()) {
                turnRestrictions.put(restriction.from, restriction);
            }
            if (customNamer != null) {
                customNamer.postprocess(graph);
            }
            
            //generate elevation profiles
            generateElevationProfiles(graph);

            applyBikeSafetyFactor(graph);
            StreetUtils.makeEdgeBased(graph, endpoints, turnRestrictions);

        } // END buildGraph()

        private void processBikeRentalNodes() {
            _log.debug("Processing bike rental nodes...");
            int n = 0;
            for (OSMNode node : _bikeRentalNodes) {
                n++;
                int capacity = Integer.MAX_VALUE;
                if (node.hasTag("capacity")) {
                    try {
                        capacity = Integer.parseInt(node.getTag("capacity"));
                    } catch (NumberFormatException e) {
                        _log.warn("Capacity is not a number: " + node.getTag("capacity"));
                    }
                }
                String creativeName = wayPropertySet.getCreativeNameForWay(node);
                BikeRentalStationVertex station = new BikeRentalStationVertex(graph, "" + node.getId(), "bike rental "
                        + node.getId(), node.getLon(), node.getLat(),
                        creativeName, capacity);
                new RentABikeOnEdge(station, station);
                new RentABikeOffEdge(station, station);
            }
            _log.debug("Created " + n + " bike rental stations.");
        }

        private void generateElevationProfiles(Graph graph) {
            Map<EdgeWithElevation, List<ElevationPoint>> data = extraElevationData.data;
            for (Map.Entry<EdgeWithElevation, List<ElevationPoint>> entry : data.entrySet()) {
                EdgeWithElevation edge = entry.getKey();
                List<ElevationPoint> points = entry.getValue();
                Collections.sort(points);

                if (points.size() == 1) {
                    ElevationPoint firstPoint = points.get(0);
                    ElevationPoint endPoint = new ElevationPoint(edge.getDistance(), firstPoint.ele);
                    points.add(endPoint);
                }
                Coordinate[] coords = new Coordinate[points.size()];
                int i = 0;
                for (ElevationPoint p : points) {
                    double d = p.distanceAlongShape;
                    if (i == 0) {
                        d = 0;
                    } else if (i == points.size() - 1) {
                        d = edge.getDistance();
                    }
                    coords[i++] = new Coordinate(d, p.ele);
                }
                // set elevation profile and warn if profile was flattened because it was too steep
                if(edge.setElevationProfile(new PackedCoordinateSequence.Double(coords), true)) {
                    _log.warn(GraphBuilderAnnotation.register(graph, Variety.ELEVATION_FLATTENED, edge));
                }
            }
        }

        private void buildAreas() {
            final int MAX_AREA_NODES = 500;
            _log.debug("building visibility graphs for areas");
            for (Area area : _areas) {
                Set<OSMNode> startingNodes = new HashSet<OSMNode>();
                List<Vertex> startingVertices = new ArrayList<Vertex>();
                Set<Edge> edges = new HashSet<Edge>();

                OSMWithTags areaEntity = area.parent;

                StreetTraversalPermission areaPermissions = getPermissionsForEntity(areaEntity,
                        StreetTraversalPermission.PEDESTRIAN_AND_BICYCLE);
                if (areaPermissions == StreetTraversalPermission.NONE) 
                        continue;
                setWayName(areaEntity);

                List<Point> vertices = new ArrayList<Point>();

                // the points corresponding to concave or hole vertices
                // or those linked to ways
                Set<Point> visibilityPoints = new HashSet<Point>();

                // create polygon and accumulate nodes for area

                for (Ring ring : area.outermostRings) {
                    List<OSMNode> nodes = new ArrayList<OSMNode>();
                    vertices.clear();
                    for (OSMNode node : ring.nodes) {
                        if (nodes.contains(node)) {
                            // hopefully, this only happens in order to
                            // close polygons
                            continue;
                        }
                        if (node == null) {
                            throw new RuntimeException("node for area "
                                    + areaEntity.getId() + " does not exist");
                        }
                        Point point = new Point(node.getLon(), node.getLat());
                        nodes.add(node);
                        vertices.add(point);
                    }
                    Polygon polygon = new Polygon(vertices);

                    if (polygon.area() < 0) {
                        polygon.reverse();
                        // need to reverse nodes as well
                        reversePolygonOfOSMNodes(nodes);
                    }

                    if (!polygon.is_in_standard_form()) {
                        standardize(polygon.vertices, nodes);
                    }

                    int n = polygon.vertices.size();
                    for (int i = 0; i < n; ++i) {
                        Point cur = polygon.vertices.get(i);
                        Point prev = polygon.vertices.get((i + n - 1) % n);
                        Point next = polygon.vertices.get((i + 1) % n);
                        OSMNode curNode = nodes.get(i);
                        if (_nodesWithNeighbors.contains(curNode.getId()) || multipleAreasContain(curNode.getId())) {
                            visibilityPoints.add(cur);
                            startingNodes.add(curNode);
                        } else if ((cur.x - prev.x) * (next.y - cur.y) - (cur.y - prev.y)
                                * (next.x - cur.x) < 0) {
                            //that math up there is a couple of cross products to check
                            //if the point is concave.
                            visibilityPoints.add(cur);
                        }

                    }

                    ArrayList<Polygon> polygons = new ArrayList<Polygon>();
                    polygons.add(polygon);
                    // holes
                    for (Ring innerRing : ring.holes) {
                        ArrayList<OSMNode> holeNodes = new ArrayList<OSMNode>();
                        vertices = new ArrayList<Point>();
                        for (OSMNode node : innerRing.nodes) {
                            if (holeNodes.contains(node)) {
                                // hopefully, this only happens in order to
                                // close polygons
                                continue;
                            }
                            if (node == null) {
                                throw new RuntimeException("node for area does not exist");
                            }
                            Point point = new Point(node.getLon(), node.getLat());
                            holeNodes.add(node);
                            vertices.add(point);
                            visibilityPoints.add(point);
                            if (_nodesWithNeighbors.contains(node.getId()) || multipleAreasContain(node.getId())) {                    
                                startingNodes.add(node);
                            }
                        }
                        Polygon hole = new Polygon(vertices);

                        if (hole.area() > 0) {
                            reversePolygonOfOSMNodes(holeNodes);
                            hole.reverse();
                        }
                        if (!hole.is_in_standard_form()) {
                            standardize(hole.vertices, holeNodes);
                        }
                        nodes.addAll(holeNodes);
                        polygons.add(hole);
                    }

                    Environment areaEnv = new Environment(polygons);

                    //FIXME: temporary hard limit on size of 
                    //areas to prevent way explosion
                    if (visibilityPoints.size() > MAX_AREA_NODES) {
                        _log.warn("Area " + area.parent + " is too complicated (" + visibilityPoints.size() + " > " + MAX_AREA_NODES);
                        continue;
                    }

                    if (!areaEnv.is_valid(VISIBILITY_EPSILON)) {
                        _log.warn("Area " + area.parent + " is not epsilon-valid (epsilon = " + VISIBILITY_EPSILON + ")");
                        continue;
                    }
                    VisibilityGraph vg = new VisibilityGraph(areaEnv, VISIBILITY_EPSILON, visibilityPoints);
                    for (int i = 0; i < nodes.size(); ++i) {
                        OSMNode nodeI = nodes.get(i);
                        for (int j = 0; j < nodes.size(); ++j) {
                            if (i == j)
                                continue;

                            if (vg.get(0, i, 0, j)) {
                                // vertex i is connected to vertex j
                                IntersectionVertex startEndpoint = getVertexForOsmNode(nodeI,
                                        areaEntity);
                                OSMNode nodeJ = nodes.get(j);
                                IntersectionVertex endEndpoint = getVertexForOsmNode(nodeJ, areaEntity);

                                Coordinate[] coordinates = new Coordinate[] {
                                        startEndpoint.getCoordinate(), endEndpoint.getCoordinate() };
                                LineString geometry = GeometryUtils.getGeometryFactory().createLineString(coordinates);

                                String id = "way (area) " + areaEntity.getId() + " from "
                                        + nodeI.getId() + " to " + nodeJ.getId();
                                id = unique(id);
                                String name = getNameForWay(areaEntity, id);

                                double length = DistanceLibrary.distance(
                                        startEndpoint.getCoordinate(), endEndpoint.getCoordinate());
                                PlainStreetEdge street = edgeFactory.createEdge(nodeI, nodeJ, areaEntity, startEndpoint,
                                        endEndpoint, geometry, name, length,
                                        areaPermissions,
                                        i > j);
                                street.setId(id);

                                edges.add(street);
                                if (startingNodes.contains(nodeI)) {
                                    startingVertices.add(startEndpoint);
                                }
                            }
                        }
                    }
                }
                pruneAreaEdges(startingVertices, edges);
            }
        }

        private void standardize(ArrayList<Point> vertices, List<OSMNode> nodes) {
            //based on code from VisiLibity
            int point_count = vertices.size();
            if (point_count > 1) { // if more than one point in the polygon.
                ArrayList<Point> vertices_temp = new ArrayList<Point>(point_count);
                ArrayList<OSMNode> nodes_temp = new ArrayList<OSMNode>(point_count);
                // Find index of lexicographically smallest point.
                int index_of_smallest = 0;
                for (int i = 1; i < point_count; i++)
                    if (vertices.get(i).compareTo(vertices.get(index_of_smallest)) < 0)
                        index_of_smallest = i;
                //minor optimization for already-standardized polygons
                if (index_of_smallest == 0) return;
                // Fill vertices_temp starting with lex. smallest.
                for (int i = index_of_smallest; i < point_count; i++) {
                    vertices_temp.add(vertices.get(i));
                    nodes_temp.add(nodes.get(i));
                }
                for (int i = 0; i < index_of_smallest; i++) {
                    vertices_temp.add(vertices.get(i));
                    nodes_temp.add(nodes.get(i));
                }
                for (int i = 0; i < point_count; ++i) {
                    vertices.set(i, vertices_temp.get(i));
                    nodes.set(i, nodes_temp.get(i));
                }
            }
        }

        private boolean multipleAreasContain(long id) {
            Set<OSMWay> areas = _areasForNode.get(id);
            if (areas == null) {
                return false;
            }
            return areas.size() > 1;
        }

        class ListedEdgesOnly implements SkipEdgeStrategy {
            private Set<Edge> edges;
            public ListedEdgesOnly(Set<Edge> edges) {
                this.edges = edges;
            }
            @Override
            public boolean shouldSkipEdge(Vertex origin, Vertex target, State current, Edge edge,
                    ShortestPathTree spt, RoutingRequest traverseOptions) {
                return !edges.contains(edge);
            }

        }
        /** 
         * Do an all-pairs shortest path search from a list of vertices over a specified
         * set of edges, and retain only those edges which are actually used in some
         * shortest path.
         * @param startingVertices
         * @param edges
         */
        private void pruneAreaEdges(List<Vertex> startingVertices, Set<Edge> edges) {
            if (edges.size() == 0)
                    return;
            TraverseMode mode;
            PlainStreetEdge firstEdge = (PlainStreetEdge) edges.iterator().next();

            if (firstEdge.getPermission().allows(StreetTraversalPermission.PEDESTRIAN)) {
                mode = TraverseMode.WALK;
            } else if (firstEdge.getPermission().allows(StreetTraversalPermission.BICYCLE)) {
                mode = TraverseMode.BICYCLE;
            } else {
                mode = TraverseMode.CAR;
            }
            RoutingRequest options = new RoutingRequest(mode);
            GenericDijkstra search = new GenericDijkstra(options);
            search.setSkipEdgeStrategy(new ListedEdgesOnly(edges));
            Set<Edge> usedEdges = new HashSet<Edge>();
            for (Vertex vertex : startingVertices) {
                State state = new State(vertex, options);
                ShortestPathTree spt = search.getShortestPathTree(state);
                for (Vertex endVertex : startingVertices) {
                    GraphPath path = spt.getPath(endVertex, false);
                    if (path != null){
                        for (Edge edge : path.edges) {
                            usedEdges.add(edge);
                        }
                    }
                }
            }
            for (Edge edge : edges) {
                if (!usedEdges.contains(edge)) {
                    edge.detach();
                }
            }
        }

        private void reversePolygonOfOSMNodes(List<OSMNode> nodes) {
            for (int i = 1; i < (nodes.size()+1) / 2; ++i) {
                OSMNode tmp = nodes.get(i);
                int opposite = nodes.size() - i;
                nodes.set(i, nodes.get(opposite));
                nodes.set(opposite, tmp);
            }
        }

        private void buildBasicGraph() {

            /* build an ordinary graph, which we will convert to an edge-based graph */
            long wayIndex = 0;

            for (OSMWay way : _ways.values()) {

                if (wayIndex % 10000 == 0)
                    _log.debug("ways=" + wayIndex + "/" + _ways.size());
                wayIndex++;

                WayProperties wayData = wayPropertySet.getDataForWay(way);

                setWayName(way);
                Set<Alert> note = wayPropertySet.getNoteForWay(way);
                Set<Alert> wheelchairNote = getWheelchairNotes(way);

                StreetTraversalPermission permissions = getPermissionsForEntity(way,
                        wayData.getPermission());
                if (permissions == StreetTraversalPermission.NONE)
                    continue;

                List<Long> nodes = way.getNodeRefs();

                IntersectionVertex startEndpoint = null, endEndpoint = null;

                ArrayList<Coordinate> segmentCoordinates = new ArrayList<Coordinate>();

                getLevelsForWay(way);

                /*
                 * Traverse through all the nodes of this edge. For nodes which are not shared with
                 * any other edge, do not create endpoints -- just accumulate them for geometry and
                 * ele tags. For nodes which are shared, create endpoints and StreetVertex
                 * instances.
                 */
                Long startNode = null;
                //where the current edge should start
                OSMNode osmStartNode = null;
                List<ElevationPoint> elevationPoints = new ArrayList<ElevationPoint>();
                double distance = 0;
                for (int i = 0; i < nodes.size() - 1; i++) {
                    OSMNode segmentStartOSMNode = _nodes.get(nodes.get(i));
                    if (segmentStartOSMNode == null) {
                        continue;
                    }
                    Long endNode = nodes.get(i + 1);
                    if (osmStartNode == null) {
                        startNode = nodes.get(i);
                        osmStartNode = segmentStartOSMNode;
                        elevationPoints.clear();
                    }
                    //where the current edge might end
                    OSMNode osmEndNode = _nodes.get(endNode);

                    if (osmStartNode == null || osmEndNode == null)
                        continue;

                    LineString geometry;

                    /*
                     * skip vertices that are not intersections, except that we use them for
                     * geometry
                     */
                    if (segmentCoordinates.size() == 0) {
                        segmentCoordinates.add(getCoordinate(osmStartNode));
                    }
                    String ele = segmentStartOSMNode.getTag("ele");
                    if (ele != null) {
                        Double elevation = ElevationUtils.parseEleTag(ele);
                        if (elevation != null) {
                            elevationPoints.add(
                                    new ElevationPoint(distance, elevation));
                        }
                    }

                    distance += DistanceLibrary.distance(
                            segmentStartOSMNode.getLat(), segmentStartOSMNode.getLon(),
                            osmEndNode.getLat(), osmEndNode.getLon());

                    if (intersectionNodes.containsKey(endNode) || i == nodes.size() - 2) {
                        segmentCoordinates.add(getCoordinate(osmEndNode));
                        ele = osmEndNode.getTag("ele");
                        if (ele != null) {
                            Double elevation = ElevationUtils.parseEleTag(ele);
                            if (elevation != null) {
                                elevationPoints.add(
                                        new ElevationPoint(distance, elevation));
                            }
                        }

                        geometry = GeometryUtils.getGeometryFactory().createLineString(segmentCoordinates
                                .toArray(new Coordinate[0]));
                        segmentCoordinates.clear();
                    } else {
                        segmentCoordinates.add(getCoordinate(osmEndNode));
                        continue;
                    }

                    /* generate endpoints */
                    if (startEndpoint == null) { // first iteration on this way
                        // make or get a shared vertex for flat intersections,
                        // one vertex per level for multilevel nodes like elevators
                        startEndpoint = getVertexForOsmNode(osmStartNode, way);
                    } else { // subsequent iterations
                        startEndpoint = endEndpoint;
                    }

                    endEndpoint = getVertexForOsmNode(osmEndNode, way);

                    P2<PlainStreetEdge> streets = getEdgesForStreet(startEndpoint, endEndpoint,
                            way, i, osmStartNode.getId(), osmEndNode.getId(), permissions, geometry);

                    PlainStreetEdge street = streets.getFirst();

                    if (street != null) {
                        double safety = wayData.getSafetyFeatures().getFirst();
                        street.setBicycleSafetyEffectiveLength(street.getLength() * safety);
                        if (safety < bestBikeSafety) {
                            bestBikeSafety = safety;
                        }
                        if (note != null) {
                            street.setNote(note);
                        }
                        if (wheelchairNote != null) {
                            street.setWheelchairNote(wheelchairNote);
                        }
                    }

                    PlainStreetEdge backStreet = streets.getSecond();
                    if (backStreet != null) {
                        double safety = wayData.getSafetyFeatures().getSecond();
                        if (safety < bestBikeSafety) {
                            bestBikeSafety = safety;
                        }
                        backStreet.setBicycleSafetyEffectiveLength(backStreet.getLength() * safety);
                        if (note != null) {
                            backStreet.setNote(note);
                        }
                        if (wheelchairNote != null) {
                            backStreet.setWheelchairNote(wheelchairNote);
                        }
                    }

                    storeExtraElevationData(elevationPoints, street, backStreet, distance);

                    applyEdgesToTurnRestrictions(way, startNode, endNode, street, backStreet);
                    startNode = endNode;
                    osmStartNode = _nodes.get(startNode);
                }
            } // END loop over OSM ways
        }

        private void setWayName(OSMWithTags way) {
            if (!way.hasTag("name")) {
                String creativeName = wayPropertySet.getCreativeNameForWay(way);
                if (creativeName != null) {
                    way.addTag("otp:gen_name", creativeName);
                }
            }
        }

        private void storeExtraElevationData(List<ElevationPoint> elevationPoints, PlainStreetEdge street, PlainStreetEdge backStreet, double length) {
            if (elevationPoints.isEmpty()) {
                return;
            }

            for (ElevationPoint p : elevationPoints) {
                if (street != null) {
                    MapUtils.addToMapList(extraElevationData.data, street, p);
                }
                if (backStreet != null) {
                    MapUtils.addToMapList(extraElevationData.data, backStreet, p.fromBack(length));
                }
            }
        }

        private void buildElevatorEdges(Graph graph) {
            /* build elevator edges */
            for (Long nodeId : multiLevelNodes.keySet()) {
                OSMNode node = _nodes.get(nodeId);
                // this allows skipping levels, e.g., an elevator that stops
                // at floor 0, 2, 3, and 5.
                // Converting to an Array allows us to
                // subscript it so we can loop over it in twos. Assumedly, it will stay
                // sorted when we convert it to an Array.
                // The objects are Integers, but toArray returns Object[]
                HashMap<OSMLevel, IntersectionVertex> vertices = multiLevelNodes.get(nodeId);

                /*
                 * first, build FreeEdges to disconnect from the graph, GenericVertices to serve as
                 * attachment points, and ElevatorBoard and ElevatorAlight edges to connect future
                 * ElevatorHop edges to. After this iteration, graph will look like (side view):
                 * +==+~~X
                 * 
                 * +==+~~X
                 * 
                 * +==+~~X
                 * 
                 * + GenericVertex, X EndpointVertex, ~~ FreeEdge, ==
                 * ElevatorBoardEdge/ElevatorAlightEdge Another loop will fill in the
                 * ElevatorHopEdges.
                 */

                OSMLevel[] levels = vertices.keySet().toArray(new OSMLevel[0]);
                Arrays.sort(levels);
                ArrayList<Vertex> onboardVertices = new ArrayList<Vertex>();
                for (OSMLevel level : levels) {
                    // get the node to build the elevator out from
                    IntersectionVertex sourceVertex = vertices.get(level);
                    String sourceVertexLabel = sourceVertex.getLabel();
                    String levelName = level.longName;

                    ElevatorOffboardVertex offboardVertex = new ElevatorOffboardVertex(graph,
                            sourceVertexLabel + "_offboard", sourceVertex.getX(),
                            sourceVertex.getY(), levelName);

                    new FreeEdge(sourceVertex, offboardVertex);
                    new FreeEdge(offboardVertex, sourceVertex);

                    ElevatorOnboardVertex onboardVertex = new ElevatorOnboardVertex(graph,
                            sourceVertexLabel + "_onboard", sourceVertex.getX(),
                            sourceVertex.getY(), levelName);

                    new ElevatorBoardEdge(offboardVertex, onboardVertex);
                    new ElevatorAlightEdge(onboardVertex, offboardVertex, level.longName);

                    // accumulate onboard vertices to so they can be connected by hop edges later
                    onboardVertices.add(onboardVertex);
                }

                // -1 because we loop over onboardVertices two at a time
                for (Integer i = 0, vSize = onboardVertices.size() - 1; i < vSize; i++) {
                    Vertex from = onboardVertices.get(i);
                    Vertex to = onboardVertices.get(i + 1);

                    // default permissions: pedestrian, wheelchair, and bicycle
                    boolean wheelchairAccessible = true;
                    StreetTraversalPermission permission = StreetTraversalPermission.PEDESTRIAN_AND_BICYCLE;
                    // check for bicycle=no, otherwise assume it's OK to take a bike
                    if (node.isTagFalse("bicycle")) {
                        permission = StreetTraversalPermission.PEDESTRIAN;
                    }
                    // check for wheelchair=no
                    if (node.isTagFalse("wheelchair")) {
                        wheelchairAccessible = false;
                    }

                    // The narrative won't be strictly correct, as it will show the elevator as part
                    // of the cycling leg, but I think most cyclists will figure out that they
                    // should really dismount.
                    ElevatorHopEdge foreEdge = new ElevatorHopEdge(from, to, permission);
                    ElevatorHopEdge backEdge = new ElevatorHopEdge(to, from, permission);
                    foreEdge.wheelchairAccessible = wheelchairAccessible;
                    backEdge.wheelchairAccessible = wheelchairAccessible;
                }
            } // END elevator edge loop
        }

        private void applyEdgesToTurnRestrictions(OSMWay way, long startNode, long endNode,
                PlainStreetEdge street, PlainStreetEdge backStreet) {
            /* Check if there are turn restrictions starting on this segment */
            List<TurnRestrictionTag> restrictionTags = turnRestrictionsByFromWay.get(way.getId());

            if (restrictionTags != null) {
                for (TurnRestrictionTag tag : restrictionTags) {
                    if (tag.via == startNode) {
                        TurnRestriction restriction = turnRestrictionsByTag.get(tag);
                        restriction.from = backStreet;
                    } else if (tag.via == endNode) {
                        TurnRestriction restriction = turnRestrictionsByTag.get(tag);
                        restriction.from = street;
                    }
                }
            }

            restrictionTags = turnRestrictionsByToWay.get(way.getId());
            if (restrictionTags != null) {
                for (TurnRestrictionTag tag : restrictionTags) {
                    if (tag.via == startNode) {
                        TurnRestriction restriction = turnRestrictionsByTag.get(tag);
                        restriction.to = street;
                    } else if (tag.via == endNode) {
                        TurnRestriction restriction = turnRestrictionsByTag.get(tag);
                        restriction.to = backStreet;
                    }
                }
            }
        }

        private void getLevelsForWay(OSMWay way) {
            /* Determine OSM level for each way, if it was not already set */
            if (!wayLevels.containsKey(way)) {
                // if this way is not a key in the wayLevels map, a level map was not
                // already applied in processRelations

                /* try to find a level name in tags */
                String levelName = null;
                OSMLevel level = OSMLevel.DEFAULT;
                if (way.hasTag("level")) { // TODO: floating-point levels &c.
                    levelName = way.getTag("level");
                    level = OSMLevel.fromString(levelName, OSMLevel.Source.LEVEL_TAG, noZeroLevels);
                } else if (way.hasTag("layer")) {
                    levelName = way.getTag("layer");
                    level = OSMLevel.fromString(levelName, OSMLevel.Source.LAYER_TAG, noZeroLevels);
                } 
                if (level == null || ( ! level.reliable)) {
                    _log.warn(GraphBuilderAnnotation.register(graph, Variety.LEVEL_AMBIGUOUS, 
                        levelName, "OSM way " + way.getId()));
                    level = OSMLevel.DEFAULT;
                }
                wayLevels.put(way, level);
            }
        }

        private void initIntersectionNodes() {
            Set<Long> possibleIntersectionNodes = new HashSet<Long>();
            for (OSMWay way : _ways.values()) {
                List<Long> nodes = way.getNodeRefs();
                for (long node : nodes) {
                    if (possibleIntersectionNodes.contains(node)) {
                        intersectionNodes.put(node, null);
                    } else {
                        possibleIntersectionNodes.add(node);
                    }
                }
            }
        }

        private Set<Alert> getWheelchairNotes(OSMWithTags way) {
            Map<String, String> tags = way.getTagsByPrefix("wheelchair:description");
            if (tags == null) {
                return null;
            }
            Set<Alert> alerts = new HashSet<Alert>();
            Alert alert = new Alert();
            alerts.add(alert);
            for (Map.Entry<String, String> entry : tags.entrySet()) {
                String k = entry.getKey();
                String v = entry.getValue();
                if (k.equals("wheelchair:description")) {
                    // no language, assume default from TranslatedString
                    alert.alertHeaderText = new TranslatedString(v);
                } else {
                    String lang = k.substring("wheelchair:description:".length());
                    alert.alertHeaderText = new TranslatedString(lang, v);
                }
            }
            return alerts;
        }

        /**
         * The safest bike lane should have a safety weight no lower than the time weight of a flat
         * street. This method divides the safety lengths by the length ratio of the safest street,
         * ensuring this property.
         * 
         * @param graph
         */
        private void applyBikeSafetyFactor(Graph graph) {
            _log.info(GraphBuilderAnnotation.register(graph, Variety.GRAPHWIDE,
                    "Multiplying all bike safety values by " + (1 / bestBikeSafety)));
            HashSet<Edge> seenEdges = new HashSet<Edge>();
            for (Vertex vertex : graph.getVertices()) {
                for (Edge e : vertex.getOutgoing()) {
                    if (!(e instanceof PlainStreetEdge)) {
                        continue;
                    }
                    PlainStreetEdge pse = (PlainStreetEdge) e;

                    if (!seenEdges.contains(e)) {
                        seenEdges.add(e);
                        pse.setBicycleSafetyEffectiveLength(pse.getBicycleSafetyEffectiveLength()
                                / bestBikeSafety);
                    }
                }
                for (Edge e : vertex.getIncoming()) {
                    if (!(e instanceof PlainStreetEdge)) {
                        continue;
                    }
                    PlainStreetEdge pse = (PlainStreetEdge) e;

                    if (!seenEdges.contains(e)) {
                        seenEdges.add(e);
                        pse.setBicycleSafetyEffectiveLength(pse.getBicycleSafetyEffectiveLength()
                                / bestBikeSafety);
                    }
                }
            }
        }

        private Coordinate getCoordinate(OSMNode osmNode) {
            return new Coordinate(osmNode.getLon(), osmNode.getLat());
        }

        public void addNode(OSMNode node) {
            if(node.isTag("amenity", "bicycle_rental")) {
                _bikeRentalNodes.add(node);
                return;
            }
            if (!(_nodesWithNeighbors.contains(node.getId()) || _areaNodes.contains(node.getId())))
                return;

            if (_nodes.containsKey(node.getId()))
                return;

            _nodes.put(node.getId(), node);

            if (_nodes.size() % 100000 == 0)
                _log.debug("nodes=" + _nodes.size());
        }

        public void addWay(OSMWay way) {
            /* only add ways once */
            long wayId = way.getId();
            if (_ways.containsKey(wayId) || _areaWaysById.containsKey(wayId))
                return;

            if (_areaWayIds.contains(wayId)) {
                _areaWaysById.put(wayId, way);
            }
            
            /* filter out ways that are not relevant for routing */
            if (!isWayRouteable(way)) {
                return;
            }
            if (way.isTag("area", "yes") && way.getNodeRefs().size() > 2) {
                //this is an area that's a simple polygon.  So we can just add it straight
                //to the areas, if it's not part of a relation.
                if (!_areaWayIds.contains(wayId)) {
                    _singleWayAreas.add(way);
                    _areaWaysById.put(wayId, way);
                    _areaWayIds.add(wayId);
                    for (Long node : way.getNodeRefs()) {
                        MapUtils.addToMapSet(_areasForNode, node, way);
                    }
                }
                return;
            }

            _ways.put(wayId, way);

            if (_ways.size() % 10000 == 0)
                _log.debug("ways=" + _ways.size());
        }

        private boolean isWayRouteable(OSMWithTags way) {
            if (!(way.hasTag("highway") || way.isTag("railway", "platform")))
                return false;
            String highway = way.getTag("highway");
            if (highway != null
                    && (highway.equals("conveyer") || highway.equals("proposed") || highway
                            .equals("raceway")))
                return false;

            String access = way.getTag("access");

            if (access != null) {
                if ("no".equals(access) || "license".equals(access)) {
                    if (way.doesTagAllowAccess("motorcar")) {
                        return true;
                    }
                    if (way.doesTagAllowAccess("bicycle")) {
                        return true;
                    }
                    if (way.doesTagAllowAccess("foot")) {
                        return true;
                    }
                    return false;
                }
            }
            return true;
        }

        public void addRelation(OSMRelation relation) {
            if (_relations.containsKey(relation.getId()))
                return;

            if (relation.isTag("type", "multipolygon") && relation.hasTag("highway")) {
                // OSM MultiPolygons are ferociously complicated, and in fact cannot be processed
                // without reference to the ways that compose them.  Accordingly, we will merely
                // mark the ways for preservation here, and deal with the details once we have
                // the ways loaded.
                if (!isWayRouteable(relation)) {
                    return;
                }
                for (OSMRelationMember member : relation.getMembers()) {
                    _areaWayIds.add(member.getRef());
                }
            } else if (!(relation.isTag("type", "restriction"))
                    && !(relation.isTag("type", "route") && relation.isTag("route", "road"))
                    && !(relation.isTag("type", "multipolygon") && relation.hasTag("highway"))
                    && !(relation.isTag("type", "level_map"))) {
                return;
            }

            _relations.put(relation.getId(), relation);

            if (_relations.size() % 100 == 0)
                _log.debug("relations=" + _relations.size());

        }

        public void secondPhase() {
            // This copies relevant tags to the ways (highway=*) where it doesn't exist, so that
            // the way purging keeps the needed way around.
            // Multipolygons may be processed more than once, which may be needed since
            // some member might be in different files for the same multipolygon.
            
            // NOTE (AMB): this purging phase may not be necessary if highway tags are not
            // copied over from multipolygon relations. Perhaps we can get by with 
            // only 2 steps -- ways+relations, followed by used nodes.
            // Ways can be tag-filtered in phase 1. 
            
            markNodesForKeeping(_ways.values(), _nodesWithNeighbors);
            markNodesForKeeping(_areaWaysById.values(), _areaNodes);
        }

        public void nodesLoaded() {
            processMultipolygons();
            AREA: for (OSMWay way : _singleWayAreas) {
                if (_processedAreas.contains(way)) {
                    continue;
                }
                for (Long nodeRef : way.getNodeRefs()) {
                    if (! _nodes.containsKey(nodeRef)) {
                        continue AREA;
                    }
                }
                try {
                    _areas.add(new Area(way, Arrays.asList(way), Collections.<OSMWay> emptyList()));
                } catch (Area.AreaConstructionException e) {
                    // this area cannot be constructed, but we already have all the
                    //necessary nodes to construct it. So, something must be wrong with
                    // the area; we'll mark it as processed so that we don't retry.
                }
                _processedAreas.add(way);
            }
            
        }

        private void markNodesForKeeping(Collection<OSMWay> osmWays, Set<Long> nodeSet) {
            for (Iterator<OSMWay> it = osmWays.iterator(); it.hasNext();) {
                OSMWay way = it.next();
                // Since the way is kept, update nodes-with-neighbors
                List<Long> nodes = way.getNodeRefs();
                if (nodes.size() > 1) {
                    nodeSet.addAll(nodes);
                }
            }
        }

        /**
         * Copies useful metadata from multipolygon relations to the relevant ways, or to the area
         * map
         * 
         * This is done at a different time than processRelations(), so that way purging doesn't
         * remove the used ways.
         */
        private void processMultipolygons() {
            RELATION: for (OSMRelation relation : _relations.values()) {
                if (_processedAreas.contains(relation)) {
                    continue;
                }
                if (!(relation.isTag("type", "multipolygon") && relation.hasTag("highway"))) {
                    continue;
                }
                // Area multipolygons -- pedestrian plazas
                ArrayList<OSMWay> innerWays = new ArrayList<OSMWay>();
                ArrayList<OSMWay> outerWays = new ArrayList<OSMWay>();
                for (OSMRelationMember member : relation.getMembers()) {
                    String role = member.getRole();
                    OSMWay way = _areaWaysById.get(member.getRef());
                    if (way == null) {
                        // relation includes way which does not exist in the data. Skip.
                        continue RELATION;
                    }
                    for (Long nodeId : way.getNodeRefs()) {
                        if (!_nodes.containsKey(nodeId)) {
                            // this area is missing some nodes, perhaps because it is on
                            // the edge of the region, so we will simply not route on it.
                            continue RELATION;
                        }
                        MapUtils.addToMapSet(_areasForNode, nodeId, way);
                    }
                    if (role.equals("inner")) {
                        innerWays.add(way);
                    } else if (role.equals("outer")) {
                        outerWays.add(way);
                    } else {
                        _log.warn("Unexpected role " + role + " in multipolygon");
                    }
                }
                _processedAreas.add(relation);
                Area area;
                try {
                    area = new Area(relation, outerWays, innerWays);
                } catch (Area.AreaConstructionException e) {
                    continue;
                }
                _areas.add(area);

                for (OSMRelationMember member : relation.getMembers()) {
                    //multipolygons for attribute mapping
                    if (!("way".equals(member.getType()) && _ways.containsKey(member.getRef()))) {
                        continue;
                    }

                    OSMWithTags way = _ways.get(member.getRef());
                    if (way == null) {
                        continue;
                    }

                    if (relation.hasTag("highway") && !way.hasTag("highway")) {
                        way.addTag("highway", relation.getTag("highway"));
                    }
                    if (relation.hasTag("name") && !way.hasTag("name")) {
                        way.addTag("name", relation.getTag("name"));
                    }
                    if (relation.hasTag("ref") && !way.hasTag("ref")) {
                        way.addTag("ref", relation.getTag("ref"));
                    }
                }
            }
        }

        /**
         * Copies useful metadata from relations to the relevant ways/nodes.
         */
        private void processRelations() {
            _log.debug("Processing relations...");

            for (OSMRelation relation : _relations.values()) {
                if (relation.isTag("type", "restriction")) {
                    processRestriction(relation);
                } else if (relation.isTag("type", "level_map")) {
                    processLevelMap(relation);
                } else if (relation.isTag("type", "route")) {
                    processRoad(relation);
                }

                // multipolygons will be further processed in secondPhase()
            }
        }

        /**
         * A temporary holder for turn restrictions while we have only way/node ids but not yet edge
         * objects
         */
        class TurnRestrictionTag {
            private long via;

            private TurnRestrictionType type;

            TurnRestrictionTag(long via, TurnRestrictionType type) {
                this.via = via;
                this.type = type;
            }
        }

        /**
         * Handle turn restrictions
         * 
         * @param relation
         */
        private void processRestriction(OSMRelation relation) {
            long from = -1, to = -1, via = -1;
            for (OSMRelationMember member : relation.getMembers()) {
                String role = member.getRole();
                if (role.equals("from")) {
                    from = member.getRef();
                } else if (role.equals("to")) {
                    to = member.getRef();
                } else if (role.equals("via")) {
                    via = member.getRef();
                }
            }
            if (from == -1 || to == -1 || via == -1) {
                _log.warn(GraphBuilderAnnotation.register(graph, Variety.TURN_RESTRICTION_BAD,
                        relation.getId()));
                return;
            }

            Set<TraverseMode> modes = EnumSet.of(TraverseMode.BICYCLE, TraverseMode.CAR);
            String exceptModes = relation.getTag("except");
            if (exceptModes != null) {
                for (String m : exceptModes.split(";")) {
                    if (m.equals("motorcar")) {
                        modes.remove(TraverseMode.CAR);
                    } else if (m.equals("bicycle")) {
                        modes.remove(TraverseMode.BICYCLE);
                        _log.warn(GraphBuilderAnnotation.register(graph,
                                Variety.TURN_RESTRICTION_EXCEPTION, via, from));
                    }
                }
            }
            modes = TraverseMode.internSet(modes);

            TurnRestrictionTag tag;
            if (relation.isTag("restriction", "no_right_turn")) {
                tag = new TurnRestrictionTag(via, TurnRestrictionType.NO_TURN);
            } else if (relation.isTag("restriction", "no_left_turn")) {
                tag = new TurnRestrictionTag(via, TurnRestrictionType.NO_TURN);
            } else if (relation.isTag("restriction", "no_straight_on")) {
                tag = new TurnRestrictionTag(via, TurnRestrictionType.NO_TURN);
            } else if (relation.isTag("restriction", "no_u_turn")) {
                tag = new TurnRestrictionTag(via, TurnRestrictionType.NO_TURN);
            } else if (relation.isTag("restriction", "only_straight_on")) {
                tag = new TurnRestrictionTag(via, TurnRestrictionType.ONLY_TURN);
            } else if (relation.isTag("restriction", "only_right_turn")) {
                tag = new TurnRestrictionTag(via, TurnRestrictionType.ONLY_TURN);
            } else if (relation.isTag("restriction", "only_left_turn")) {
                tag = new TurnRestrictionTag(via, TurnRestrictionType.ONLY_TURN);
            } else {
                _log.warn(GraphBuilderAnnotation.register(graph, Variety.TURN_RESTRICTION_UNKNOWN,
                        relation.getTag("restriction")));
                return;
            }
            TurnRestriction restriction = new TurnRestriction();
            restriction.type = tag.type;
            restriction.modes = modes;
            turnRestrictionsByTag.put(tag, restriction);

            MapUtils.addToMapList(turnRestrictionsByFromWay, from, tag);
            MapUtils.addToMapList(turnRestrictionsByToWay, to, tag);

        }

        /**
         * Process an OSM level map.
         * 
         * @param relation
         */
        private void processLevelMap(OSMRelation relation) {
            Map<String, OSMLevel> levels = OSMLevel.mapFromSpecList(relation.getTag("levels"), Source.LEVEL_MAP, true);
            for (OSMRelationMember member : relation.getMembers()) {
                if ("way".equals(member.getType()) && _ways.containsKey(member.getRef())) {
                    OSMWay way = _ways.get(member.getRef());
                    if (way != null) {
                        String role = member.getRole();
                        // if the level map relation has a role:xyz tag, this way is something
                        // more complicated than a single level (e.g. ramp/stairway).
                        if (!relation.hasTag("role:" + role)) {
                            if (levels.containsKey(role)) {
                                wayLevels.put(way, levels.get(role));
                            } else {
                                _log.warn(member.getRef() + " has undefined level " + role);
                            }
                        }
                    }
                }
            }
        }

        /*
         * Handle route=road relations.
         * 
         * @param relation
         */
        private void processRoad(OSMRelation relation) {
            for (OSMRelationMember member : relation.getMembers()) {
                if (!("way".equals(member.getType()) && _ways.containsKey(member.getRef()))) {
                    continue;
                }

                OSMWithTags way = _ways.get(member.getRef());
                if (way == null) {
                    continue;
                }

                if (relation.hasTag("name")) {
                    if (way.hasTag("otp:route_name")) {
                        way.addTag(
                                "otp:route_name",
                                addUniqueName(way.getTag("otp:route_name"), relation.getTag("name")));
                    } else {
                        way.addTag(new OSMTag("otp:route_name", relation.getTag("name")));
                    }
                }
                if (relation.hasTag("ref")) {
                    if (way.hasTag("otp:route_ref")) {
                        way.addTag("otp:route_ref",
                                addUniqueName(way.getTag("otp:route_ref"), relation.getTag("ref")));
                    } else {
                        way.addTag(new OSMTag("otp:route_ref", relation.getTag("ref")));
                    }
                }
            }
        }

        private String addUniqueName(String routes, String name) {
            String[] names = routes.split(", ");
            for (String existing : names) {
                if (existing.equals(name)) {
                    return routes;
                }
            }
            return routes + ", " + name;
        }

        /**
         * Handle oneway streets, cycleways, and whatnot. See
         * http://wiki.openstreetmap.org/wiki/Bicycle for various scenarios, along with
         * http://wiki.openstreetmap.org/wiki/OSM_tags_for_routing#Oneway.
         * 
         * @param end
         * @param start
         */
        private P2<PlainStreetEdge> getEdgesForStreet(IntersectionVertex start,
                IntersectionVertex end, OSMWithTags way, int index, long startNode, long endNode,
                StreetTraversalPermission permissions, LineString geometry) {
            // get geometry length in meters, irritatingly.
            Coordinate[] coordinates = geometry.getCoordinates();
            double d = 0;
            for (int i = 1; i < coordinates.length; ++i) {
                d += DistanceLibrary.distance(coordinates[i - 1], coordinates[i]);
            }

            LineString backGeometry = (LineString) geometry.reverse();

            Map<String, String> tags = way.getTags();

            if (permissions == StreetTraversalPermission.NONE)
                return new P2<PlainStreetEdge>(null, null);

            PlainStreetEdge street = null, backStreet = null;

            /*
             * pedestrian rules: everything is two-way (assuming pedestrians are allowed at all)
             * bicycle rules: default: permissions;
             * 
             * cycleway=dismount means walk your bike -- the engine will automatically try walking
             * bikes any time it is forbidden to ride them, so the only thing to do here is to
             * remove bike permissions
             * 
             * oneway=... sets permissions for cars and bikes oneway:bicycle overwrites these
             * permissions for bikes only
             * 
             * now, cycleway=opposite_lane, opposite, opposite_track can allow once oneway has been
             * set by oneway:bicycle, but should give a warning if it conflicts with oneway:bicycle
             * 
             * bicycle:backward=yes works like oneway:bicycle=no bicycle:backwards=no works like
             * oneway:bicycle=yes
             */

            String foot = way.getTag("foot");
            if ("yes".equals(foot) || "designated".equals(foot)) {
                permissions = permissions.add(StreetTraversalPermission.PEDESTRIAN);
            }

            if (OSMWithTags.isFalse(foot)) {
                permissions = permissions.remove(StreetTraversalPermission.PEDESTRIAN);
            }

            boolean forceBikes = false;
            String bicycle = way.getTag("bicycle");
            if ("yes".equals(bicycle) || "designated".equals(bicycle)) {
                permissions = permissions.add(StreetTraversalPermission.BICYCLE);
                forceBikes = true;
            }

            if (way.isTag("cycleway", "dismount") || "dismount".equals(bicycle)) {
                permissions = permissions.remove(StreetTraversalPermission.BICYCLE);
                if (forceBikes) {
                    _log.warn(GraphBuilderAnnotation.register(graph, Variety.CONFLICTING_BIKE_TAGS,
                            way.getId()));
                }
            }
            P2<StreetTraversalPermission> permissionPair = getPermissions(permissions, way);
            StreetTraversalPermission permissionsFront = permissionPair.getFirst();
            StreetTraversalPermission permissionsBack = permissionPair.getSecond();

            String access = way.getTag("access");
            boolean noThruTraffic = "destination".equals(access) || "private".equals(access)
                    || "customers".equals(access) || "delivery".equals(access)
                    || "forestry".equals(access) || "agricultural".equals(access);

            if (permissionsFront != StreetTraversalPermission.NONE) {
                street = getEdgeForStreet(start, end, way, index, startNode, endNode, d, permissionsFront,
                        geometry, false);
                street.setNoThruTraffic(noThruTraffic);
            }
            if (permissionsBack != StreetTraversalPermission.NONE) {
                backStreet = getEdgeForStreet(end, start, way, index, endNode, startNode, d, permissionsBack,
                        backGeometry, true);
                backStreet.setNoThruTraffic(noThruTraffic);
            }

            /* mark edges that are on roundabouts */
            if ("roundabout".equals(tags.get("junction"))) {
                if (street != null)
                    street.setRoundabout(true);
                if (backStreet != null)
                    backStreet.setRoundabout(true);
            }

            return new P2<PlainStreetEdge>(street, backStreet);
        }

        private P2<StreetTraversalPermission> getPermissions(StreetTraversalPermission permissions, OSMWithTags way) {

            StreetTraversalPermission permissionsFront = permissions;
            StreetTraversalPermission permissionsBack = permissions;

            if (way.isTagTrue("oneway") || "roundabout".equals(way.getTag("junction"))) {
                permissionsBack = permissionsBack.remove(StreetTraversalPermission.BICYCLE_AND_CAR);
            }
            if (way.isTag("oneway", "-1")) {
                permissionsFront = permissionsFront
                        .remove(StreetTraversalPermission.BICYCLE_AND_CAR);
            }
            String oneWayBicycle = way.getTag("oneway:bicycle");
            if (OSMWithTags.isTrue(oneWayBicycle) || way.isTagFalse("bicycle:backwards")) {
                permissionsBack = permissionsBack.remove(StreetTraversalPermission.BICYCLE);
            }
            if ("-1".equals(oneWayBicycle)) {
                permissionsFront = permissionsFront.remove(StreetTraversalPermission.BICYCLE);
            }
            if (OSMWithTags.isFalse(oneWayBicycle) || way.isTagTrue("bicycle:backwards")) {
                if (permissions.allows(StreetTraversalPermission.BICYCLE)) {
                    permissionsFront = permissionsFront.add(StreetTraversalPermission.BICYCLE);
                    permissionsBack = permissionsBack.add(StreetTraversalPermission.BICYCLE);
                }
            }

            // any cycleway which is opposite* allows contraflow biking
            String cycleway = way.getTag("cycleway");
            String cyclewayLeft = way.getTag("cycleway:left");
            String cyclewayRight = way.getTag("cycleway:right");
            if ((cycleway != null && cycleway.startsWith("opposite"))
                    || (cyclewayLeft != null && cyclewayLeft.startsWith("opposite"))
                    || (cyclewayRight != null && cyclewayRight.startsWith("opposite"))) {

                permissionsBack = permissionsBack.add(StreetTraversalPermission.BICYCLE);
            }
            return new P2<StreetTraversalPermission>(permissionsFront, permissionsBack);
        }

        private PlainStreetEdge getEdgeForStreet(IntersectionVertex start, IntersectionVertex end,
                OSMWithTags way, int index, long startNode, long endNode, double length,
                StreetTraversalPermission permissions, LineString geometry, boolean back) {

            String id = "way " + way.getId() + " from " + index;
            id = unique(id);

            String name = getNameForWay(way, id);

            boolean steps = "steps".equals(way.getTag("highway"));
            if (steps) {
                // consider the elevation gain of stairs, roughly
                length *= 2;
            }

            PlainStreetEdge street = edgeFactory
                    .createEdge(_nodes.get(startNode), _nodes.get(endNode), way, start, end,
                            geometry, name, length, permissions, back);
            street.setId(id);

            String highway = way.getTag("highway");
            if ("footway".equals(highway) && way.isTag("footway", "crossing") && !way.isTag("bicycle", "designated")) {
                street.setStreetClass(StreetEdge.CLASS_CROSSING);
            } else if ("residential".equals(highway) || "tertiary".equals(highway)
                    || "secondary".equals(highway) || "secondary_link".equals(highway)
                    || "primary".equals(highway) || "primary_link".equals(highway)
                    || "trunk".equals(highway) || "trunk_link".equals(highway)) {
                street.setStreetClass(StreetEdge.CLASS_STREET);
            } else {
                street.setStreetClass(StreetEdge.CLASS_OTHERPATH);
            }

            if (!way.hasTag("name")) {
                street.setBogusName(true);
            }
            street.setStairs(steps);

            /* TODO: This should probably generalized somehow? */
            if (way.isTagFalse("wheelchair") || (steps && !way.isTagTrue("wheelchair"))) {
                street.setWheelchairAccessible(false);
            }

            street.setSlopeOverride(wayPropertySet.getSlopeOverride(way));

            if (customNamer != null) {
                customNamer.nameWithEdge(way, street);
            }

            return street;
        }

        private String getNameForWay(OSMWithTags way, String id) {
            String name = way.getAssumedName();

            if (customNamer != null) {
                name = customNamer.name(way, name);
            }

            if (name == null) {
                name = id;
            }
            return name;
        }

        private StreetTraversalPermission getPermissionsForEntity(OSMWithTags entity,
                StreetTraversalPermission def) {
            Map<String, String> tags = entity.getTags();
            StreetTraversalPermission permission = null;

            String highway = tags.get("highway");
            String cycleway = tags.get("cycleway");
            String access = tags.get("access");
            String motorcar = tags.get("motorcar");
            String bicycle = tags.get("bicycle");
            String foot = tags.get("foot");

            /*
             * Only a few tags are examined here, because we only care about modes supported by OTP
             * (wheelchairs are not of concern here)
             * 
             * Only a few values are checked for, all other values are presumed to be permissive (=>
             * This may not be perfect, but is closer to reality, since most people don't follow the
             * rules perfectly ;-)
             */
            if (access != null) {
                if ("no".equals(access) || "license".equals(access)) {
                    // this can actually be overridden
                    permission = StreetTraversalPermission.NONE;
                    if (entity.doesTagAllowAccess("motorcar")) {
                        permission = permission.add(StreetTraversalPermission.CAR);
                    }
                    if (entity.doesTagAllowAccess("bicycle")) {
                        permission = permission.add(StreetTraversalPermission.BICYCLE);
                    }
                    if (entity.doesTagAllowAccess("foot")) {
                        permission = permission.add(StreetTraversalPermission.PEDESTRIAN);
                    }
                } else {
                    permission = def;
                }
            } else if (motorcar != null || bicycle != null || foot != null) {
                permission = def;
            }

            if (motorcar != null) {
                if ("no".equals(motorcar) || "license".equals(motorcar)) {
                    permission = permission.remove(StreetTraversalPermission.CAR);
                } else {
                    permission = permission.add(StreetTraversalPermission.CAR);
                }
            }

            if (bicycle != null) {
                if ("no".equals(bicycle) || "license".equals(bicycle)) {
                    permission = permission.remove(StreetTraversalPermission.BICYCLE);
                } else {
                    permission = permission.add(StreetTraversalPermission.BICYCLE);
                }
            }

            if (foot != null) {
                if ("no".equals(foot) || "license".equals(foot)) {
                    permission = permission.remove(StreetTraversalPermission.PEDESTRIAN);
                } else {
                    permission = permission.add(StreetTraversalPermission.PEDESTRIAN);
                }
            }

            if (highway != null) {
                if ("construction".equals(highway)) {
                    permission = StreetTraversalPermission.NONE;
                }
            } else {
                if ("construction".equals(cycleway)) {
                    permission = StreetTraversalPermission.NONE;
                }
            }

            if (permission == null)
                return def;

            return permission;
        }

        /**
         * Is this a multi-level node that should be decomposed to multiple coincident nodes?
         * Currently returns true only for elevators.
         * 
         * @param node
         * @return whether the node is multi-level
         * @author mattwigway
         */
        private boolean isMultiLevelNode(OSMNode node) {
            return node.hasTag("highway") && "elevator".equals(node.getTag("highway"));
        }

        /**
         * Record the level of the way for this node, e.g. if the way is at level 5, mark that this
         * node is active at level 5.
         * 
         * @param the way that has the level
         * @param the node to record for
         * @author mattwigway
         */
        private IntersectionVertex recordLevel(OSMNode node, OSMWithTags way) {
            OSMLevel level = wayLevels.get(way);
            HashMap<OSMLevel, IntersectionVertex> vertices;
            long nodeId = node.getId();
            if (multiLevelNodes.containsKey(nodeId)) {
                vertices = multiLevelNodes.get(nodeId);
            } else {
                vertices = new HashMap<OSMLevel, IntersectionVertex>();
                multiLevelNodes.put(nodeId, vertices);
            }
            if (!vertices.containsKey(level)) {
                Coordinate coordinate = getCoordinate(node);
                String label = "osm node " + nodeId + " at level " + level.shortName;
                IntersectionVertex vertex = new IntersectionVertex(graph, label, coordinate.x,
                        coordinate.y, label);
                vertices.put(level, vertex);
                // multilevel nodes should also undergo turn-conversion
                endpoints.add(vertex);
                return vertex;
            }
            return vertices.get(level);
        }

        /**
         * Make or get a shared vertex for flat intersections, or one vertex per level for
         * multilevel nodes like elevators. When there is an elevator or other Z-dimension
         * discontinuity, a single node can appear in several ways at different levels.
         * 
         * @param node The node to fetch a label for.
         * @param way The way it is connected to (for fetching level information).
         * @return vertex The graph vertex.
         */
        private IntersectionVertex getVertexForOsmNode(OSMNode node, OSMWithTags way) {
            // If the node should be decomposed to multiple levels,
            // use the numeric level because it is unique, the human level may not be (although
            // it will likely lead to some head-scratching if it is not).
            IntersectionVertex iv = null;
            if (isMultiLevelNode(node)) {
                // make a separate node for every level
                return recordLevel(node, way);
            }
            // single-level case
            long nid = node.getId();
            iv = intersectionNodes.get(nid);
            if (iv == null) {
                Coordinate coordinate = getCoordinate(node);
                String label = "osm node " + nid;
                iv = new IntersectionVertex(graph, label, coordinate.x, coordinate.y, label);
                intersectionNodes.put(nid, iv);
                endpoints.add(iv);
            }
            return iv;
        }

        @Override
        public void doneRelations() {
            //nothing to do here
        }
    }

    public CustomNamer getCustomNamer() {
        return customNamer;
    }

    public void setCustomNamer(CustomNamer customNamer) {
        this.customNamer = customNamer;
    }

    @Override
    public void checkInputs() {
        for (OpenStreetMapProvider provider : _providers) {
            provider.checkInputs();
        }
    }
}

>>>>>>> 76aa07461566a5976980e6696204781271955163
