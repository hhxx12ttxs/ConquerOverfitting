/*
Copyright (C) 2009 Grant Slender

This file is part of OFCGWT.
http://code.google.com/p/ofcgwt/

OFCGWT is free software: you can redistribute it and/or modify
it under the terms of the Lesser GNU General Public License as
published by the Free Software Foundation, either version 3 of
the License, or (at your option) any later version.

OFCGWT is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

See <http://www.gnu.org/licenses/lgpl-3.0.txt>.
 */
package com.rednels.ofcgwt.client.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Vector;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.rednels.ofcgwt.client.ChartWidget;
import com.rednels.ofcgwt.client.model.axis.RadarAxis;
import com.rednels.ofcgwt.client.model.axis.XAxis;
import com.rednels.ofcgwt.client.model.axis.YAxis;
import com.rednels.ofcgwt.client.model.elements.Element;

/**
 * This is the most important class in the OFCGWT library. Start here,
 * configuring the title, axes, legends, labels, and draw-able elements in your
 * chart. You add an element to the chart data, for example...</br>
 * 
 * <pre>
 * ChartData cd = new ChartData(&quot;Sales by Region&quot;);
 * PieChart pie = new PieChart();
 * pie.addValues(10, 30, 40, 20);
 * cd.addElements(pie);
 * </pre>
 * 
 * When finished, call {@link ChartWidget#setChartData(ChartData)} and the GWT
 * JSON objects will convert the chart data into a formatted OFC2 JSON data
 * string. It will also attach the events correctly and ensure all the callbacks are handled.
 */
/**
 * @author Grant Slender
 * 
 */
public class ChartData implements JSONizable {
	private Text title;
	private ToolTip tooltip;
	private XAxis x_axis;
	private YAxis y_axis;
	private RadarAxis radar_axis;
	private YAxis y_axis_right;
	private Text y_legend;
	private Text y2_legend;
	private Text x_legend;
	private Legend legend;
	private String bg_colour;
	private String yaxis_label_style;
	private String yaxisright_label_style;
	private final Collection<Element> elements = new Vector<Element>();
	private boolean isDecimalSeparatorComma = false;
	private boolean isFixedNumDecimalsForced = false;
	private boolean isThousandSeparatorDisabled = false;

	private Integer numDecimals;

	/**
	 * Creates a new chart data instance.
	 */
	public ChartData() {
	// nothing...
	}

	/**
	 * Creates a new chart data instance with the given title.
	 * 
	 * @param titleText
	 */
	public ChartData(String titleText) {
		this(titleText, null);
	}

	/**
	 * Creates a new chart data instance with the given title and style.
	 * 
	 * @param titleText
	 * @param style
	 */
	public ChartData(String titleText, String style) {
		setTitle(new Text(titleText, style));
	}

	/**
	 * Adds a collection of chart elements to the list of elements
	 * 
	 * @param collection
	 *            of type Element
	 */
	public void addElements(Collection<Element> collection) {
		this.elements.addAll(collection);
	}

