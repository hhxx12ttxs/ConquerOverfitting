package stepmania.timing;

import stepmania.enums.timing.SegmentEffectType;
import stepmania.enums.timing.TimingSegmentType;

public class ScrollSegment extends TimingSegment
{
	private double ratio;
	
	
	public ScrollSegment()
	{
		this(TimingSegment.ROW_INVALID);
	}
	public ScrollSegment(int startRow)
	{
		this(startRow, 1);
	}
	public ScrollSegment(int startRow, double r)
	{
		super(startRow);
		this.setRatio(r);
	}
	
	public ScrollSegment(double beat)
	{
		this(beat, 1);
	}
	public ScrollSegment(double beat, double r)
	{
		super(beat);
		this.setRatio(r);
	}
	public ScrollSegment(ScrollSegment other)
	{
		super(other);
		this.setRatio(other.getRatio());
	}
	
	
	@Override
	public boolean IsNotable()
	{
		return true;
	}
	
	@Override
	public TimingSegmentType GetType()
	{
		return TimingSegmentType.SEGMENT_SCROLL;
	}
	
	
	@Override
	public SegmentEffectType GetEffectType()
	{
		return SegmentEffectType.EFFECT_INDEFINITE;
	}
	
	/**
	 * @return the ratio
	 */
	public double getRatio()
	{
		return ratio;
	}
	/**
	 * @param ratio the ratio to set
	 */
	public void setRatio(double ratio)
	{
		this.ratio = ratio;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		long temp;
		temp = Double.doubleToLongBits(ratio);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		ScrollSegment other = (ScrollSegment) obj;
		if (Double.doubleToLongBits(ratio) != Double
				.doubleToLongBits(other.ratio))
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
		return "ScrollSegment [row=" + this.getStartRow() +
				", ratio=" + ratio + "]";
	}
	
	
}

