package stepmania.timing;

import stepmania.enums.timing.SegmentEffectType;
import stepmania.enums.timing.TimingSegmentType;

public class DelaySegment extends TimingSegment
{
	private double pauseSeconds;
	
	public DelaySegment()
	{
		this(TimingSegment.ROW_INVALID);
	}
	public DelaySegment(int startRow)
	{
		this(startRow, 0);
	}
	public DelaySegment(int startRow, double p)
	{
		super(startRow);
		this.setPause(p);
	}
	
	public DelaySegment(double beat)
	{
		this(beat, 0);
	}
	public DelaySegment(double beat, double p)
	{
		super(beat);
		this.setPause(p);
	}
	
	public DelaySegment(DelaySegment other)
	{
		super(other);
		this.setPause(other.getPause());
	}
	
	
	@Override
	public boolean IsNotable()
	{
		return this.pauseSeconds > 0;
	}
	
	@Override
	public TimingSegmentType GetType()
	{
		return TimingSegmentType.SEGMENT_DELAY;
	}
	
	public SegmentEffectType GetEffectType()
	{
		return SegmentEffectType.EFFECT_ROW;
	}

	public double getPause()
	{
		return this.pauseSeconds;
	}
	public void setPause(double pause)
	{
		this.pauseSeconds = pause;
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
		temp = Double.doubleToLongBits(pauseSeconds);
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
		DelaySegment other = (DelaySegment) obj;
		if (Double.doubleToLongBits(pauseSeconds) != Double.doubleToLongBits(other.pauseSeconds))
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
		return "BPMSegment [row=" + this.getStartRow() +
				",bps=" + pauseSeconds + "]";
	}
	
	
}

