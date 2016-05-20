/**
 * Copyright (C) 2010 Alfredo Morresi
 * 
 * This file is part of SmsForFree project.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/>.
 */

package it.rainbowbreeze.smsforfree.domain;

/**
 * 
 * 
 * @author Alfredo "Rainbowbreeze" Morresi
 *
 */
public class SmsServiceParameter
{
	//---------- Ctors
	public SmsServiceParameter() {
		this("", "", FORMAT_NONE);
	}
	
	public SmsServiceParameter(String desc, String value, int format)
	{
		mDesc = desc;
		mValue = value;
		mFormat = format;
	}

	
	
	
	//---------- Public fields
	public final static int FORMAT_NONE = 0;
	public final static int FORMAT_PASSWORD = 1;
	public final static int FORMAT_OPTIONAL = 2;
	

	
	
	//---------- Public properties
	/** Description of the field */
	private String mDesc;
	public String getDesc()
	{ return mDesc; }
	public void setDesc(String newValue)
	{ mDesc = newValue; }
	
	/** Value of the field */
	private String mValue;
	public String getValue()
	{ return mValue; }
	public void setValue(String newValue)
	{ mValue = newValue; }
	
	/** Metainformation about the field (password, allowed chars) */
	private int mFormat;
	public int getFormat()
	{ return mFormat; }
	public void setFormat(int newValue)
	{ mFormat = newValue; }

	
	

	//---------- Public methods
	
	public boolean isPassword()
	{ return (mFormat & FORMAT_PASSWORD) == FORMAT_PASSWORD; }

	public boolean isOptional()
	{ return (mFormat & FORMAT_OPTIONAL) == FORMAT_OPTIONAL; }

	
	
	
	//---------- Private methods

	
	
	
}

