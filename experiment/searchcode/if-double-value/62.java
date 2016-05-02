<<<<<<< HEAD
package SongGenerator;

import org.joda.time.DateTime;

/**
 * This is a Value class to hold one of the following data types:Date, Integer, Double, String, Boolean.
 * This class is immutable.
 * @author Alicia Bendz
 *
 */
public class Value {

	/**
	 * Enum to indicate type of value.
	 * @author Alicia Bendz
	 *
	 */
	public enum Type { DATE, INTEGER, DOUBLE, STRING, BOOLEAN };
	
	/**
	 * Type of value.
	 */
	private final Type mType;
	
	/**
	 * The possible values.
	 */
	private final DateTime mDateValue;
	private final Integer mIntegerValue;
	private final Double mDoubleValue;
	private final String mStringValue;
	private final Boolean mBooleanValue;
	
	/**
	 * Default constructor.
	 */
	public Value(){
		mDateValue = null;
		mIntegerValue = null;
		mDoubleValue = null;
		mStringValue = null;
		mType = null;
		mBooleanValue = null;
	}
	
	/**
	 * Date constructor setting all but the Date field to null.
	 * @param date The Date to store.
	 */
	public Value(DateTime date){
		mDateValue = date;
		mIntegerValue = null;
		mDoubleValue = null;
		mStringValue = null;
		mBooleanValue = null;
		mType = Type.DATE;
	}
	
	/**
	 * Integer constructor setting all but the Integer field to null.
	 * @param integer The Integer to store.
	 */
	public Value(Integer integer){
		mDateValue = null;
		mIntegerValue = integer;
		mDoubleValue = null;
		mStringValue = null;
		mBooleanValue = null;
		mType = Type.INTEGER;
	}
	
	/**
	 * Double constructor setting all but the Double field to null.
	 * @param doubl The Double to store.
	 */
	public Value(Double doubl){
		mDateValue = null;
		mIntegerValue = null;
		mDoubleValue = doubl;
		mStringValue = null;
		mBooleanValue = null;
		mType = Type.DOUBLE;
	}
	
	/**
	 * String constructor setting all but the String field to null.
	 * @param string The String to store.
	 */
	public Value(String string){
		mDateValue = null;
		mIntegerValue = null;
		mDoubleValue = null;
		mBooleanValue = null;
		mStringValue = string;
		mType = Type.STRING;
	}
	
	/**
	 * Boolean constructor setting all but the Boolean field to null.
	 * @param bool The Boolean to store.
	 */
	public Value(Boolean bool){
		mDateValue = null;
		mIntegerValue = null;
		mDoubleValue = null;
		mBooleanValue = bool;
		mStringValue = null;
		mType = Type.BOOLEAN;
	}
	
	/**
	 * If the value of this Value is greater than the given value, return true. this.value > v.
	 * This does not apply to Boolean values.
	 * @param v The value to compare to.
	 * @return If the given value is less than this value.
	 */
	public boolean greaterThan(Value v){
		
		//determine type of current value and if the type of the given value is the same, 
		//compare them
		if(mDateValue != null && v.isType(Type.DATE)){
			
			//a date is greater than another if it happens after the other
			if(mDateValue.isAfter(v.getDate()))
				return true;
		} else if (mIntegerValue != null && v.isType(Type.INTEGER)){
			if(mIntegerValue.intValue() > v.getInteger().intValue())
				return true;
		} else if (mDoubleValue != null && v.isType(Type.DOUBLE)){
			if(mDoubleValue.doubleValue() > v.getDouble().doubleValue())
				return true;
			
		} else if (mStringValue != null && v.isType(Type.STRING)){
			//Lexicographical comparison
			if(mStringValue.compareTo(v.getString()) > 0)
				return true;
		}
		
		return false;
	}
	
