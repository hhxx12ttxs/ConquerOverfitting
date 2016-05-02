package stepmania.timing;

import stepmania.enums.timing.SegmentEffectType;
import stepmania.enums.timing.TimingSegmentType;
import stepmania.utils.Utils;

public class WarpSegment extends TimingSegment
{
	private int rowsSkipped;
	
	public WarpSegment()
	{
		this(TimingSegment.ROW_INVALID);
	}
	public WarpSegment(int startRow)
	{
		this(startRow, TimingSegment.ROW_INVALID);
	}
	public WarpSegment(int startRow, int length)
	{
		super(startRow);
		this.setRowsSkipped(length);
	}
	public WarpSegment(int startRow, double length)
	{
		super(startRow);
		this.setRowsSkipped(Utils.BeatToNoteRow(length));
	}
	public WarpSegment(double beat)
	{
		this(beat, TimingSegment.ROW_INVALID);
	}
	public WarpSegment(double beat, int length)
	{
		super(beat);
		this.setRowsSkipped(length);
	}
	public WarpSegment(double beat, double length)
	{
		super(beat);
		this.setRowsSkipped(Utils.BeatToNoteRow(length));
	}
	public WarpSegment(WarpSegment other)
	{
		super(other);
		this.setRowsSkipped(other.getRowsSkipped());
	}
	
	@Override
	public boolean IsNotable()
	{
		return this.rowsSkipped > 0;
	}
	
	@Override
	public TimingSegmentType GetType()
	{
		return TimingSegmentType.SEGMENT_WARP;
	}
	
	@Override
	public SegmentEffectType GetEffectType()
	{
		return SegmentEffectType.EFFECT_RANGE;
	}
	
	/**
	 * @return the rowsSkipped
	 */
	public int getRowsSkipped()
	{
		return rowsSkipped;
	}
	/**
	 * @param rowsSkipped the rowsSkipped to set
	 */
	public void setRowsSkipped(int rowsSkipped)
	{
		this.rowsSkipped = rowsSkipped;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + rowsSkipped;
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
		WarpSegment other = (WarpSegment) obj;
		if (rowsSkipped != other.rowsSkipped)
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
		return "WarpSegment [row=" + this.getStartRow() +
				",rowsSkipped=" + rowsSkipped + "]";
	}
	
}

