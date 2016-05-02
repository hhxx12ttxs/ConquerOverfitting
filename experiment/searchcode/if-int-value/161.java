/*
 * Created on Jan 27, 2008
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 * Copyright @2008 the original author or authors.
 */
package org.fest.swing.driver;

import java.awt.Insets;
import java.awt.Point;

import javax.swing.JSlider;

import org.fest.swing.edt.GuiQuery;

import static javax.swing.SwingConstants.VERTICAL;

import static org.fest.swing.awt.AWT.centerOf;
import static org.fest.swing.driver.JSliderOrientationQuery.orientationOf;
import static org.fest.swing.edt.GuiActionRunner.execute;

/**
 * Understands a location in a <code>{@link JSlider}</code>.
 *
 * @author Alex Ruiz
 * @author Yvonne Wang
 */
public final class JSliderLocation {

  /**
   * Returns the coordinates of the given value in the given <code>{@link JSlider}</code>.
   * @param slider the given <code>JSlider</code>.
   * @param value the given value.
   * @return the coordinates of the given value in the given <code>JSlider</code>.
   */
  public Point pointAt(JSlider slider, int value) {
    if (orientationOf(slider) == VERTICAL) return locationForVerticalOrientation(slider, value);
    return locationForHorizontalOrientation(slider, value);
  }

  private static Point locationForVerticalOrientation(JSlider slider, int value) {
    return execute(new JSliderLocationQueryStrategy(slider, value) {
      int max(Insets insets) {
        return slider().getHeight() - insets.top - insets.bottom - 1;
      }

      int coordinateOf(Point center) {
        return center.y;
      }

      Point update(Point center, int coordinate) {
        return new Point(center.x, coordinate);
      }
    });
  }

  private static Point locationForHorizontalOrientation(JSlider slider, int value) {
    return execute(new JSliderLocationQueryStrategy(slider, value) {
      int max(Insets insets) {
        return slider().getWidth() - insets.left - insets.right - 1;
      }

      int coordinateOf(Point center) {
        return center.x;
      }

      Point update(Point center, int coordinate) {
        return new Point(coordinate, center.y);
      }
    });
  }

  private static abstract class JSliderLocationQueryStrategy extends GuiQuery<Point> {
    private final JSlider slider;
    private final int value;

    JSliderLocationQueryStrategy(JSlider slider, int value) {
      this.slider = slider;
      this.value = value;
    }

    protected final Point executeInEDT() {
      Point center = centerOf(slider);
      int max = max(slider.getInsets());
      int coordinate = coordinateOf(center);
      coordinate = (int)(percent() * max);
      if (slider.getInverted()) coordinate = max - coordinate;
      return update(center, coordinate);
    }

    abstract int max(Insets insets);

    abstract int coordinateOf(Point center);

    abstract Point update(Point center, int coordinate);

    private float percent() {
      int minimum = slider.getMinimum();
      int range = slider.getMaximum() - minimum;
      return (float)(value - minimum) / range;
    }

    JSlider slider() { return slider; }
  }
}