	/**
	 * If the value of this Value is less than the given value, return true. this.value < v.
	 * This does not apply to boolean values.
	 * @param v The value to compare to.
	 * @return If the given value is greater than this value.
	 */
	public boolean lessThan(Value v){

		//determine type of current value and if the type of the given value is the same, 
		//compare them
		if(mDateValue != null && v.isType(Type.DATE)){
			
			//a date is less than another if it happens before the other
			if(mDateValue.isBefore(v.getDate()))
				return true;
		} else if (mIntegerValue != null && v.isType(Type.INTEGER)){
			if(mIntegerValue.intValue() < v.getInteger().intValue())
				return true;
		} else if (mDoubleValue != null && v.isType(Type.DOUBLE)){
			if(mDoubleValue.doubleValue() < v.getDouble().doubleValue())
				return true;
			
		} else if (mStringValue != null && v.isType(Type.STRING)){
			//Lexicographical comparison
			if(mStringValue.compareTo(v.getString()) < 0)
				return true;
		}
		
		return false;
	}
	
	/**
	 * This method checks if two values are equal. Two values are equal if their stored 
	 * values are equal.
	 * This may not be useful for Dates and Doubles.
	 * @param v The value to check against.
	 * @return This returns true if the stored value in both Values is the same.
	 */
	public boolean equal(Value v){
		
		//determine type of current value and if the type of the given value is the same, 
		//compare them
		if(mDateValue != null && v.isType(Type.DATE)){
			if(mDateValue.isEqual((v.getDate())))
					return true;
		} else if (mIntegerValue != null && v.isType(Type.INTEGER)){
				if(mIntegerValue.intValue() == v.getInteger().intValue())
						return true;
		} else if (mDoubleValue != null && v.isType(Type.DOUBLE)){
			if(mDoubleValue.doubleValue() == v.getDouble().doubleValue())
					return true;
					
		} else if (mStringValue != null && v.isType(Type.STRING)){
			//Lexicographical comparison
			return mStringValue.equals(v.getString());
		} else if (mBooleanValue != null && v.isType(Type.BOOLEAN)){
			return (mBooleanValue && v.getBoolean()) || (!mBooleanValue && !v.getBoolean());
		}
				return false;
	}
	
	/**
	 * This method determines if this value contains the given value. This can only be 
	 * applied to Strings.
	 * @param v The value to check against.
	 * @return If this value contains v, then return true.
	 */
	public boolean contains(Value v){
		//this only applies to strings
		if (mStringValue != null && v.isType(Type.STRING)){
			//Lexicographical comparison
			return mStringValue.contains(v.getString());
		}
				
		return false;
	}
	
	/**
	 * Check if the type of the Value matches a given type.
	 * @param t The given type
	 * @return
	 */
	public boolean isType(Type t){
		return t == mType;
	}
	
	/**
	 * Getter for the DateTime value of the Value. Returns null if there isn't one.
	 * @return The DateTime or null.
	 */
	public DateTime getDate(){
		return mDateValue;
	}
	
	/**
	 * Getter for the Integer value of the Value. Returns null if there isn't one.
	 * @return The Integer or null.
	 */
	public Integer getInteger(){
		return mIntegerValue;
	}
	
	/**
	 * Getter for the Double value of the Value. Returns null if there isn't one.
	 * @return The Double or null.
	 */
	public Double getDouble(){
		return mDoubleValue;
	}
	
	/**
	 * Get the String value of the Value. Returns null if there isn't one.
	 * @return The String or null.
	 */
	public String getString(){
		return mStringValue;
	}
	
	/**
	 * Get the Boolean value of this Value. Returns null if there isn't one.
	 * @return The Boolean value or null.
	 */
	public Boolean getBoolean(){
		return mBooleanValue;
	}
	
