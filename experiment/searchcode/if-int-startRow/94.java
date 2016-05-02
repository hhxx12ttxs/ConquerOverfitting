package stepmania.timing;

import stepmania.enums.timing.SegmentEffectType;
import stepmania.enums.timing.TimingSegmentType;

public class LabelSegment extends TimingSegment
{
	private String label;
	
	public LabelSegment()
	{
		this(TimingSegment.ROW_INVALID);
	}
	public LabelSegment(int startRow)
	{
		this(startRow, "");
	}
	public LabelSegment(int startRow, String s)
	{
		super(startRow);
		this.setLabel(s);
	}
	public LabelSegment(double beat)
	{
		this(beat, "");
	}
	public LabelSegment(double beat, String s)
	{
		super(beat);
		this.setLabel(s);
	}
	public LabelSegment(LabelSegment other)
	{
		super(other);
		this.setLabel(other.getLabel());
	}
	
	
	@Override
	public boolean IsNotable()
	{
		return true;
	}
	
	@Override
	public TimingSegmentType GetType()
	{
		return TimingSegmentType.SEGMENT_LABEL;
	}
	
	@Override
	public SegmentEffectType GetEffectType()
	{
		return SegmentEffectType.EFFECT_INDEFINITE;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((label == null) ? 0 : label.hashCode());
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
		LabelSegment other = (LabelSegment) obj;
		if (label == null)
		{
			if (other.label != null)
			{
				return false;
			}
		}
		else if (!label.equals(other.label))
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
		return "LabelSegment [row=" + this.getStartRow() +
				",label=" + label + "]";
	}
	/**
	 * @return the label
	 */
	public String getLabel()
	{
		return label;
	}
	/**
	 * @param label the label to set
	 */
	public void setLabel(String label)
	{
		this.label = label;
	}
	
}

