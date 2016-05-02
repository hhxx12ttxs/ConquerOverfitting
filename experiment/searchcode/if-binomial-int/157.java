package cs276.pa4;

import java.io.FileNotFoundException;
import java.io.IOException;

public class NaiveBayesClassifier {

  private static void doBinomialChi2(final MessageIterator mi, final String train) throws FileNotFoundException,
      IOException, ClassNotFoundException {
    final Chi2ClassifierX mc = new Chi2ClassifierX(mi, train);
    mc.doBinomialChi2();

  }

  private static void doKfoldBinomialChi2(final MessageIteratorK mik,
			final String train) throws FileNotFoundException, IOException,
			ClassNotFoundException {
		final Chi2ClassifierX mc = new Chi2ClassifierX(mik, train);
		mc.runKfoldChi2ClassifierX();

  }
  private static void doBinomial(final MessageIterator mi) {
    final BinomialClassifier mc = new BinomialClassifier(mi);
    mc.doBinomial();
  }

  private static void doKFoldBinomial(final MessageIteratorK mik) {
	    final BinomialClassifier mc = new BinomialClassifier(mik);
	    mc.runKfoldBinomialNB();
  }
  
  public static void doKfoldMultinomial(final MessageIteratorK mik) {
		final MultinomialClassifier mc = new MultinomialClassifier(mik);
		mc.runKfoldMultinomial();
  }

  public static void doMultinomial(final MessageIterator mi) {
    final MultinomialClassifier mc = new MultinomialClassifier(mi);
    mc.doMultinomial();
  }

  public static void doTWCNB(final MessageIterator mi) {
    final TWCNBClassifier mc = new TWCNBClassifier(mi);
    mc.doTWCNB();
  }

  public static void doCNB(final MessageIterator mi) {
    final CNBClassifier mc = new CNBClassifier(mi);
    mc.doCNB();
  }

  public static void doWCNB(final MessageIterator mi) {
    final WCNBClassifier mc = new WCNBClassifier(mi);
    mc.doWCNB();
  }

  public static void outputProbability(final double[] probability) {
    for (int i = 0; i < probability.length; i++) {
      if (i == 0) {
        System.out.format("%1.8g", probability[i]);
      }
      else {
        System.out.format("\t%1.8g", probability[i]);
      }
    }
    System.out.format("%n");
  }

  public static void main(final String args[]) throws FileNotFoundException, IOException, ClassNotFoundException {
    if (args.length != 2) {
      System.err.println("Usage: NaiveBayesClassifier <mode> <train>");
      System.exit(-1);
    }
    final String mode = args[0];
    final String train = args[1];

    MessageIterator mi = null;
    MessageIteratorK mik = null;
    try {
      mi = new MessageIterator(train);
      mik = new MessageIteratorK(train);
    }
    catch (final Exception e) {
      System.err.println("Unable to create message iterator from file " + train);
      e.printStackTrace();
      System.exit(-1);
    }

    if (mode.equals("binomial")) {
      doBinomial(mi);
    }
    else if (mode.equals("binomial-chi2")) {
      doBinomialChi2(mi, train);
    }
    else if (mode.equals("multinomial")) {
      doMultinomial(mi);
    }
    else if (mode.equals("twcnb")) {
      doTWCNB(mi);
    }
    else if (mode.equals("cnb")) {
      doCNB(mi);
    }
    else if (mode.equals("wcnb")) {
      doWCNB(mi);
    }
    else if (mode.equals("multinomial-k")) {
        doKfoldMultinomial(mik);
      }
    else if (mode.equals("binomial-k")) {
        doKFoldBinomial(mik);
      }
    else if (mode.equals("binomial-chi2-k")) {
    	doKfoldBinomialChi2(mik, train);
      }
    else {
      // Add other test cases that you want to run here.

      System.err.println("Unknown mode " + mode);
      System.exit(-1);
    }
  }

}