	/**
	 * A method that will compare two values according to a given operator. Evaluates the 
	 * expression: v [operator] thisValue.
	 * If check(new Value(3), GREATER) is called on a Value(2), it will return true since 3 > 2.
	 * Ignores the maximize and minimize operators.
	 * @param v The value to compare to.
	 * @param o The operator of comparison.
	 * @return True if the expression is true, false if not.
	 */
	public boolean check(Value v, Operator o){
		switch (o){
		case EQUALS:
			return equal(v);
		case GREATER:
			return v.greaterThan(this);
		case LESS:
			return v.lessThan(this);
		case GREATER_EQUAL:
			return equal(v) || v.greaterThan(this);
		case LESS_EQUAL:
			return equal(v)|| v.lessThan(this);
		case NOT_EQUAL:
			return !equal(v);
		case CONTAINS:
			return v.contains(this);
		case NOT_CONTAINS:
			return !v.contains(this);
		default:
			return false;
		}
	}

=======
/***********************************************************************************************
 * File: DataAxis - derived from YAxis.java
 * Last Modified: $Id: DataAxis.java,v 1.4 2003/03/09 22:42:10 nathaniel_auvil Exp $
 * Copyright (C) 2000
 * Author: John Thomsen
 * Contributor(s):
 *
 * Copyright 2002 (C) Nathaniel G. Auvil. All Rights Reserved.
 *
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "jCharts" or "Nathaniel G. Auvil" must not be used to
 * 	  endorse or promote products derived from this Software without
 * 	  prior written permission of Nathaniel G. Auvil.  For written
 *    permission, please contact nathaniel_auvil@users.sourceforge.net
 *
 * 4. Products derived from this Software may not be called "jCharts"
 *    nor may "jCharts" appear in their names without prior written
 *    permission of Nathaniel G. Auvil. jCharts is a registered
 *    trademark of Nathaniel G. Auvil.
 *
 * 5. Due credit should be given to the jCharts Project
 *    (http://jcharts.sourceforge.net/).
 *
 * THIS SOFTWARE IS PROVIDED BY Nathaniel G. Auvil AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * jCharts OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 ************************************************************************************************/

package org.jCharts.axis;


import org.jCharts.chartData.IDataSeries;
import org.jCharts.chartData.processors.AxisChartDataProcessor;
import org.jCharts.AxisChart;


import java.awt.*;


/****
 *
 * @deprecated just using the YAxis Object
 */
public class DataAxis extends Axis
{
	//---these values are not the same as the data set min and max;
	//---these are what is displayed on screen which includes padding.
	private double minValue;
	private double maxValue;

	//---Difference between the points on the axis.
	//---ie-> if 10 and the origin was 0, the values would be 0,10,20,30,etc...
	private double increment;


	//---multiplication value used to determine the coordinate location of values on YAxis
	//private double oneUnitPixelSize;

	//---not always equal to the origin as charts may not start at zero.
	protected float zeroLineCoordinate;


	/*****
	 *
	 * @param axisChart
	 * @deprecated this class is no longer used
	 */
	public DataAxis( AxisChart axisChart )
	{
		super( axisChart, 0 );
	}


	protected boolean amDataAxis()
	{
		return true;
	}


	protected boolean amLabelAxis()
	{
		return false;
	}


