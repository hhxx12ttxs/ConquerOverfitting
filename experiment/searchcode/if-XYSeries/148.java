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
package de.htwg_konstanz.datamining.jlinreg.model;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Default implementation for {@link IInputData}.
 *
 * @author Juergen Fickel
 * @version 09.05.2011
 */
public final class InputDataImpl implements IInputData {

  private enum Column { INDEX, X, Y };

  private final String title;
  private final String indexColumnName;
  private final String xColumnName;
  private final String yColumnName;
  private final XYSeries values;

  /**
   * Creates a new instance of {@code InputDataImpl}.
   * 
   * @param csvFile
   *          file with the input data. This file must be a comma
   *          separated value file (*.csv) with at least two lines.
   * @throws JLinRegException
   *           if there is a problem with {@code csvFile}
   */
  public InputDataImpl(final String csvFile) throws JLinRegException {
    super();
    final List<String[]> rawData = this.readFile(csvFile);

    // Validate parameter
    if (null == rawData) {
      throw new NullPointerException("Parameter rawData must not be null!");
    } else  if (rawData.isEmpty()) {
      throw new JLinRegException("Paramter rawData contains no data!");
    }

    // Initialise title
    final String[] titleRow = rawData.get(0);
    if (1 != titleRow.length) {
      throw new JLinRegException("The data contains more than one title in the first row!");
    }
    this.title = titleRow[0];

    // Initialise column labels
    final String[] columnLabelsRow = rawData.get(1);
    if (Column.values().length != columnLabelsRow.length) {
      throw new JLinRegException("The data does not contain exactly three columns!");
    }
    this.indexColumnName = columnLabelsRow[Column.INDEX.ordinal()];
    this.xColumnName = columnLabelsRow[Column.X.ordinal()];
    this.yColumnName = columnLabelsRow[Column.Y.ordinal()];

    // Initialise X and Y values
    this.values = new XYSeries(this.title);
    for (int i = 2; i < rawData.size(); i++) {
      final String[] dataRow = rawData.get(i);
      if (Column.values().length != dataRow.length) {
        throw new JLinRegException("Data row " + (i - 1) + " does not contain enough values!");
      }
      final Double xValue = Double.parseDouble(dataRow[Column.X.ordinal()]);
      final Double yValue = Double.parseDouble(dataRow[Column.Y.ordinal()]);
      this.values.add(new XYDataItem(xValue, yValue));
    }
  }

  private List<String[]> readFile(final String csvFile) {
    List<String[]> result = Collections.emptyList();
    try {
      final CSVReader reader = new CSVReader(new FileReader(csvFile));
      result = reader.readAll();
    } catch (final FileNotFoundException e) {
      e.printStackTrace();
    } catch (final IOException e) {
      e.printStackTrace();
    }
    return result;
  }

  @Override
  public String getTitle() {
    return this.title;
  }

  @Override
  public String getIndexColumnName() {
    return this.indexColumnName;
  }

  @Override
  public String getXColumnName() {
    return this.xColumnName;
  }

  @Override
  public String getYColumnName() {
    return this.yColumnName;
  }

  @Override
  public int getValueCount() {
    return this.values.getItemCount();
  }

  @Override
  public List<Double> getXValues() {
    final List<Double> result = new ArrayList<Double>(this.values.getItemCount());
    @SuppressWarnings("unchecked") final List<XYDataItem> xyValues = this.values.getItems();
    for (final XYDataItem xyDataItem : xyValues) {
      result.add(xyDataItem.getXValue());
    }
    return result;
  }

  @Override
  public List<Double> getYValues() {
    final List<Double> result = new ArrayList<Double>(this.values.getItemCount());
    @SuppressWarnings("unchecked") final List<XYDataItem> xyValues = this.values.getItems();
    for (final XYDataItem xyDataItem : xyValues) {
      result.add(xyDataItem.getYValue());
    }
    return result;
  }

  @Override
  public XYSeries getAsSeries() {
    XYSeries result = null;
    try {
      result = (XYSeries) this.values.clone();
    } catch (final CloneNotSupportedException e) {
      e.printStackTrace();
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("InputDataImpl [title=").append(title).append(", indexColumnName=").append(indexColumnName)
        .append(", xColumnName=").append(xColumnName).append(", yColumnName=").append(yColumnName).append(", values=")
        .append(values).append("]");
    return builder.toString();
  }

}

