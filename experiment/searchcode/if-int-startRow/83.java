package stepmania.timing;

import stepmania.enums.timing.SegmentEffectType;
import stepmania.enums.timing.TimingSegmentType;

public class TickcountSegment extends TimingSegment
{
	private static final int DEFAULT_TICKS = 4;
	
	private int ticksPerBeat;
	
	public TickcountSegment()
	{
		this(TimingSegment.ROW_INVALID);
	}
	public TickcountSegment(int startRow)
	{
		this(startRow, TickcountSegment.DEFAULT_TICKS);
	}
	public TickcountSegment(int startRow, int ticks)
	{
		super(startRow);
		this.setTicks(ticks);
	}
	public TickcountSegment(double beat)
	{
		this(beat, TimingSegment.ROW_INVALID);
	}
	public TickcountSegment(double beat, int ticks)
	{
		super(beat);
		this.setTicks(ticks);
	}
	public TickcountSegment(TickcountSegment other)
	{
		super(other);
		this.setTicks(other.getTicks());
	}
	
	@Override
	public boolean IsNotable()
	{
		return true;
	}
	
	@Override
	public TimingSegmentType GetType()
	{
		return TimingSegmentType.SEGMENT_TICKCOUNT;
	}
	
	@Override
	public SegmentEffectType GetEffectType()
	{
		return SegmentEffectType.EFFECT_INDEFINITE;
	}
	
	/**
	 * @return the ticksPerBeat
	 */
	public int getTicks()
	{
		return ticksPerBeat;
	}
	/**
	 * @param ticksPerBeat the ticksPerBeat to set
	 */
	public void setTicks(int ticksPerBeat)
	{
		this.ticksPerBeat = ticksPerBeat;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ticksPerBeat;
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
		TickcountSegment other = (TickcountSegment) obj;
		if (ticksPerBeat != other.ticksPerBeat)
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
		return "TickcountSegment [row=" + this.getStartRow() +
				",ticksPerBeat=" + ticksPerBeat + "]";
	}
}