	/*************************************************************************************************
	 * Add all text labels to be display on this axis.
	 *
	 **************************************************************************************************/
	public void addTextTags()
	{
/*
		NumberFormat numberFormat;
		AxisProperties axisProperties = super.getAxisChart().getAxisProperties();

		Font font = axisProperties.getScaleFont();
		Font derivedFont = null;

		if( super.getVerticalScaleFlag() )
		{
			derivedFont = font.deriveFont( Axis.VERTICAL_LABEL_ROTATION );
		}

		this.textTagGroup = new TextTagGroup(
		   font,
		   derivedFont,
		   axisProperties.getScaleFontColor(),
		   super.getAxisChart().getGraphics2D().getFontRenderContext() );

		super.setNumberOfLabelsOnAxis( super.getAxisChart().getAxisProperties().getDataAxisNumItems() );

		if( showText() == false ) return;

		double value = this.minValue;

		//---DOLLAR SIGNS
		if( axisProperties.getDataAxisUseDollarSigns() )
		{
			numberFormat = NumberFormat.getCurrencyInstance();
		}
		else
		{
			numberFormat = NumberFormat.getInstance();
		}

		//---COMMAS
		if( axisProperties.getDataAxisUseCommas() )
		{
			numberFormat.setGroupingUsed( true );
		}
		else
		{
			numberFormat.setGroupingUsed( false );
		}

		//---TRIM OFF DECIMAL PLACES IF ROUND TO WHOLE NUMBER
		if( axisProperties.getDataAxisRoundValuesToNearest() >= 0 )
		{
			numberFormat.setMaximumFractionDigits( 0 );
			numberFormat.setMinimumFractionDigits( 0 );
		}
		else
		{
			numberFormat.setMaximumFractionDigits( -axisProperties.getDataAxisRoundValuesToNearest() );
			numberFormat.setMinimumFractionDigits( -axisProperties.getDataAxisRoundValuesToNearest() );
		}

		//LOOP
		for( int i = 0; i <= super.getAxisChart().getAxisProperties().getDataAxisNumItems(); i++ )
		{
			textTagGroup.addTextTag( numberFormat.format( value ) );

			value += this.increment;
		}

		super.setWidestLabel( textTagGroup.getWidestTextTag() );
		super.setTallestLabel( textTagGroup.getTallestTextTag() );
*/

	}


	/*******************************************************************************************
	 * Calculates the axis scale increment.
	 *
	 * If the user does not specify a scale, it is auto computed in the followin way:
	 *  <LI>if all values are positive, the MIN value will be zero.</LI>
	 *  <LI>if all values are negative, the MAX value will be zero.</LI>
	 *  <LI>Padding is done by either adding or subtracting the increment by the rounding power of ten
	 *  specified in the properties.</LI>
	 *
	 * @param axisChartDataProcessor need to get the min/max
	 ********************************************************************************************/
	void computeScaleIncrement( AxisChartDataProcessor axisChartDataProcessor )
	{
/*
		AxisProperties axisProperties = super.getAxisChart().getAxisProperties();

		int numScaleItems = axisProperties.getDataAxisNumItems();
		double powerOfTen = Math.pow( 10.0d, Math.abs( (double) super.getAxisChart().getAxisProperties().getDataAxisRoundValuesToNearest() ) );

		if( axisProperties.hasUserDefinedScale() )
		{
			this.increment = this.round( axisProperties.getUserDefinedDataAxisIncrement(), powerOfTen );

			//---if we round this down to zero, force it to the power of ten.
			//---for example, round to nearest 100, value = 35...would push down to 0 which is illegal.
			if( this.increment == 0 )
			{
				this.increment = powerOfTen;
			}

			this.minValue = this.round( axisProperties.getUserDefinedDataAxisMinimum(), powerOfTen );
			this.maxValue = this.minValue + ( this.increment * numScaleItems );
		}
		//---else, we will determine the axis scale to use
		else
		{


			double range;

			//---if MIN >= 0, MAX is the range, if MAX < 0, -MIN is the range
			if( ( axisChartDataProcessor.getMinValue() >= 0 ) || ( axisChartDataProcessor.getMaxValue() < 0 ) )
			{
				range = Math.max( axisChartDataProcessor.getMaxValue(), -axisChartDataProcessor.getMinValue() );

				this.increment = range / numScaleItems;
				this.roundTheIncrement( powerOfTen );

				if( axisChartDataProcessor.getMinValue() >= 0 )
				{
					this.minValue = 0.0d;
					this.maxValue = this.increment * numScaleItems;
				}
				else
				{
					this.maxValue = 0.0d;
					this.minValue = -( this.increment * numScaleItems );
				}

			// data is the double[][] with the chart values.  getMax just finds the largest point anywhere in the array.
			double yMax = this.maxValue;
			double yMin = this.minValue;

			// In the following line, note that Math.log is actually Natural Logarithm.
			// log base a of b = ln b / ln a => log base 10 of x = ln 10 / ln x
			//double yDelta = Math.pow( 10.0, Math.round( Math.log( yMax - yMin ) / Math.log( 10 ) ) );
			double yDelta = Math.pow( 10.0, Math.round( Math.log( range ) / Math.log( 10 ) ) );
			double yStart = yMin - ( yMin % yDelta );
			double yEnd = yMax - ( yMax % yDelta ) + yDelta;

			// Count the number of segments this gives us.  Shoot for 20 segments or so.
			int segments = (int) ( ( yEnd - yStart ) / yDelta );
			if( segments <= 2 )
			{
				// we need 10 times this many
				yDelta = yDelta / 10.0;
			}
			else if( segments <= 5 )
			{
				// we need 4 times this many
				yDelta = yDelta / 4.0;
			}
			else if( segments <= 10 )
			{
				yDelta = yDelta / 2.0;
			}
			// Recalc start and end to match with new delta.
			yStart = yMin - ( yMin % yDelta );
			yEnd = yMax - ( yMax % yDelta ) + yDelta;
			segments = (int) ( ( yEnd - yStart ) / yDelta );

			//axisProperties = new AxisProperties(yStart, yDelta);
			//axisProperties.setYAxisNumItems(segments);

			this.increment= yDelta;


			}
			//---else MIN is negative and MAX is positive, so add values together (minus a negative is a positive)
			else
			{
				this.minValue = this.round( axisChartDataProcessor.getMinValue(), powerOfTen );

				//---round min value down to get the start value for axis.  Compute range from this value.
				if( super.getAxisChart().getAxisProperties().getDataAxisRoundValuesToNearest() > 0 )
				{
					this.minValue -= powerOfTen;
				}
				else
				{
					this.minValue -= ( 1 / powerOfTen );
				}

				//---we want the rounded Axis min for range
				//---MIN is always negative at this point so minus a negative is a positive
				range = axisChartDataProcessor.getMaxValue() - this.minValue;

				this.increment = range / numScaleItems;
				this.roundTheIncrement( powerOfTen );

				//---axis starts at minValue, not zero!
				this.maxValue = this.minValue + ( this.increment * numScaleItems );
			}

		}
*/
	}