	/**
	 * Adds an element to the list of elements
	 * 
	 * @param e
	 *            the element
	 */
	public void addElements(Element... e) {
		this.elements.addAll(Arrays.asList(e));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rednels.ofcgwt.client.model.JSONizable.buildJSON()
	 */
	public JSONValue buildJSON() {
		final JSONObject json = new JSONObject();
		if (title != null) json.put("title", title.buildJSON());
		if (tooltip != null) json.put("tooltip", tooltip.buildJSON());
		if (x_axis != null) json.put("x_axis", x_axis.buildJSON());
		if (y_axis != null) json.put("y_axis", y_axis.buildJSON());
		if (yaxis_label_style != null) json.put("y_label__label_style", new JSONString(yaxis_label_style));
		if (y_axis_right != null) json.put("y_axis_right", y_axis_right.buildJSON());
		if (yaxisright_label_style != null) json.put("y_label_2__label_style", new JSONString(yaxisright_label_style));
		if (radar_axis != null) json.put("radar_axis", radar_axis.buildJSON());
		if (y_legend != null) json.put("y_legend", y_legend.buildJSON());
		if (y2_legend != null) json.put("y2_legend", y2_legend.buildJSON());
		if (x_legend != null) json.put("x_legend", x_legend.buildJSON());
		if (legend != null) json.put("legend", legend.buildJSON());
		if (bg_colour != null) json.put("bg_colour", new JSONString(bg_colour));
		if (isDecimalSeparatorComma) json.put("is_decimal_separator_comma", new JSONNumber(1));
		if (isFixedNumDecimalsForced) json.put("is_fixed_num_decimals_forced", new JSONNumber(1));
		if (isThousandSeparatorDisabled) json.put("is_thousand_separator_disabled", new JSONNumber(1));
		if (numDecimals != null) json.put("num_decimals", new JSONNumber(numDecimals));
		if (elements == null) return json;
		final JSONArray ary = new JSONArray();
		int index = 0;
		for (final Element e : elements) {
			ary.set(index++, e.buildJSON());
		}
		json.put("elements", ary);
		return json;
	}

	/**
	 * Get the current background colour
	 * 
	 * @return String background colour
	 */
	public String getBackgroundColour() {
		return bg_colour;
	}

	/**
	 * Get the current elements collection
	 * 
	 * @return Element collection
	 */
	public Collection<Element> getElements() {
		return elements;
	}

	/**
	 * Get the current chart legend
	 * 
	 * @return Legend chart legend
	 */
	public Legend getLegend() {
		return legend;
	}

	/**
	 * @return the numDecimals
	 */
	public Integer getNumDecimals() {
		return numDecimals;
	}

	/**
	 * Get the current RadarAxis object
	 * 
	 * @return RadarAxis object
	 */
	public RadarAxis getRadarAxis() {
		if (radar_axis == null) radar_axis = new RadarAxis();
		return radar_axis;
	}

	/**
	 * Get the current title Text
	 * 
	 * @return Text title
	 */
	public Text getTitle() {
		return title;
	}

	/**
	 * Gets the tooltip.
	 * 
	 * @return the tooltip
	 */
	public ToolTip getTooltipStyle() {
		return tooltip;
	}

	/**
	 * Get the current XAxis object
	 * 
	 * @return XAxis object
	 */
	public XAxis getXAxis() {
		if (x_axis == null) x_axis = new XAxis();
		return x_axis;
	}

	/**
	 * Get the current x legend Text
	 * 
	 * @return Text x legend
	 */
	public Text getXLegend() {
		return x_legend;
	}

	/**
	 * Get the current YAxis object (left side)
	 * 
	 * @return YAxis object
	 */
	public YAxis getYAxis() {
		if (y_axis == null) y_axis = new YAxis();
		return y_axis;
	}

	/**
	 * Get the current YAxis object (right side)
	 * 
	 * @return YAxis object
	 */
	public YAxis getYAxisRight() {
		return y_axis_right;
	}

	/**
	 * Get the current y legend Text
	 * 
	 * @return Text y legend
	 */
	public Text getYLegend() {
		return y_legend;
	}

	/**
	 * Get the current y right legend Text
	 * 
	 * @return Text y right legend
	 */
	public Text getYRightLegend() {
		return this.y2_legend;
	}

	/**
	 * @return the isDecimalSeparatorComma
	 */
	public boolean isDecimalSeparatorComma() {
		return isDecimalSeparatorComma;
	}

	/**
	 * @return the isFixedNumDecimalsForced
	 */
	public boolean isFixedNumDecimalsForced() {
		return isFixedNumDecimalsForced;
	}

	/**
	 * @return the isThousandSeparatorDisabled
	 */
	public boolean isThousandSeparatorDisabled() {
		return isThousandSeparatorDisabled;
	}

	/**
	 * Removes an element from the list of elements
	 * 
	 * @param e
	 *            the element
	 * @return true if an element was removed as a result of this call
	 */
	public boolean removeElement(Element e) {
		return this.elements.remove(e);
	}

	/**
	 * Sets the chart background colour in HTML hex format (#ffffff). Set to
	 * "-1" to set transparent.
	 * 
	 * @param bg_colour
	 *            String colour
	 */
	public void setBackgroundColour(String bg_colour) {
		this.bg_colour = bg_colour;
	}

	/**
	 * @param isDecimalSeparatorComma
	 *            the isDecimalSeparatorComma to set
	 */
	public void setDecimalSeparatorComma(boolean isDecimalSeparatorComma) {
		this.isDecimalSeparatorComma = isDecimalSeparatorComma;
	}

	/**
	 * Clears and then sets the list of elements to this collection
	 * 
	 * @param elements
	 *            Collection
	 */
	public void setElements(Collection<Element> elements) {
		this.elements.clear();
		this.elements.addAll(elements);
	}

	/**
	 * @param isFixedNumDecimalsForced
	 *            the isFixedNumDecimalsForced to set
	 */
	public void setFixedNumDecimalsForced(boolean isFixedNumDecimalsForced) {
		this.isFixedNumDecimalsForced = isFixedNumDecimalsForced;
	}

	/**
	 * Sets the chart legend
	 * 
	 * @param legend
	 *            Legend object
	 */
	public void setLegend(Legend legend) {
		this.legend = legend;
	}

	/**
	 * @param numDecimals
	 *            the numDecimals to set
	 */
	public void setNumDecimals(Integer numDecimals) {
		this.numDecimals = numDecimals;
	}

	/**
	 * Sets the RadarAxis to this RadarAxis object
	 * 
	 * @param radar_axis
	 *            RadarAxis object
	 */
	public void setRadarAxis(RadarAxis radar_axis) {
		this.radar_axis = radar_axis;
	}

	/**
	 * @param isThousandSeparatorDisabled
	 *            the isThousandSeparatorDisabled to set
	 */
	public void setThousandSeparatorDisabled(boolean isThousandSeparatorDisabled) {
		this.isThousandSeparatorDisabled = isThousandSeparatorDisabled;
	}

	/**
	 * Sets the title to this Text object
	 * 
	 * @param title
	 *            Text object
	 */
	public void setTitle(Text title) {
		this.title = title;
	}

	/**
	 * Sets the tooltip.
	 * 
	 * @param tooltip
	 *            the new tooltip
	 */
	public void setTooltipStyle(ToolTip tooltip) {
		this.tooltip = tooltip;
	}

	/**
	 * Sets the XAxis to this XAxis object
	 * 
	 * @param x_axis
	 *            XAxis object
	 */
	public void setXAxis(XAxis x_axis) {
		this.x_axis = x_axis;
	}

	/**
	 * Sets the x legend to this Text object
	 * 
	 * @param x_legend
	 *            Text object
	 */
	public void setXLegend(Text x_legend) {
		this.x_legend = x_legend;
	}

	/**
	 * Sets the left YAxis to this YAxis object
	 * 
	 * @param y_axis
	 *            YAxis object
	 */
	public void setYAxis(YAxis y_axis) {
		this.y_axis = y_axis;
	}

	/**
	 * Sets the y axis label style.
	 * 
	 * @param size
	 *            the size
	 * @param colour
	 *            the label colour
	 */
	public void setYAxisLabelStyle(Integer size, String colour) {
		yaxis_label_style = createLabelStyle(size, colour);
	}

	/**
	 * Sets the right YAxis to this YAxis object
	 * 
	 * @param y_axis_right
	 *            YAxis object
	 */
	public void setYAxisRight(YAxis y_axis_right) {
		this.y_axis_right = y_axis_right;
	}

	/**
	 * Sets the y axis right label style.
	 * 
	 * @param size
	 *            the size
	 * @param colour
	 *            the label colour
	 */
	public void setYAxisRightLabelStyle(Integer size, String colour) {
		yaxisright_label_style = createLabelStyle(size, colour);
	}

	/**
	 * Sets the y legend to this Text object
	 * 
	 * @param y_legend
	 *            Text object
	 */
	public void setYLegend(Text y_legend) {
		this.y_legend = y_legend;
	}

	/**
	 * Sets the y right legend to this Text object
	 * 
	 * @param y2_legend
	 *            Text object
	 */
	public void setYRightLegend(Text y2_legend) {
		this.y2_legend = y2_legend;
	}

	/**
	 * Event listener interface for 'change' events.
	 * 
	 * @deprecated As of OFCGWT 2.0, you should not obtain a JSON string of the
	 *             chart model via toString(). Use
	 *             {@link ChartWidget#setChartData(ChartData)} which will
	 *             process the model and add event handlers correctly.
	 */
	@Deprecated
	public String toString() {
		return buildJSON().toString();
	}

	private String createLabelStyle(Integer size, String colour) {
		String label_style = size.toString();
		if (colour != null && colour.length() > 0) label_style += "," + colour;
		return label_style;
	}
}

