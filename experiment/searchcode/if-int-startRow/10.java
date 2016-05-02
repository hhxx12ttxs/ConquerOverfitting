package stepmania.timing;

import stepmania.enums.timing.SegmentEffectType;
import stepmania.enums.timing.TimingSegmentType;
import stepmania.utils.Utils;

public abstract class TimingSegment
implements Comparable<TimingSegment>
{
	protected static final int ROW_INVALID = -1;
	
	private int startRow;
	
	public TimingSegment(int startRow)
	{
		this.setStartRow(startRow);
	}
	public TimingSegment(double beat)
	{
		this.setStartBeat(beat);
	}
	public TimingSegment()
	{
		this.setStartRow(TimingSegment.ROW_INVALID);
	}
	public TimingSegment(TimingSegment other)
	{
		this.setStartRow(other.getStartRow());
	}
	
	
	public abstract boolean IsNotable();
	
	public abstract TimingSegmentType GetType();
	
	public abstract SegmentEffectType GetEffectType();
	
	
	/**
	 * @return the startRow
	 */
	public int getStartRow()
	{
		return startRow;
	}
	
	/**
	 * @param startRow the startRow to set
	 */
	public void setStartRow(int startRow)
	{
		this.startRow = startRow;
	}
	
	public double getStartBeat()
	{
		return Utils.NoteRowToBeat(startRow);
	}
	
	public void setStartBeat(double beat)
	{
		this.startRow = Utils.BeatToNoteRow(beat);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + startRow;
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
		if (obj == null || (obj.getClass() != this.getClass()))
		{
			return false;
		}
		TimingSegment other = (TimingSegment) obj;
		return startRow == other.startRow;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "TimingSegment at row " + this.getStartRow();
	}
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(TimingSegment o)
	{
		int check = new Integer(this.startRow).compareTo(o.getStartRow());
		if (check != 0) return check;
		return this.GetType().compareTo(o.GetType());
	}
}