	/***********************************************************************************************
	 * Rounds the scale increment up by the power of ten specified in the properties.
	 *
	 * @param value the value to round
	 * @param powerOfTen the product of 10 times the rounding property.
	 * @return double the rounded result
	 ************************************************************************************************/
	private double round( double value, double powerOfTen )
	{
/*
		if( super.getAxisChart().getAxisProperties().getDataAxisRoundValuesToNearest() > 0 )
		{
			return ( Math.round( value / powerOfTen ) * powerOfTen );
		}
		else if( super.getAxisChart().getAxisProperties().getDataAxisRoundValuesToNearest() < 0 )
		{
			return ( Math.round( value * powerOfTen ) / powerOfTen );
		}
		else
		{
			return ( Math.round( value ) );
		}
*/
		return 0;
	}


	/***********************************************************************************************
	 * Rounds the scale increment up by the power of ten specified in the properties.
	 *
	 * @param powerOfTen the value of 10 times the rounding property.
	 ************************************************************************************************/
	private void roundTheIncrement( double powerOfTen )
	{
/*
		this.increment = this.round( this.increment, powerOfTen );

		//---round the increment up
		if( super.getAxisChart().getAxisProperties().getDataAxisRoundValuesToNearest() > 0 )
		{
			this.increment += powerOfTen;
		}
		else
		{
			this.increment += ( 1 / powerOfTen );
		}
*/
	}


