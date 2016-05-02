package stepmania.timing;

import stepmania.enums.timing.SegmentEffectType;
import stepmania.enums.timing.SpeedSegmentUnit;
import stepmania.enums.timing.TimingSegmentType;

public class SpeedSegment extends TimingSegment
{
	private double ratio;
	private double delay;
	private SpeedSegmentUnit unit;
	
	
	public SpeedSegment()
	{
		this(TimingSegment.ROW_INVALID);
	}
	public SpeedSegment(int startRow)
	{
		this(startRow, 1);
	}
	public SpeedSegment(int startRow, double r)
	{
		this(startRow, r, 0);
	}
	
	public SpeedSegment(int startRow, double r, double d)
	{
		this(startRow, r, d, SpeedSegmentUnit.SPEEDUNIT_BEATS);
	}
	public SpeedSegment(int startRow, double r, double d,
			SpeedSegmentUnit u)
	{
		super(startRow);
		this.setRatio(r);
		this.setDelay(d);
		this.setUnit(u);
		
	}
	public SpeedSegment(double beat)
	{
		this(beat, 1);
	}
	public SpeedSegment(double beat, double r)
	{
		this(beat, r, 0);
	}
	public SpeedSegment(double beat, double r, double d)
	{
		this(beat, r, d, SpeedSegmentUnit.SPEEDUNIT_BEATS);
	}
	public SpeedSegment(double beat, double r, double d,
			SpeedSegmentUnit u)
	{
		super(beat);
		this.setRatio(r);
		this.setDelay(d);
		this.setUnit(u);
	}
	public SpeedSegment(SpeedSegment other)
	{
		super(other);
		this.setRatio(other.getRatio());
		this.setDelay(other.getDelay());
		this.setUnit(other.getUnit());
	}
	
	
	@Override
	public boolean IsNotable()
	{
		return true;
	}
	
	@Override
	public TimingSegmentType GetType()
	{
		return TimingSegmentType.SEGMENT_SPEED;
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
	/**
	 * @return the delay
	 */
	public double getDelay()
	{
		return delay;
	}
	/**
	 * @param delay the delay to set
	 */
	public void setDelay(double delay)
	{
		this.delay = delay;
	}
	/**
	 * @return the unit
	 */
	public SpeedSegmentUnit getUnit()
	{
		return unit;
	}
	/**
	 * @param unit the unit to set
	 */
	public void setUnit(SpeedSegmentUnit unit)
	{
		this.unit = unit;
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
		temp = Double.doubleToLongBits(delay);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(ratio);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((unit == null) ? 0 : unit.hashCode());
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
		SpeedSegment other = (SpeedSegment) obj;
		if (Double.doubleToLongBits(delay) != Double
				.doubleToLongBits(other.delay))
		{
			return false;
		}
		if (Double.doubleToLongBits(ratio) != Double
				.doubleToLongBits(other.ratio))
		{
			return false;
		}
		if (unit != other.unit)
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
		return "SpeedSegment [row=" + this.getStartRow() +
				", ratio=" + ratio + ", delay=" + delay + 
				", unit="
				+ unit + "]";
	}
}

