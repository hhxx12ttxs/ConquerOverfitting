package stepmania.timing;

import stepmania.enums.timing.SegmentEffectType;
import stepmania.enums.timing.TimingSegmentType;

public class ComboSegment extends TimingSegment
{
	private int combo;
	private int missCombo;
	
	public ComboSegment()
	{
		this(TimingSegment.ROW_INVALID);
	}
	public ComboSegment(int startRow)
	{
		this(startRow, 1);
	}
	public ComboSegment(int startRow, int c)
	{
		this(startRow, c, 1);
	}
	public ComboSegment(int startRow, int c, int m)
	{
		super(startRow);
		this.setCombo(c);
		this.setMissCombo(m);
	}
	public ComboSegment(double beat)
	{
		this(beat, 1);
	}
	public ComboSegment(double beat, int c)
	{
		this(beat, c, 1);
	}
	public ComboSegment(double beat, int c, int m)
	{
		super(beat);
		this.setCombo(c);
		this.setMissCombo(m);
	}
	public ComboSegment(ComboSegment other)
	{
		super(other);
		this.setCombo(other.getCombo());
		this.setMissCombo(other.getMissCombo());
	}
	
	
	@Override
	public boolean IsNotable()
	{
		return true;
	}
	
	@Override
	public TimingSegmentType GetType()
	{
		return TimingSegmentType.SEGMENT_COMBO;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + combo;
		result = prime * result + missCombo;
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
		ComboSegment other = (ComboSegment) obj;
		if (combo != other.combo)
		{
			return false;
		}
		return missCombo == other.missCombo;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "ComboSegment [row=" + this.getStartRow() + ", combo=" + 
				combo + ", missCombo=" + missCombo + "]";
	}
	@Override
	public SegmentEffectType GetEffectType()
	{
		return SegmentEffectType.EFFECT_INDEFINITE;
	}
	
	/**
	 * @return the combo
	 */
	public int getCombo()
	{
		return combo;
	}
	/**
	 * @param combo the combo to set
	 */
	public void setCombo(int combo)
	{
		this.combo = combo;
	}
	/**
	 * @return the missCombo
	 */
	public int getMissCombo()
	{
		return missCombo;
	}
	/**
	 * @param missCombo the missCombo to set
	 */
	public void setMissCombo(int missCombo)
	{
		this.missCombo = missCombo;
	}
}

