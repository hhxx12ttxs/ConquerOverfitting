/*
 * Created on Jan 21, 2008
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * Copyright @2008 the original author or authors.
 */
package org.fest.swing.driver;

import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import org.fest.swing.core.Robot;
import org.fest.swing.exception.ActionFailedException;
import org.fest.swing.exception.UnexpectedException;

import static java.lang.Math.*;
import static java.lang.String.valueOf;
import static javax.swing.text.DefaultEditorKit.*;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.swing.driver.ComponentBoundsQuery.boundsOf;
import static org.fest.swing.driver.JComponentOriginQuery.originOf;
import static org.fest.swing.driver.JTextComponentEditableQuery.isEditable;
import static org.fest.swing.driver.JTextComponentSelectTextTask.selectTextInRange;
import static org.fest.swing.driver.JTextComponentSelectionEndQuery.selectionEndOf;
import static org.fest.swing.driver.JTextComponentSelectionStartQuery.selectionStartOf;
import static org.fest.swing.driver.JTextComponentSetTextTask.setTextIn;
import static org.fest.swing.exception.ActionFailedException.actionFailure;
import static org.fest.swing.format.Formatting.format;
import static org.fest.swing.query.ComponentEnabledQuery.isEnabled;
import static org.fest.swing.query.ComponentParentQuery.parentOf;
import static org.fest.swing.query.JComponentVisibleRectQuery.visibleRectOf;
import static org.fest.swing.query.JTextComponentTextQuery.textOf;
import static org.fest.util.Strings.*;

/**
 * Understands simulation of user input on a <code>{@link JTextComponent}</code>. Unlike
 * <code>JTextComponentFixture</code>, this driver only focuses on behavior present only in
 * <code>{@link JTextComponent}</code>s. This class is intended for internal use only.
 *
 * @author Alex Ruiz
 */
public class JTextComponentDriver extends JComponentDriver {

  private static final String EDITABLE_PROPERTY = "editable";
  private static final String TEXT_PROPERTY = "text";

  /**
   * Creates a new </code>{@link JTextComponentDriver}</code>.
   * @param robot the robot to use to simulate user input.
   */
  public JTextComponentDriver(Robot robot) {
    super(robot);
  }

  /**
   * Deletes the text of the <code>{@link JTextComponent}</code>.
   * @param textBox the target <code>JTextComponent</code>.
   */
  public void deleteText(JTextComponent textBox) {
    replaceText(textBox, null);
  }

  /**
   * Types the given text into the <code>{@link JTextComponent}</code>, replacing any existing text already there. If
   * the empty <code>String</code> or <code>null</code> is given, simply removes all existing text.
   * @param textBox the target <code>JTextComponent</code>.
   * @param text the text to enter.
   */
  public void replaceText(JTextComponent textBox, String text) {
    if (!isEnabled(textBox)) return;
    selectAll(textBox);
    if (isEmpty(text) && !isEmpty(textOf(textBox))) {
      invokeAction(textBox, deletePrevCharAction);
      return;
    }
    enterText(textBox, text);
  }

  /**
   * Selects the text in the <code>{@link JTextComponent}</code>.
   * @param textBox the target <code>JTextComponent</code>.
   */
  public void selectAll(JTextComponent textBox) {
    if (!isEnabled(textBox)) return;
    scrollToVisible(textBox, 0);
    invokeAction(textBox, selectAllAction);
  }

  /**
   * Types the given text into the <code>{@link JTextComponent}</code>.
   * @param textBox the target <code>JTextComponent</code>.
   * @param text the text to enter.
   */
  public void enterText(JTextComponent textBox, String text) {
    if (!isEnabled(textBox)) return;
    focus(textBox);
    robot.enterText(text);
  }

  /**
   * Sets the given text into the <code>{@link JTextComponent}</code>. Unlike 
   * <code>{@link #enterText(JTextComponent, String)}</code>, this method bypasses the event system and allows immediate 
   * updating on the underlying document model.
   * <p>
   * Primarily desired for speeding up tests when precise user event fidelity isn't necessary.
   * </p>
   * @param textBox the target <code>JTextComponent</code>.
   * @param text the text to enter.
   */
  public void setText(JTextComponent textBox, String text) {
    if (!isEnabled(textBox)) return;
    focus(textBox);
    setTextIn(textBox, text);
    robot.waitForIdle();
  }

  /**
   * Select the given text range.
   * @param textBox the target <code>JTextComponent</code>.
   * @param text the text to select.
   * @throws ActionFailedException if selecting the text fails.
   */
  public void selectText(JTextComponent textBox, String text) {
    if (!isEnabled(textBox)) return;
    String actualText = textOf(textBox);
    if (isEmpty(actualText)) return;
    int indexFound = actualText.indexOf(text);
    if (indexFound == -1) return;
    selectText(textBox, indexFound, indexFound + text.length());
  }

  /**
   * Select the given text range.
   * @param textBox the target <code>JTextComponent</code>.
   * @param start the starting index of the selection.
   * @param end the ending index of the selection.
   * @throws ActionFailedException if selecting the text in the given range fails.
   */
  public void selectText(JTextComponent textBox, int start, int end) {
    if (!isEnabled(textBox) || isEmpty(textOf(textBox))) return;
    robot.moveMouse(textBox, scrollToVisible(textBox, start));
    robot.moveMouse(textBox, scrollToVisible(textBox, end));
    selectTextInRange(textBox, start, end);
    verifySelectionMade(textBox, start, end);
  }

