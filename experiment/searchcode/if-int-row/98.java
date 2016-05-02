package stepmania.chart;

import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;

import stepmania.enums.notes.TapNoteType;

public class TrackMap
{
	private TreeMap<Integer, TapNote> notes;
	
	public TrackMap()
	{
		setNotes(new TreeMap<Integer, TapNote>());
	}
	
	/**
	 * @return the notes
	 */
	public TreeMap<Integer, TapNote> getNotes()
	{
		return notes;
	}
	
	/**
	 * @param notes the notes to set
	 */
	public void setNotes(TreeMap<Integer, TapNote> notes)
	{
		this.notes = notes;
	}
	
	public void addNote(int row, TapNote note)
	{
		this.getNotes().put(row, note);
	}
	
	public void removeNote(int row)
	{
		this.getNotes().remove(row);
	}
	
	public void changeNote(int row, TapNote note)
	{
		// same behavior ironically.
		this.addNote(row, note);
	}
	
	/**
	 * Returns the range of notes in the form [start, end).
	 * @param start the starting row.
	 * @param end the row to indicate the end: it actually ends one before.
	 * @return the requested range of notes.
	 */
	public NavigableMap<Integer, TapNote> getTapNoteRange(int start, int end)
	{
		return this.getNotes().subMap(start, true, end, false);
	}
	
	public void clearAll()
	{
		this.getNotes().clear();
	}
	
	public int size()
	{
		return this.getNotes().size();
	}
	
	public boolean isEmpty()
	{
		return this.size() == 0;
	}
	
	public SortedMap<Integer, TapNote> getSortedNotes()
	{
		return this.getSortedNotes(0);
	}
	
	public SortedMap<Integer, TapNote> getSortedNotes(int k)
	{
		return this.getNotes().tailMap(k);
	}
	
	public SortedMap<Integer, TapNote> getSortedNotes(int s, int e)
	{
		return this.getNotes().subMap(s, e);
	}
	
	public boolean isValid()
	{
		boolean inHold = false;
		for (Map.Entry<Integer, TapNote> entry :
			this.getSortedNotes().entrySet())
		{
			final TapNote tn = entry.getValue();
			if (!inHold)
			{
				if (tn.isHoldTail())
				{
					// TODO: Log this somehow?
					return false;
				}
				if (tn.isHoldHead() || tn.isRollHead())
				{
					inHold = true;
				}
			}
			else
			{
				if (tn.isHoldTail())
				{
					inHold = false;
				}
				else
				{
					// TODO: Log this somehow?
					return false;
				}
			}
		}
		return !inHold;
	}
	
	public boolean isHoldAtRow(final int target)
	{
		for (int row = target;
				(row = this.getPrevNoteRow(row)) != -1 && row >= 0;)
		{
			final TapNote tn = this.getTapNote(row);
			switch (tn.getType())
			{
				case HOLD_HEAD:
				case ROLL_HEAD:
				{
					int end = this.getNextNoteRow(row);
					if (end != -1)
					{
						return this.getTapNote(end).getType()
								== TapNoteType.HOLD_TAIL;
					}
					return false;
				}
				case TAP:
				case MINE:
				case LIFT:
				case FAKE:
				{
					return false;
				}
				case NONE:
				case AUTO_KEYSOUND:
				default: // Unsure what other cases there are.
				{
					continue;
				}
			}
		}
		return false;
	}
	
	public int getNumNotes()
	{
		int num = 0;
		for (Map.Entry<Integer, TapNote> entry :
			this.getSortedNotes().entrySet())
		{
			if (!entry.getValue().isHoldTail()) num++;
		}
		return num;
	}
	
	public int getPrevNoteRow(int end)
	{
		SortedMap<Integer, TapNote> map = this.getSortedNotes(0, end);
		if (map.size() > 0) return map.lastKey();
		return -1;
	}
	
	public int getNextNoteRow(int start)
	{
		SortedMap<Integer, TapNote> map = this.getSortedNotes(start + 1);
		if (map.size() > 0) return map.firstKey();
		return -1;
	}
	
	private int getNumTapNoteTypes(int startRow, int endRow, TapNoteType t)
	{
		int num = 0;
		for (Map.Entry<Integer, TapNote> entry :
			this.getSortedNotes(startRow, endRow).entrySet())
		{
			if (entry.getValue().getType() == t) num++;
		}
		return num;
	}
	
	public int getNumTapNotes(int startRow, int endRow)
	{
		return this.getNumTapNoteTypes(startRow, endRow, TapNoteType.TAP);
	}
	
	public int getNumHolds(int startRow, int endRow)
	{
		return this.getNumTapNoteTypes(startRow, endRow, TapNoteType.HOLD_HEAD);
	}
	
	public int getNumLiftNotes(int startRow, int endRow)
	{
		return this.getNumTapNoteTypes(startRow, endRow, TapNoteType.LIFT);
	}
	
	public int getNumMines(int startRow, int endRow)
	{
		return this.getNumTapNoteTypes(startRow, endRow, TapNoteType.MINE);
	}
	
	public int getNumFakeNotes(int startRow, int endRow)
	{
		return this.getNumTapNoteTypes(startRow, endRow, TapNoteType.FAKE);
	}
	
	public int getFirstRow()
	{
		return this.getNotes().firstKey();
	}
	
	public int getLastRow()
	{
		return this.getNotes().lastKey();
	}
	
	public boolean hasTap()
	{
		for (Map.Entry<Integer, TapNote> entry :
			this.getSortedNotes().entrySet())
		{
			if (entry.getValue().isTap()) return true;
		}
		return false;
	}
	
	public boolean hasTap(int row)
	{
		if (notes.containsKey(row))
		{
			return notes.get(row).isTap();
		}
		return false;
	}
	
	public TapNote getTapNote(int row)
	{
		return notes.get(row);
	}
}

