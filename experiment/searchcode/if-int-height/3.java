/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.byu.cs340.aabp.hypeerweb;

/**
 *
 * @author rbirdman
 */
public class Height 
{// extends Integer {
	int height;
	
	public Height() 
	{
		this(0);
	}
	
	public Height(int height) 
	{
		this.height = height;
	}
	
	public void incr() 
	{
		height++;
	}
	
	public void incrBy(int incrVal) 
	{
		height += incrVal;
	}
	
	//cannot have a value less than 0
	public void decr() 
	{
		if(height > 0)
		{
			height--;
		}
	}
	
	public void decrBy(int decrVal) {
		if(height >= decrVal)
		{
			height -= decrVal;
		}
			
	}
	
	public void setHeight(int val) 
	{
		height = val;
	}
	
	public boolean equals(Height other) 
	{
		return equals(other.height);
	}
	
	public boolean equals(int other) 
	{
		return height == other;
	}
	
	public boolean equals(Integer other) 
	{
		return height == other;
	}

	@Override
	public int hashCode() 
	{
		int hash = 7;
		hash = 73 * hash + this.height;
		return hash;
	}
	
	@Override
	public boolean equals(Object other) 
	{
		if(other instanceof Height)
		{
			return equals((Height) other);
		}
		else
		{
			return equals((Integer) other);
		}
	}
	
	@Override
	public String toString() 
	{
		return "" + height;
	}
	
	
}

