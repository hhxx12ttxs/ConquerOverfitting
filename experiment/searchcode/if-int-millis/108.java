package visual.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Arrays;

import javax.swing.JComponent;

import util.ThreadUtils;

import colors.JSortingLineColors;

public class JSortingLine extends JComponent
{
	private volatile int length;
	private final int thickness;
	private static int compareDelayNanos = 0;
	private static int swapDelayNanos = 0;

	private boolean sorted;
	public final JSortingLineColors colors = new JSortingLineColors();

	public static final int MAX_LENGTH = Integer.MAX_VALUE;

	public int length()
	{
		return this.length;
	}

	public synchronized void setLength(int length)
	{
		this.length = length;
		repaint();
	}

	public JSortingLine(int length, int thickness)
	{
		if(length < 0)
			throw new IllegalArgumentException("Illegal length " + thickness);
		this.length = length;
		if(thickness < 0)
			throw new IllegalArgumentException("Illegal thickness " + thickness);
		this.thickness = thickness;
		this.setMaximumSize(new Dimension(MAX_LENGTH, thickness));

		setForeground(colors.getUnSorted());
		this.sorted = false;
	}

	public static void setCompareDelay(double millis)
	{
		if(millis < 0)
			throw new IllegalArgumentException("delay " + millis + " is negative");
		compareDelayNanos = (int)(millis * 1000000);
	}

	public static void setSwapDelay(double millis)
	{
		if(millis < 0)
			throw new IllegalArgumentException("delay " + millis + " is negative");
		
		swapDelayNanos = (int)(millis * 1000000);
	}

	@Override
	public void paintComponent(Graphics g)
	{
		Color foreground = getForeground();
		if(g.getColor() != foreground)
			g.setColor(foreground);

		if(thickness == 1)
			g.drawLine(0, 0, length, 0);
		else
			g.fillRect(0, 0, length, thickness);
	}

	public synchronized void markSorted(boolean sorted)
	{
		if(sorted != this.sorted)
		{
			this.sorted = sorted;
			setForeground(colors.getSorted());
		}
	}

	/**
	 * NOT FOR EXTERNAL USE?!.
	 */
	@Override
	@Deprecated
	public void setForeground(Color c)
	{
		super.setForeground(c);
	}

	@Override
	public String toString()
	{
		char[] buf = new char[length];
		Arrays.fill(buf, '_');
		return String.valueOf(buf);
	}

	private static final long serialVersionUID = 4797134840445937813L;
	
	public synchronized int compareTo(JSortingLine other) throws InterruptedException
	{
		if(compareDelayNanos > 0.0)
		{
			Color oldthis = this.getForeground();
			Color oldother = other.getForeground();

			this.setForeground(colors.getPrimaryCompare());
			other.setForeground(other.colors.getSecondaryCompare());

			sleep(compareDelayNanos);
			
			this.setForeground(oldthis);
			other.setForeground(oldother);
		}
		
		return this.length - other.length;
	}

	private void sleep(long nanos) throws InterruptedException
	{
		ThreadUtils.sleepNanos(nanos);
	}

	public synchronized void swap(JSortingLine other) throws InterruptedException
	{
		if(swapDelayNanos > 0.0)
		{
			sleep(swapDelayNanos);
		}
		
		boolean sortedBefore = this.sorted;
		this.markSorted(other.sorted);
		other.markSorted(sortedBefore);

		int lengthBefore = this.length();
		this.setLength(other.length());
		other.setLength(lengthBefore);
	}
}

