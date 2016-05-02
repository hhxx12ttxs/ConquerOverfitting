/*
 * jLinReg
 * Copyright (C) 2011 Juergen Fickel
 *
 * See <https://github.com/jufickel/jlinreg> for the project page on
 * github.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License,
 * version 3, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.htwg_konstanz.datamining.jlinreg.core;

import java.util.List;

import net.jcip.annotations.NotThreadSafe;

import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;

import de.htwg_konstanz.datamining.jlinreg.model.IInputData;

/**
 * Default implementation of the calculation of the linear regression
 * algorithm.
 *
 * @author Juergen Fickel
 * @version 08.05.2011
 */
@NotThreadSafe
public final class CalculationImpl implements ICalculation {

  private static final CalculationImpl INSTANCE = new CalculationImpl();

  private CalculationImpl() {
    super();
  }

  /**
   * Static factory method.
   *
   * @return the sole INSTANCE of {@code CalculationImpl}
   */
  public static ICalculation getInstance() {
    return INSTANCE;
  }

  @Override
  public double getArithmeticMean(final List<Double> values) {
    double sum = 0.0d;
    for (final double value : values) {
      sum += value;
    }
    return sum / (double) values.size();
  }

  @Override
  public double getSlope(final IInputData xyValues) {
    double result = 0.0d;
    final double xMean = this.getArithmeticMean(xyValues.getXValues());
    final double yMean = this.getArithmeticMean(xyValues.getYValues());
    final double sumOfPointProducts = this.sumPointProducts(xyValues.getAsSeries());
    final double sumOfXsquare = this.sumXsquare(xyValues.getXValues());
    final int n = xyValues.getValueCount();
    final double numerator = sumOfPointProducts - (n * xMean * yMean);
    final double denumerator = sumOfXsquare - n * (xMean * xMean);
    result = (0.0d == denumerator) ? 0.0d : numerator / denumerator;
    return result;
  }

  private double sumPointProducts(final XYSeries series) {
    double result = 0.0d;
    @SuppressWarnings("unchecked") final List<XYDataItem> items = series.getItems();
    for (final XYDataItem xyDataItem : items) {
      result += (xyDataItem.getXValue() * xyDataItem.getYValue());
    }
    return result;
  }

  private double sumXsquare(final List<Double> xValues) {
    double result = 0.0d;
    for (final Double xValue : xValues) {
      result += (xValue * xValue);
    }
    return result;
  }

  @Override
  public double getYintercept(final double yMean, final double slope, final double xMean) {
    double result = 0.0d;
    result = yMean - slope * xMean;
    return result;
  }

  @Override
  public double getCorrelationCoefficient(final IInputData xyValues) {
    double result = 0.0d;
    final double xMean = this.getArithmeticMean(xyValues.getXValues());
    final double yMean = this.getArithmeticMean(xyValues.getYValues());

    // Calculate the nominator
    double nominator = 0.0d;
    @SuppressWarnings("unchecked") final List<XYDataItem> dataItems = xyValues.getAsSeries().getItems();
    for (final XYDataItem xyDataItem : dataItems) {
      nominator += (xyDataItem.getXValue() - xMean) * (xyDataItem.getYValue() - yMean);
    }
    
    // Calculate the denominator
    final double varianceOfX = this.getVariance(xyValues.getXValues(), xMean);
    final double varianceOfY = this.getVariance(xyValues.getYValues(), yMean);
    final double denominator = Math.sqrt(varianceOfX) * Math.sqrt(varianceOfY);

    result = (0.0d == denominator) ? 0.0d : nominator / denominator;
    return result;
  }

  private double getVariance(final List<Double> items, final double mean) {
    double result = 0.0d;
    for (final Double item : items) {
      final double distanceFromMean = item - mean;
      result += (distanceFromMean * distanceFromMean);
    }
    return result;
  }

  @Override
  public double getMinimum(final List<Double> values) {
    double result = Double.MAX_VALUE;
    for (final Double value : values) {
      if (result > value) {
        result = value;
      }
    }
    return result;
  }

  @Override
  public double getMaximum(final List<Double> values) {
    double result = Double.MIN_VALUE;
    for (final Double value : values) {
      if (result < value) {
        result = value;
      }
    }
    return result;
  }

  @Override
  public double predict(final double xValue, final double slope, final double yIntecept) {
    double result = 0.0d;
    result = xValue * slope + yIntecept;
    return result;
  }

}

