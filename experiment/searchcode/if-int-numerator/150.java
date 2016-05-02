<<<<<<< HEAD
/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
package org.pentaho.di.ui.core.widget;

import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;

import java.lang.reflect.Method;

/**
 * This class defines the fairly generic FormInput.
 * This class is simply a convenience utility, containing
 * the primary information required to build an input 
 * for a FormLayout.  
 *
 * This template requires one to define the type of contained control.
 *
 * ex:
 *      FormInput<Text> input = new FormInput<Text>( new Label( shell, SWT.NONE ),
 *					             new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER) );	
 *	input.setText( "Hello", FormInput.Widget.LABEL );
 *	input.setText( "World", FormInput.Widget.INPUT );
 *	input.setToolTip( "To whom do you want to send a shout out?", FormInput.Widget.INPUT );
 *	input.setPosition( 0, 47, FormInput.Widget.LABEL, FormInput.Position.LEFT );
 *	input.setPosition( 0, 130, FormInput.Widget.LABEL, FormInput.Position.RIGHT );
 *	input.setPosition( input.getLabel( ), 10, FormInput.Widget.INPUT, FormInput.Position.LEFT );
 *
 * @author Robert D. Rice
 */
public class FormInput<C extends Control> extends Object /*implements ConstantsIF*/ {
    public static final String vc_id = "$Id: FormInput.java 1672 2009-05-20 20:12:26Z robert $";

    /** enumeration of available positioning elements */
    public enum Position { LEFT, RIGHT, TOP, BOTTOM }

    /** enumeration of the contained widgets */
    public enum Widget { LABEL, INPUT }

    /** attributes */
    protected Label label = null;
    protected C input = null;
    protected FormData labelFD = new FormData( );
    protected FormData inputFD = new FormData( );

    /**
     * Constructor.
     * @param label
     * @param control input
     */
    public FormInput( Label label, C input ) {
	super( );
	setLabel( label );
	setInput( input );
    }

    /**
     * getter for the label
     * @return label
     */
    public Label getLabel( ) {
	return label;
    }

    /**
     * setter for the label
     * @param label
     */
    public void setLabel( Label label ) {
	this.label = label;
	this.label.setLayoutData( getLabelFD( ) );
    }

    /**
     * getter for the input
     * @return input
     */
    public C getInput( ) {
	return input;
    }

    /**
     * setter for the input
     * @param input
     */
    public void setInput( C input ) {
	this.input = input;
	this.input.setLayoutData( getInputFD( ) );
    }

    /**
     * getter for the labelFD
     * @return labelFD
     */
    public FormData getLabelFD( ) {
	return labelFD;
    }

    /**
     * setter for the labelFD
     * @param labelFD
     */
    public void setLabelFD( FormData labelFD ) {
	this.labelFD = labelFD;
    }

    /**
     * getter for the inputFD
     * @return inputFD
     */
    public FormData getInputFD( ) {
	return inputFD;
    }

    /**
     * setter for the inputFD
     * @param inputFD
     */
    public void setInputFD( FormData inputFD ) {
	this.inputFD = inputFD;
    }

    /**
     * setter for the element position
     * @param numerator
     * @param offset
     * @param widget to set position, [ lable, input ]
     * @param position side, [ left, right, top, bottom ]
     */
    public void setPosition( int numerator, int offset, 
			     Widget widget, Position side ) {
	setPosition( new FormAttachment( numerator, offset ), widget, side );
    }

    /**
     * setter for the element position
     * @param Control
     * @param offset
     * @param widget to set position, [ lable, input ]
     * @param position side, [ left, right, top, bottom ]
     */
    public void setPosition( Control control, int offset, 
			     Widget widget, Position side ) {
	setPosition( new FormAttachment( control, offset ), widget, side );
    }

    /**
     * setter for the element position
     * @param FormAttachment position
     * @param widget to set position, [ lable, input ]
     * @param position side, [ left, right, top, bottom ]
     */
    public void setPosition( FormAttachment position, 
			     Widget widget, Position side ) {
	FormData layout = widget == Widget.LABEL ? getLabelFD( ) : getInputFD( );

	switch (side) {
	case LEFT: layout.left = position; break;
	case RIGHT: layout.right = position; break;
	case TOP: layout.top = position; break;
	case BOTTOM: layout.bottom = position; break;
	default: break;
        }
    }

    /**
     * setter for the widget text
     * @param string text
     * @param widget to set text on
     */
    public void setText( String text, Widget widget ) {
	Control control = widget == Widget.LABEL ? getLabel( ) : getInput( );
	Class<?>[] params = { String.class };

	try {
	    Method method = control.getClass( ).getDeclaredMethod( "setText", params );
	    method.invoke( control, text );
	} catch ( Exception ex ) {
	    ; // oops
	}
    }

