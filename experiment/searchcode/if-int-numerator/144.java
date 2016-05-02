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

