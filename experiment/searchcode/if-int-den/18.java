//---------------------------------------------------------------------------------
//
// BlackBerry Extensions
// Copyright (c) 2008-2012 Vincent Simonetti
//
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
// documentation files (the "Software"), to deal in the Software without restriction, including without limitation 
// the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
// to permit persons to whom the Software is furnished to do so, subject to the following conditions:
// 
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
// 
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
// INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
// PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE 
// LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
// OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
// DEALINGS IN THE SOFTWARE.
//
//---------------------------------------------------------------------------------
//
// Created 2009
package rebuild.graphics.tiff.tags;

import java.io.IOException;

import rebuild.BBXResource;
import rebuild.Resources;
import rebuild.graphics.tiff.Tag;
import rebuild.graphics.tiff.Writer;

/**
 * A tag that contains one or more {@link Writer.RATIONAL} items.
 * @since BBX 1.0.1
 */
public class RationalTag extends Tag
{
	/**
	 * Create a new {@link RationalTag}.
	 * @param tag The tag value.
	 */
	public RationalTag(short tag)
	{
		this(tag, 0, 1);
	}
	
	/**
	 * Create a new {@link RationalTag}.
	 * @param tag The tag value.
	 * @param num The numerator for the {@link Writer.RATIONAL}.
	 * @param den The denominator for the {@link Writer.RATIONAL}.
	 */
	public RationalTag(short tag, int num, int den)
	{
		this(tag, new int[]{ num }, new int[]{ den });
	}
	
	/**
	 * Create a new {@link RationalTag}.
	 * @param tag The tag value.
	 * @param num The numerators for the {@link Writer.RATIONAL}.
	 * @param den The denominators for the {@link Writer.RATIONAL}.
	 */
	public RationalTag(short tag, int[] num, int[] den)
	{
		this(tag, num, den, Writer.RATIONAL);
	}
	
	/**
	 * Create a new {@link RationalTag}.
	 * @param tag The tag value.
	 * @param num The numerators for the {@link Writer.RATIONAL}.
	 * @param den The denominators for the {@link Writer.RATIONAL}.
	 */
	protected RationalTag(short tag, int[] num, int[] den, short type)
	{
		super(tag, type, num.length, 0, new int[][]{ num, den});
		if(den == null)
		{
			//We don't need to check num because if it is null, we will get the exception from the super's constructor
			throw new NullPointerException("den");
		}
		if(num.length == 0 || den.length == 0)
		{
			throw new NullPointerException((num.length == 0 && den.length == 0 ? "(num, den)" : num.length == 0 ? "num" : "den") + ".length == 0");
		}
		if(num.length != den.length)
		{
			throw new IllegalArgumentException(Resources.getString(BBXResource.TIFF_DIFFERENT_RATIONAL));
		}
	}
	
	/**
	 * Data type is a {@link Writer.RATIONAL}.
	 * @see setDataType
	 */
	public final void setDataType(short dataType)
	{
	}
	
	/**
	 * Set the number of {@link Writer.RATIONAL} contained in this tag.
	 * @param count The number of {@link Writer.RATIONAL} contained in the tag. If the number is bigger than the current count then it will add 0/1 {@link Writer.RATIONAL}, else it will remove the {@link Writer.RATIONAL}.
	 * @see getCount
	 */
	public void setCount(int count)
	{
		if(count <= 0)
		{
			throw new IllegalArgumentException("count <= 0");
		}
		if(count != super.count)
		{
			int[][] dat = (int[][])super.getExtraData();
			int[] num = null;
			int[] den = null;
			if(count > super.count)
			{
				num = new int[count];
				den = new int[count];
				int ccount = (int)super.count;
				System.arraycopy(dat[0], 0, num, 0, ccount);
				System.arraycopy(dat[1], 0, den, 0, ccount);
				for(int i = ccount + 1; i < count; i++)
				{
					den[i] = 1;
				}
			}
			else
			{
				num = net.rim.device.api.util.Arrays.copy(dat[0], 0, count);
				den = net.rim.device.api.util.Arrays.copy(dat[1], 0, count);
			}
			dat[0] = num;
			dat[1] = den;
			super.count = count;
		}
	}
	
	/**
	 * Unused.
	 * @see getExtraData
	 */
	protected final void setExtraData(Object obj)
	{
	}
	
	/**
	 * Get the denominator at the specified index.
	 * @param index The index to get the denominator.
	 * @return The denominator at the specified index.
	 */
	public final int getDenominator(int index)
	{
		return ((int[][])super.getExtraData())[1][index];
	}
	
	/**
	 * Set the denominator at the specified index.
	 * @param index The index to set the denominator.
	 * @param den The denominator to set at the specified index.
	 */
	public final void setDenominator(int index, int den)
	{
		((int[][])super.getExtraData())[1][index] = den;
	}
	
	/**
	 * Get the numerator at the specified index.
	 * @param index The index to get the numerator.
	 * @return The numerator at the specified index.
	 */
	public final int getNumerator(int index)
	{
		return ((int[][])super.getExtraData())[0][index];
	}
	
	/**
	 * Set the numerator at the specified index.
	 * @param index The index to set the numerator.
	 * @param num The numerator to set at the specified index.
	 */
	public final void setNumerator(int index, int num)
	{
		((int[][])super.getExtraData())[0][index] = num;
	}
	
	/**
	 * This method figures out if any extra data is exists, this method can search the data type, extra data, value, and count. It is called whenever one of these types change.
	 * @param big <code>true</code> if the tag is being written to a BigTIFF, <code>false</code> if otherwise.
	 * @return true if the tag contains extra data, false if otherwise.
	 */
	protected boolean extraData(boolean big)
	{
		if(big)
		{
			return super.count > 1L;
		}
		else
		{
			return true;
		}
	}

	/**
	 * Setup the tag for writing in a BigTIFF.
	 */
	protected void setupForBigTIFF(boolean set)
	{
		if(set && super.count == 1L)
		{
			int[][] dat = (int[][])super.getExtraData();
			this.value = Writer.shiftValue(dat[0][0], 0) | Writer.shiftValue(dat[1][0], 1);
		}
		else
		{
			this.value = 0L;
		}
	}

	/**
	 * The extra data writer, if any extra data exists then this method will be called and (through internal methods) a offset to the data will be used in place of the <code>value</code> parameter.
	 * @param wr The writer used to write the extra data.
	 * @throws IOException If any IO exception occurs.
	 */
	public void writeData(Writer wr) throws IOException
	{
		int[][] dat = (int[][])super.getExtraData();
		for(int i = 0; i < super.count; i++)
		{
			wr.writeUInt(dat[0][i]);
			wr.writeUInt(dat[1][i]);
		}
	}
	
	/**
	 * Get if this {@link Tag} can have a default value, often defined by a parameter-less constructor.
	 * @return <code>true</code> if the {@link Tag} has a default value, <code>false</code> if otherwise. Default is <code>false</code>.
	 */
	public boolean hasDefault()
	{
		return false;
	}
	
	/**
	 * Get if this {@link Tag}'s current value is the default value. This is ignored if {@link Tag#hasDefault()} returns <code>false</code>.
	 * @return <code>true</code> if the {@link Tag}'s value is the default value, <code>false</code> if otherwise.
	 */
	public boolean isDefault()
	{
		return false;
	}
}