	/**************************************************************************************************
	 * Returns the screen coordinate of the zero line.  This will not always be the same as the origin
	 *  as not all charts start at zero.
	 *
	 * @return float the screen pixel location of the zero line.
	 ***************************************************************************************************/
	public float getZeroLineCoordinate()
	{
		return this.zeroLineCoordinate;
	}


	/*************************************************************************************************
	 * Takes a value and determines the screen coordinate it should be drawn at.
	 *
	 * @param value
	 * @return float the screen pixel coordinate
	 **************************************************************************************************/
	float computeAxisCoordinate( double value )
	{
/*
		double returnValue;

		if( amHorizontal() == false )
		{
			returnValue = super.getOrigin() - ( value - this.getMinValue() ) * this.getOneUnitPixelSize();

		}
		else
		{
			returnValue = super.getOrigin() + ( value - this.getMinValue() ) * this.getOneUnitPixelSize();
		}
*/

		/* -- Debug for various settings..
		System.out.println("Pix:"+this.getOneUnitPixelSize());
		System.out.println("origin:"+super.getOrigin());
		System.out.println("Ret"+returnValue);
		*/

//		return (float) returnValue;

		return 0;
	}


	/**************************************************************************************************
	 * Returns the MAX value plotted by the axis.
	 *
	 * @return double the MAX value plotted by the axis
	 ***************************************************************************************************/
	public double getMaxValue()
	{
		return this.maxValue;
	}


	/**************************************************************************************************
	 * Returns the MIN value plotted by the axis.
	 *
	 * @return double the MIN value plotted by the axis
	 ***************************************************************************************************/
	public double getMinValue()
	{
		return this.minValue;
	}


	/**************************************************************************************************
	 * Returns the number of pixels one value unit occupies.
	 *
	 * @return double the number of pixels one value unit occupies.
	 ***************************************************************************************************/
	public double getOneUnitPixelSize()
	{
		/* -- Debug for various settings
		System.out.println("inc:"+this.increment);
		System.out.println("Scale:"+this.getScalePixelLength());
		*/
		//return ( super.getScalePixelLength() / this.increment );

		return 0;
		// return this.oneUnitPixelSize; // Old code
	}


	/*********************************************************************************************
	 * Renders the DataAxis on the passes Graphics2D object
	 *
	 * @param graphics2D
	 * @param iDataSeries
	 **********************************************************************************************/
	protected void render( Graphics2D graphics2D,
	                       IDataSeries iDataSeries )
	{
/*
		super.render( graphics2D, iDataSeries );

		Line2D.Float line2D = new Line2D.Float();

		if( amHorizontal() )
		{
			line2D = super.getAxisChart().getVerticalAxis().getAxisLine( line2D );
		}
		else
		{
			line2D = super.getAxisChart().getHorizontalAxis().getAxisLine( line2D );
		}

		float offset = super.getScalePixelLength();
		offset *= (float) -this.minValue;
		offset /= (float) this.increment;

		offset = ( amHorizontal() ) ? offset : -offset;

		float start = ( amHorizontal() ) ? line2D.x1 : line2D.y1;

		//---need this regardless if draw line or not.
		this.zeroLineCoordinate = start + offset;

		// System.out.println("Zero Line:"+zeroLineCoordinate);


		//---ZERO LINE
		if( super.getAxisChart().getAxisProperties().getShowZeroLine() &&
		   this.minValue < 0.0d &&
		   this.maxValue > 0.0d )
		{
			if( amHorizontal() )
			{
				line2D.x1 = this.zeroLineCoordinate;
				line2D.x2 = line2D.x1;
			}
			else
			{
				line2D.y1 = this.zeroLineCoordinate;
				line2D.y2 = line2D.y1;
			}

			graphics2D.setStroke( super.getAxisChart().getAxisProperties().getZeroLineStroke() );
			graphics2D.setPaint( super.getAxisChart().getAxisProperties().getZeroLinePaint() );
			graphics2D.draw( line2D );
		}
*/
	}
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