  /**
   * Move the pointer to the location of the given index. Takes care of auto-scrolling through text.
   * @param textBox the target <code>JTextComponent</code>.
   * @param index the given location.
   * @return the position of the pointer after being moved.
   * @throws ActionFailedException if it was not possible to scroll to the location of the given index.
   */
  private Point scrollToVisible(JTextComponent textBox, int index) {
    Rectangle indexLocation = locationOf(textBox, index);
    if (isRectangleVisible(textBox, indexLocation)) return centerOf(indexLocation);
    scrollToVisible(textBox, indexLocation);
    indexLocation = locationOf(textBox, index);
    if (isRectangleVisible(textBox, indexLocation)) return centerOf(indexLocation);
    throw actionFailure(concat(
        "Unable to make visible the location of the index '", valueOf(index),
        "' by scrolling the point (", formatOriginOf(indexLocation), ") on ", format(textBox)));
  }

  private Rectangle locationOf(JTextComponent textBox, int index) {
    Rectangle r = null;
    try {
      r = JTextComponentModelToViewQuery.modelToView(textBox, index);
    } catch (UnexpectedException e) {
      throw actionFailure(concat("Unable to get location for index '", valueOf(index), "' in ", format(textBox)));
    }
    if (r != null) return r;
    throw actionFailure(concat("Text component", format(textBox)," has zero size"));
  }

  private boolean isRectangleVisible(JTextComponent textBox, Rectangle r) {
    return visibleRectOf(textBox).contains(r.x, r.y);
  }

  private String formatOriginOf(Rectangle r) {
    return concat(valueOf(r.x), ",", valueOf(r.y));
  }

  private void scrollToVisible(JTextComponent textBox, Rectangle r) {
    super.scrollToVisible(textBox, r);
    // Taken from JComponent
    if (isVisible(textBox, r)) return;
    scrollToVisibleIfIsTextField(textBox, r);
  }

  private void scrollToVisibleIfIsTextField(JTextComponent textBox, Rectangle r) {
    if (!(textBox instanceof JTextField)) return;
    Point origin = originOf(textBox);
    Container parent = parentOf(textBox);
    while (parent != null && !(parent instanceof JComponent) && !(parent instanceof CellRendererPane)) {
      addRectangleCoordinatesToPoint(boundsOf(parent), origin);
      parent = parentOf(parent);
    }
    if (parent == null || parent instanceof CellRendererPane) return;
    super.scrollToVisible((JComponent)parent, rectangleWithPointAddedToCoordinates(origin, r));
  }

  private void addRectangleCoordinatesToPoint(Rectangle r, Point p) {
    p.x += r.x;
    p.y += r.y;
  }

  private Rectangle rectangleWithPointAddedToCoordinates(Point p, Rectangle r) {
    Rectangle destination = new Rectangle(r);
    destination.x += p.x;
    destination.y += p.y;
    return destination;
  }

  private Point centerOf(Rectangle r) {
    return new Point(r.x + r.width / 2, r.y + r.height / 2);
  }

  private void verifySelectionMade(JTextComponent textBox, int start, int end) {
    int actualStart = selectionStartOf(textBox);
    int actualEnd = selectionEndOf(textBox);
    if (actualStart == min(start, end) && actualEnd == max(start, end)) return;
    throw actionFailure(concat(
        "Unable to select text using indices '", valueOf(start), "' and '", valueOf(end),
        ", current selection starts at '", valueOf(actualStart), "' and ends at '", valueOf(actualEnd), "'"));
  }

  /**
   * Asserts that the text in the given <code>{@link JTextComponent}</code> is equal to the specified <code>String</code>.
   * @param textBox the given <code>JTextComponent</code>.
   * @param expected the text to match.
   * @throws AssertionError if the text of the <code>JTextComponent</code> is not equal to the given one.
   */
  public void requireText(JTextComponent textBox, String expected) {
    assertThat(textOf(textBox)).as(textProperty(textBox)).isEqualTo(expected);
  }

  /**
   * Asserts that the given <code>{@link JTextComponent}</code> is empty.
   * @param textBox the given <code>JTextComponent</code>.
   * @throws AssertionError if the <code>JTextComponent</code> is not empty.
   */
  public void requireEmpty(JTextComponent textBox) {
    assertThat(textOf(textBox)).as(textProperty(textBox)).isEmpty();
  }

  private static String textProperty(JTextComponent textBox) {
    return propertyName(textBox, TEXT_PROPERTY);
  }

  /**
   * Asserts that the given <code>{@link JTextComponent}</code> is editable.
   * @param textBox the given <code>JTextComponent</code>.
   * @throws AssertionError if the <code>JTextComponent</code> is not editable.
   */
  public void requireEditable(JTextComponent textBox) {
    assertEditable(textBox, true);
  }

  /**
   * Asserts that the given <code>{@link JTextComponent}</code> is not editable.
   * @param textBox the given <code>JTextComponent</code>.
   * @throws AssertionError if the <code>JTextComponent</code> is editable.
   */
  public void requireNotEditable(JTextComponent textBox) {
    assertEditable(textBox, false);
  }

  private void assertEditable(JTextComponent textBox, boolean editable) {
    assertThat(isEditable(textBox)).as(editableProperty(textBox)).isEqualTo(editable);
  }

  private static String editableProperty(JTextComponent textBox) {
    return propertyName(textBox, EDITABLE_PROPERTY);
  }
}

