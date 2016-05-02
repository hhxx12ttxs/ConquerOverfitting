package stepmania.timing;

import stepmania.enums.timing.SegmentEffectType;
import stepmania.enums.timing.TimingSegmentType;

public class BPMSegment extends TimingSegment
{
	private double bps;
	
	public BPMSegment()
	{
		this(TimingSegment.ROW_INVALID);
	}
	public BPMSegment(int startRow)
	{
		this(startRow, 0);
	}
	public BPMSegment(int startRow, double bpm)
	{
		super(startRow);
		this.setBPM(bpm);
	}
	
	public BPMSegment(double beat)
	{
		this(beat, 0);
	}
	public BPMSegment(double beat, double bpm)
	{
		super(beat);
		this.setBPM(bpm);
	}
	
	public BPMSegment(BPMSegment other)
	{
		super(other);
		this.setBPM(other.getBPM());
	}
	
	
	@Override
	public boolean IsNotable()
	{
		return true;
	}
	
	@Override
	public TimingSegmentType GetType()
	{
		return TimingSegmentType.SEGMENT_BPM;
	}
	
	public SegmentEffectType GetEffectType()
	{
		return SegmentEffectType.EFFECT_INDEFINITE;
	}
	
	/**
	 * @return the bps
	 */
	public double getBPS()
	{
		return bps;
	}
	/**
	 * @param bps the bps to set
	 */
	public void setBPS(double bps)
	{
		this.bps = bps;
	}
	public double getBPM()
	{
		return this.bps * 60;
	}
	public void setBPM(double bpm)
	{
		this.bps = bpm / 60;
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
		temp = Double.doubleToLongBits(bps);
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
		BPMSegment other = (BPMSegment) obj;
		if (Double.doubleToLongBits(bps) != Double.doubleToLongBits(other.bps))
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
				",bps=" + bps + "]";
	}
	
	
}

