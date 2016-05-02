<<<<<<< HEAD
/*
 * Copyright 2012 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.pdf417.decoder.ec;

import com.google.zxing.ChecksumException;

/**
 * <p>PDF417 error correction implementation.</p>
 *
 * <p>This <a href="http://en.wikipedia.org/wiki/Reed%E2%80%93Solomon_error_correction#Example">example</a>
 * is quite useful in understanding the algorithm.</p>
 *
 * @author Sean Owen
 * @see com.google.zxing.common.reedsolomon.ReedSolomonDecoder
 */
public final class ErrorCorrection {

  private final ModulusGF field;

  public ErrorCorrection() {
    this.field = ModulusGF.PDF417_GF;
  }

  /**
   * @param received received codewords
   * @param numECCodewords number of those codewords used for EC
   * @param erasures location of erasures
   * @return number of errors
   * @throws ChecksumException if errors cannot be corrected, maybe because of too many errors
   */
  public int decode(int[] received,
                    int numECCodewords,
                    int[] erasures) throws ChecksumException {

    ModulusPoly poly = new ModulusPoly(field, received);
    int[] S = new int[numECCodewords];
    boolean error = false;
    for (int i = numECCodewords; i > 0; i--) {
      int eval = poly.evaluateAt(field.exp(i));
      S[numECCodewords - i] = eval;
      if (eval != 0) {
        error = true;
      }
    }

    if (!error) {
      return 0;
    }

    ModulusPoly knownErrors = field.getOne();
    if (erasures != null) {
      for (int erasure : erasures) {
        int b = field.exp(received.length - 1 - erasure);
        // Add (1 - bx) term:
        ModulusPoly term = new ModulusPoly(field, new int[]{field.subtract(0, b), 1});
        knownErrors = knownErrors.multiply(term);
      }
    }

    ModulusPoly syndrome = new ModulusPoly(field, S);
    //syndrome = syndrome.multiply(knownErrors);

    ModulusPoly[] sigmaOmega =
        runEuclideanAlgorithm(field.buildMonomial(numECCodewords, 1), syndrome, numECCodewords);
    ModulusPoly sigma = sigmaOmega[0];
    ModulusPoly omega = sigmaOmega[1];

    //sigma = sigma.multiply(knownErrors);

    int[] errorLocations = findErrorLocations(sigma);
    int[] errorMagnitudes = findErrorMagnitudes(omega, sigma, errorLocations);

    for (int i = 0; i < errorLocations.length; i++) {
      int position = received.length - 1 - field.log(errorLocations[i]);
      if (position < 0) {
        throw ChecksumException.getChecksumInstance();
      }
      received[position] = field.subtract(received[position], errorMagnitudes[i]);
    }
    return errorLocations.length;
  }

  private ModulusPoly[] runEuclideanAlgorithm(ModulusPoly a, ModulusPoly b, int R)
      throws ChecksumException {
    // Assume a's degree is >= b's
    if (a.getDegree() < b.getDegree()) {
      ModulusPoly temp = a;
      a = b;
      b = temp;
    }

    ModulusPoly rLast = a;
    ModulusPoly r = b;
    ModulusPoly tLast = field.getZero();
    ModulusPoly t = field.getOne();

    // Run Euclidean algorithm until r's degree is less than R/2
    while (r.getDegree() >= R / 2) {
      ModulusPoly rLastLast = rLast;
      ModulusPoly tLastLast = tLast;
      rLast = r;
      tLast = t;

      // Divide rLastLast by rLast, with quotient in q and remainder in r
      if (rLast.isZero()) {
        // Oops, Euclidean algorithm already terminated?
        throw ChecksumException.getChecksumInstance();
      }
      r = rLastLast;
      ModulusPoly q = field.getZero();
      int denominatorLeadingTerm = rLast.getCoefficient(rLast.getDegree());
      int dltInverse = field.inverse(denominatorLeadingTerm);
      while (r.getDegree() >= rLast.getDegree() && !r.isZero()) {
        int degreeDiff = r.getDegree() - rLast.getDegree();
        int scale = field.multiply(r.getCoefficient(r.getDegree()), dltInverse);
        q = q.add(field.buildMonomial(degreeDiff, scale));
        r = r.subtract(rLast.multiplyByMonomial(degreeDiff, scale));
      }

      t = q.multiply(tLast).subtract(tLastLast).negative();
    }

    int sigmaTildeAtZero = t.getCoefficient(0);
    if (sigmaTildeAtZero == 0) {
      throw ChecksumException.getChecksumInstance();
    }

    int inverse = field.inverse(sigmaTildeAtZero);
    ModulusPoly sigma = t.multiply(inverse);
    ModulusPoly omega = r.multiply(inverse);
    return new ModulusPoly[]{sigma, omega};
  }

  private int[] findErrorLocations(ModulusPoly errorLocator) throws ChecksumException {
    // This is a direct application of Chien's search
    int numErrors = errorLocator.getDegree();
    int[] result = new int[numErrors];
    int e = 0;
    for (int i = 1; i < field.getSize() && e < numErrors; i++) {
      if (errorLocator.evaluateAt(i) == 0) {
        result[e] = field.inverse(i);
        e++;
      }
    }
    if (e != numErrors) {
      throw ChecksumException.getChecksumInstance();
    }
    return result;
  }

