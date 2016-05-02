package stepmania.timing;

import stepmania.enums.timing.SegmentEffectType;
import stepmania.enums.timing.TimingSegmentType;

public class TimeSignatureSegment extends TimingSegment
{
	private int numerator;
	private int denominator;
	
	public TimeSignatureSegment()
	{
		this(TimingSegment.ROW_INVALID);
	}
	public TimeSignatureSegment(int startRow)
	{
		this(startRow, 4);
	}
	public TimeSignatureSegment(int startRow, int n)
	{
		this(startRow, n, 4);
	}
	
	public TimeSignatureSegment(int startRow, int n, int d)
	{
		super(startRow);
		this.setNum(n);
		this.setDen(d);
	}
	public TimeSignatureSegment(double beat)
	{
		this(beat, 4);
	}
	public TimeSignatureSegment(double beat, int n)
	{
		this(beat, n, 4);
	}
	public TimeSignatureSegment(double beat, int n, int d)
	{
		super(beat);
		this.setNum(n);
		this.setDen(d);
	}
	public TimeSignatureSegment(TimeSignatureSegment other)
	{
		super(other);
		this.setNum(other.getNum());
		this.setDen(other.getDen());
	}
	
	
	@Override
	public boolean IsNotable()
	{
		return true;
	}
	
	@Override
	public TimingSegmentType GetType()
	{
		return TimingSegmentType.SEGMENT_TIME_SIG;
	}
	
	public SegmentEffectType GetEffectType()
	{
		return SegmentEffectType.EFFECT_INDEFINITE;
	}
	
	/**
	 * @return the numerator
	 */
	public int getNum()
	{
		return numerator;
	}
	/**
	 * @param numerator the numerator to set
	 */
	public void setNum(int numerator)
	{
		this.numerator = numerator;
	}
	/**
	 * @return the denominator
	 */
	public int getDen()
	{
		return denominator;
	}
	/**
	 * @param denominator the denominator to set
	 */
	public void setDen(int denominator)
	{
		this.denominator = denominator;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + denominator;
		result = prime * result + numerator;
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (!super.equals(obj))
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		TimeSignatureSegment other = (TimeSignatureSegment) obj;
		if (denominator != other.denominator)
		{
			return false;
		}
		if (numerator != other.numerator)
		{
			return false;
		}
		return true;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "TimeSignatureSegment [row=" + this.getStartRow() +
				", numerator=" + numerator
				+ ", denominator=" + denominator + "]";
	}
	
}