    /**
     * getter for the widget text
     * @param widget to retrieve the text from
     * @return string text
     */
    public String getText( Widget widget ) {
	String text = null;
	Control control = widget == Widget.LABEL ? getLabel( ) : getInput( );
	
	try {
	    Method method = control.getClass( ).getDeclaredMethod( "getText" );
	    text = (String)method.invoke( control );
	} catch ( Exception ex ) {
	    ; // oops
	}
	
	return text;
    }
    
    /**
     * setter for the tooltip
     * @param string text
     */
    public void setToolTip( String text, Widget widget ) {
	switch (widget) {
	case LABEL: getLabel( ).setToolTipText( text ); break;
	case INPUT: getInput( ).setToolTipText( text ); break;
	default: break;
	}
    }
} 

=======
/*
 * --------- BEGIN COPYRIGHT NOTICE ---------
 * Copyright 2002-2012 Extentech Inc.
 * Copyright 2013 Infoteria America Corp.
 * 
 * This file is part of OpenXLS.
 * 
 * OpenXLS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * OpenXLS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with OpenXLS.  If not, see
 * <http://www.gnu.org/licenses/>.
 * ---------- END COPYRIGHT NOTICE ----------
 */
package com.extentech.formats.XLS;

import com.extentech.toolkit.ByteTools;
import com.extentech.toolkit.Logger;


/** <b>Scl: Sheet Zoom (A0h)</b><br>

   Scl stores the zoom magnification for the sheet
   
   <p><pre>
    offset  name            size    contents
    ---
    4       num             2       = Numerator of the view magnification fraction (num)  
	6		denum			2		= Denumerator of the view magnification fraction (den)
    </p></pre>
*/

public final class Scl extends com.extentech.formats.XLS.XLSRecord 
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -4595833226859365049L;
//	int num = 100; 20081231 KSC: default val is 1, making the calc (num/denum)*100
	int num= 1;
	int denum = 1;
    
    /** default constructor
    */
    Scl(){
        super();
        byte[] bs = new byte[4];
        bs[0] = 1;
        bs[1] = 0;
        bs[2] = 1;
        bs[3] = 0;
        setOpcode(SCL);
        setLength((short)4);
        if(DEBUGLEVEL > DEBUG_LOW)
        	Logger.logInfo("Scl.init()" + String.valueOf(this.offset));
        this.setData(bs);
        this.originalsize = 4;
    }
    
    /** sets the zoom as a percentage for this sheet
     * 
     * @param b
     */
    public void setZoom(float b){
        byte[] data = this.getData();

/* 20081231 KSC:  appears that zooming is such that 1/1=100%         
        // set our scale to 1000
        denum = 1000;
        byte[] denmbd= ByteTools.shortToLEBytes((short) denum);
        System.arraycopy(denmbd, 0, data, 2, 2);

        // take something like .2345 and come up with 24 & 100
        float nx = b * denum; // get denum
        // get the num
        num = (int)nx;

        if((denum % b)>0){
        	if(b>999) // only 2 precision places for zoom... out a warn
        		Logger.logWarn("Cannot set zoom to : " +b + " rounding to nearest valid zoom setting.");
        }
*/
        // 20081231 KSC: Convert double to fraction and set num/denum to results
        int[] n= gcd((int)(b*100), 100);
        num= n[0];
        denum= n[1];
        byte[] nmbd= ByteTools.shortToLEBytes((short) num);
        System.arraycopy(nmbd, 0, data, 0, 2);
        nmbd= ByteTools.shortToLEBytes((short) denum);
        System.arraycopy(nmbd, 0, data, 2, 2);
        
        this.setData(data);        
    }
    
    /** gets the zoom as a percentage for this sheet
     * 
     * @return
     */
    public float getZoom(){
        return ((float)num/(float)denum);	
    }
    
	public void init(){
        super.init();       
        num = (int) ByteTools.readShort(this.getByteAt(0),this.getByteAt(1));
        denum = (int) ByteTools.readShort(this.getByteAt(2),this.getByteAt(3));        
        if((DEBUGLEVEL > DEBUG_LOW))
        	Logger.logInfo("Scl.init() sheet zoom:" + getZoom());
    }
	
	
	private int[] gcd (int numerator, int denominator)
	{
		int highest;
		int n= 1;
		int d= 1;

		if (denominator>numerator)
			highest=denominator;
		else
			highest=numerator;

		for(int x = highest;x>0;x--)
		{
			if (denominator%x==0 && numerator%x==0)
			{
				n=numerator/x;
				d=denominator/x;
				break;
			}	
		}
		return new int[] { n, d};
	}
}
>>>>>>> 76aa07461566a5976980e6696204781271955163