  private int[] findErrorMagnitudes(ModulusPoly errorEvaluator,
                                    ModulusPoly errorLocator,
                                    int[] errorLocations) {
    int errorLocatorDegree = errorLocator.getDegree();
    int[] formalDerivativeCoefficients = new int[errorLocatorDegree];
    for (int i = 1; i <= errorLocatorDegree; i++) {
      formalDerivativeCoefficients[errorLocatorDegree - i] =
          field.multiply(i, errorLocator.getCoefficient(i));
    }
    ModulusPoly formalDerivative = new ModulusPoly(field, formalDerivativeCoefficients);

    // This is directly applying Forney's Formula
    int s = errorLocations.length;
    int[] result = new int[s];
    for (int i = 0; i < s; i++) {
      int xiInverse = field.inverse(errorLocations[i]);
      int numerator = field.subtract(0, errorEvaluator.evaluateAt(xiInverse));
      int denominator = field.inverse(formalDerivative.evaluateAt(xiInverse));
      result[i] = field.multiply(numerator, denominator);
    }
    return result;
  }
}
=======
/*
 * Copyright 2010 bufferings[at]gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package bufferings.ktr.wjr.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * The result panel which shows the result summary of the tests.
 * 
 * @author bufferings[at]gmail.com
 */
public class WjrResultPanel extends Composite {

  private static WjrResultPanelUiBinder uiBinder =
    GWT.create(WjrResultPanelUiBinder.class);

  interface WjrResultPanelUiBinder extends UiBinder<Widget, WjrResultPanel> {
  }

  /**
   * The result bar style css resource.
   * 
   * @author bufferings[at]gmail.com
   */
  protected interface ResultBarStyle extends CssResource {
    /**
     * The style when the result is not yet state.
     * 
     * @return The style when the result is not yet state.
     */
    String notyet();

    /**
     * The style when the result is fail state.
     * 
     * @return The style when the result is fail state.
     */
    String fail();

    /**
     * The style when the result is succeed state.
     * 
     * @return The style when the result is succeed state.
     */
    String succeed();
  }

  /**
   * The style of the result bar.
   */
  @UiField
  protected ResultBarStyle resultBarStyle;

  /**
   * The label to show the total tests count.
   */
  @UiField
  protected HasText totalLabel;

  /**
   * The label to show the run tests count.
   */
  @UiField
  protected HasText runsLabel;

  /**
   * The label to show the error tests count.
   */
  @UiField
  protected HasText errorsLabel;

  /**
   * The label to show the failure tests count.
   */
  @UiField
  protected HasText failuresLabel;

  /**
   * The label to show the result state.
   */
  @UiField
  protected Label resultBar;

  /**
   * Constructs the WjrResultPanel.
   */
  public WjrResultPanel() {
    initWidget(uiBinder.createAndBindUi(this));
    updateResults(0, 0, 0, 0, 0);
  }

  /**
   * Updates the result.
   * 
   * @param runningsCount
   *          The running tests count.
   * @param runsCount
   *          The run tests count.
   * @param totalCount
   *          The total tests count.
   * @param errorsCount
   *          The error tests count.
   * @param failuresCount
   *          The failure tests count.
   */
  public void updateResults(int runningsCount, int runsCount, int totalCount,
      int errorsCount, int failuresCount) {
    totalLabel.setText(createTotalLabelString(totalCount));
    runsLabel.setText(createRunsLabelString(runsCount, runningsCount
      + runsCount));
    errorsLabel.setText(createErrorsLabelString(errorsCount));
    failuresLabel.setText(createFailuresLabelString(failuresCount));

    if (errorsCount > 0 || failuresCount > 0) {
      resultBar.setStyleName(resultBarStyle.fail());
    } else if (runsCount > 0) {
      resultBar.setStyleName(resultBarStyle.succeed());
    } else {
      resultBar.setStyleName(resultBarStyle.notyet());
    }

    String width = calcPercent(runsCount, runningsCount + runsCount) + "%";
    resultBar.setWidth(width);
  }

  /**
   * Calculates the percent value.
   * 
   * @param numerator
   *          The numerator.
   * @param denominator
   *          The denominator.
   * @return The percent value.
   */
  protected int calcPercent(int numerator, int denominator) {
    if (denominator == 0) {
      return 0;
    } else {
      return (int) ((double) numerator * 100 / denominator);
    }
  }

  /**
   * Creates the total label string from run tests count.
   * 
   * @param totalCount
   *          The total count.
   * @return
   */
  protected String createTotalLabelString(int totalCount) {
    return new StringBuilder().append("Total: ").append(totalCount).toString();
  }

  /**
   * Creates the runs label string from run tests count and run and running
   * tests count.
   * 
   * @param runsCount
   *          The run tests count.
   * @param runsAndRunningsCount
   *          The run tests count and running tests count.
   * @return
   */
  protected String createRunsLabelString(int runsCount, int runsAndRunningsCount) {
    return new StringBuilder()
      .append("Runs: ")
      .append(runsCount)
      .append("/")
      .append(runsAndRunningsCount)
      .toString();
  }

  /**
   * Creates the errors label string from error tests count.
   * 
   * @param errorsCount
   *          The error tests count.
   * @return The errors label string.
   */
  protected String createErrorsLabelString(int errorsCount) {
    return new StringBuilder()
      .append("Errors: ")
      .append(errorsCount)
      .toString();
  }

  /**
   * Creates the failures label string from failure tests count.
   * 
   * @param failuresCount
   *          The failure tests count.
   * @return The failures label string.
   */
  protected String createFailuresLabelString(int failuresCount) {
    return new StringBuilder()
      .append("Failures: ")
      .append(failuresCount)
      .toString();
  }
}
>>>>>>> 76aa07461566a5976980e6696204781271955163

