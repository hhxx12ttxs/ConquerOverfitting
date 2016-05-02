package stepmania.timing;

import stepmania.enums.timing.SegmentEffectType;
import stepmania.enums.timing.TimingSegmentType;

public class StopSegment extends TimingSegment
{
	private double pauseSeconds;
	
	public StopSegment()
	{
		this(TimingSegment.ROW_INVALID);
	}
	public StopSegment(int startRow)
	{
		this(startRow, 0);
	}
	public StopSegment(int startRow, double p)
	{
		super(startRow);
		this.setPause(p);
	}
	
	public StopSegment(double beat)
	{
		this(beat, 0);
	}
	public StopSegment(double beat, double p)
	{
		super(beat);
		this.setPause(p);
	}
	
	public StopSegment(StopSegment other)
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
		return TimingSegmentType.SEGMENT_STOP;
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
		StopSegment other = (StopSegment) obj;
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
		return "StopSegment [row=" + this.getStartRow() +
				",bps=" + pauseSeconds + "]";
	}
	
	
}

