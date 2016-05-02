package stepmania.chart;

import java.util.ArrayList;
import java.util.Map;
import java.util.NavigableMap;

import stepmania.enums.StepsType;
import stepmania.enums.notes.NoteSync;
import stepmania.enums.notes.PlayerNumber;
import stepmania.enums.notes.TapNoteType;
import stepmania.utils.Utils;

public class NoteData
{
	private ArrayList<TrackMap> notes;
	
	private int numTracks;
	
	public NoteData()
	{
		this(4);
	}
	public NoteData(int t)
	{
		this.init(t);
	}
	public NoteData(StepsType s)
	{
		this.init(s.getColumns());
	}
	
	private void init(int c)
	{
		this.setNumTracks(c);
		notes = new ArrayList<TrackMap>(c);
		for (int i = 0; i < c; i++)
		{
			notes.add(new TrackMap());
		}
	}
	
	/**
	 * @return the numTracks
	 */
	public int getNumTracks()
	{
		return numTracks;
	}
	
	/**
	 * @param tracks the numTracks to set
	 */
	public void setNumTracks(int tracks)
	{
		// TODO: Find a way to preserve the original notedata?
		// int old = this.numTracks;
		this.numTracks = tracks;
		
		notes = new ArrayList<TrackMap>(tracks);
		for (int i = 0; i < tracks; i++)
		{
			notes.add(new TrackMap());
		}
	}
	
	public TrackMap getTrack(int track)
	{
		return this.getNotes().get(track);
	}
	
	/**
	 * @return the notes
	 */
	public ArrayList<TrackMap> getNotes()
	{
		return notes;
	}
	
	/**
	 * @param notes the notes to set
	 */
	public void setNotes(ArrayList<TrackMap> notes)
	{
		this.notes = notes;
	}
	
	public void addNote(int track, int row, char c)
	{
		this.addNote(track, row, new TapNote(TapNoteType.getTapNoteType(c)));
	}
	
	public void addNote(int track, int row, TapNote note)
	{
		this.getTrack(track).addNote(row, note);
	}
	
	public void addNote(int track, double beat, char c)
	{
		this.addNote(track, beat, new TapNote(TapNoteType.getTapNoteType(c)));
	}
	
	public void addNote(int track, double beat, TapNote note)
	{
		this.addNote(track, Utils.BeatToNoteRow(beat), note);
	}
	
	public void removeNote(int track, int row)
	{
		this.getTrack(track).removeNote(row);
	}
	
	public void removeNote(int track, double beat)
	{
		this.removeNote(track, Utils.BeatToNoteRow(beat));
	}
	
	public void changeNote(int track, int row, TapNote note)
	{
		// same behavior ironically.
		this.addNote(track, row, note);
	}
	
	public NavigableMap<Integer, TapNote> getTapNoteRange(int track, int start, int end)
	{
		return this.getTrack(track).getTapNoteRange(start, end);
	}
	
	public boolean isEmpty()
	{
		for (TrackMap t : this.getNotes())
		{
			if (t.size() > 0) return false;
		}
		return true;
	}
	
	public boolean isTrackEmpty(int track)
	{
		return this.getTrack(track).isEmpty();
	}
	
	public void clearAll()
	{
		for (TrackMap t : this.getNotes())
		{
			t.clearAll();
		}
	}
	
	public void addNotes(int row, String line)
	{
		this.addNotes(row, line, PlayerNumber.PN_ONE);
	}
	
	public void addNotes(double beat, String line)
	{
		this.addNotes(Utils.BeatToNoteRow(beat), line);
	}
	
	public void addNotes(double beat, String line, PlayerNumber pn)
	{
		this.addNotes(Utils.BeatToNoteRow(beat), line, pn);
	}
	
	public void addNotes(int row, String line, PlayerNumber pn)
	{
		for (int i = 0, track = 0; i < line.length(); track++ ) // manually done.
		{
			char ch = line.charAt(i);
			TapNote tn = TapNoteType.genTapNote(ch, pn);
			
			i++;
			if (i != line.length())
			{
				/* Get more information about the note.
				 * Sometimes there is meta data via brackets.
				 */
				
				ch = line.charAt(i);
				int close = i;
				// future planning: attack data.
				if (ch == '{')
				{
					// TODO: Actually process this.
					close = line.indexOf("}", i);
					i = close;
					ch = line.charAt(i);
				}
				
				// keysound data.
				if (ch == '[')
				{
					close = line.indexOf("]", i);
					int ks = Integer.parseInt(line.substring(i, close));
					tn.setKeysoundIndex(ks);
					i = close;
					ch = line.charAt(i);
				}
			}
			if (tn.getType() != TapNoteType.NONE)
			{
				this.addNote(track, row, tn);
			}
		}
	}
	
	public boolean isValid()
	{
		for (TrackMap t : notes)
		{
			if (!t.isValid()) return false;
		}
		return true;
	}
	
	public int getFirstRow()
	{
		if (this.isEmpty()) return 0;
		int earliest = Utils.MAX_NOTE_ROW;
		for (TrackMap t : this.getNotes())
		{
			if (!t.isEmpty())
				earliest = Math.min(earliest, t.getFirstRow());
		}
		return earliest;
	}
	
	public double getFirstBeat()
	{
		return Utils.NoteRowToBeat(this.getFirstRow());
	}
	
	public int getLastRow()
	{
		if (this.isEmpty()) return 0;
		int latest = 0;
		for (TrackMap t : this.getNotes())
		{
			if (!t.isEmpty())
				latest = Math.max(latest, t.getLastRow());
		}
		return latest;
	}
	
	public double getLastBeat()
	{
		return Utils.NoteRowToBeat(this.getLastRow());
	}
	
	private boolean doesRowNeedSimultaneousPresses(int presses, final int row)
	{
		int notesAtRow = 0;
		for (TrackMap t : this.getNotes())
		{
			TapNote tn = t.getTapNote(row);
			if (tn == null) continue;
			switch (tn.getType())
			{
				case MINE:
				case NONE:
				case FAKE:
				case LIFT: // TODO: Make this count?
				{
					continue; // go to the next track.
				}
				default:
				{
					notesAtRow++;
				}
			}
		}
		if (notesAtRow == 0) return false;
		
		if (notesAtRow < presses)
		{
			// Check the active holds/rolls.
			for (TrackMap t : this.getNotes())
			{
				if (t.isHoldAtRow(row))
				{
					notesAtRow++;
				}
			}
		}
		
		return notesAtRow >= presses;
	}
	
	/**
	 * Get the number of rows that require multiple presses at once.
	 * 
	 * Presses include dealing with holds and rolls.
	 * This does not factor in timing data.
	 * @param presses The mininum number to hit.
	 * @param startRow the start row
	 * @param endRow the end row
	 * @return the number of simultaneous presses.
	 */
	private int getNumRowsWithSimultaneousPresses(int presses,
			int startRow, int endRow)
	{
		int num = 0;
		for (int row = startRow - 1;
				(row = this.getNextRowAllTracks(row)) != -1 && row < endRow;)
		{
			if (this.doesRowNeedSimultaneousPresses(presses, row))
			{
				num++;
			}
		}
		return num;
	}
	
	private int getNumRowsWithSimulatenousTaps(int taps,
			int startRow, int endRow)
	{
		int num = 0;
		for (int row = startRow - 1;
				(row = this.getNextRowAllTracks(row)) != -1 && row < endRow;)
		{
			int notesAtRow = 0;
			for (TrackMap t : this.notes)
			{
				final TapNote tn = t.getTapNote(row);
				if (tn == null) continue;
				switch (tn.getType())
				{
					case MINE:
					case FAKE:
					case NONE:
					{
						continue;
					}
					default:
					{
						notesAtRow++;
					}
				}
			}
			if (notesAtRow >= taps) num++;
		}
		return num;
	}
	
	public int getNumJumps()
	{
		return this.getNumJumps(0);
	}
	public int getNumJumps(int startRow)
	{
		return this.getNumJumps(startRow, Utils.MAX_NOTE_ROW);
	}
	public int getNumJumps(int startRow, int endRow)
	{
		return this.getNumRowsWithSimulatenousTaps(2, startRow, endRow);
	}
	
	public int getNumHolds()
	{
		return this.getNumHolds(0);
	}
	public int getNumHolds(int startRow)
	{
		return this.getNumHolds(startRow, Utils.MAX_NOTE_ROW);
	}
	public int getNumHolds(int startRow, int endRow)
	{
		int holds = 0;
		for (TrackMap t : this.getNotes())
		{
			if (!t.isEmpty())
				holds += t.getNumHolds(startRow, endRow);
		}
		return holds;
	}
	
	public int getNumTriples()
	{
		return this.getNumTriples(0);
	}
	public int getNumTriples(int startRow)
	{
		return this.getNumTriples(startRow, Utils.MAX_NOTE_ROW);
	}
	public int getNumTriples(int startRow, int endRow)
	{
		return this.getNumRowsWithSimulatenousTaps(3, startRow, endRow);
	}
	
	public int getNumHands()
	{
		return this.getNumHands(0);
	}
	public int getNumHands(int startRow)
	{
		return this.getNumHands(startRow, Utils.MAX_NOTE_ROW);
	}
	public int getNumHands(int startRow, int endRow)
	{
		return this.getNumRowsWithSimultaneousPresses(3, startRow, endRow);
	}
	
	public int getNumNotes()
	{
		int num = 0;
		for (TrackMap t : this.getNotes())
		{
			num += t.getNumNotes();
		}
		return num;
	}
	
	public int getNumTapNotes()
	{
		return this.getNumTapNotes(0);
	}
	public int getNumTapNotes(int startRow)
	{
		return this.getNumTapNotes(startRow, Utils.MAX_NOTE_ROW);
	}
	
	/**
	 * Get the number of tap notes found within the NoteData.
	 * This does not factor timing data or jumps into account.
	 * This assumes that tap notes are just plain tap notes.
	 *
	 * @param startRow the start row
	 * @param endRow the end row
	 * @return the number of tap notes
	 */
	public int getNumTapNotes(int startRow, int endRow)
	{
		int taps = 0;
		for (TrackMap t : this.getNotes())
		{
			if (!t.isEmpty())
				taps += t.getNumTapNotes(startRow, endRow);
		}
		return taps;
	}
	
	public int getNumLiftNotes()
	{
		return this.getNumLiftNotes(0);
	}
	public int getNumLiftNotes(int startRow)
	{
		return this.getNumLiftNotes(startRow, Utils.MAX_NOTE_ROW);
	}
	
	/**
	 * Get the number of lift notes found within the NoteData.
	 * This does not factor timing data or jumps into account.
	 *
	 * @param startRow the start row
	 * @param endRow the end row
	 * @return the number of lift notes
	 */
	public int getNumLiftNotes(int startRow, int endRow)
	{
		int taps = 0;
		for (TrackMap t : this.getNotes())
		{
			if (!t.isEmpty())
				taps += t.getNumLiftNotes(startRow, endRow);
		}
		return taps;
	}
	
	public int getNumMines()
	{
		return this.getNumMines(0);
	}
	public int getNumMines(int startRow)
	{
		return this.getNumMines(startRow, Utils.MAX_NOTE_ROW);
	}
	
	/**
	 * Get the number of mines found within the NoteData.
	 * This does not factor timing data or jumps into account.
	 *
	 * @param startRow the start row
	 * @param endRow the end row
	 * @return the number of mines
	 */
	public int getNumMines(int startRow, int endRow)
	{
		int taps = 0;
		for (TrackMap t : this.getNotes())
		{
			if (!t.isEmpty())
				taps += t.getNumMines(startRow, endRow);
		}
		return taps;
	}
	
	public int getNumFakeNotes()
	{
		return this.getNumFakeNotes(0);
	}
	public int getNumFakeNotes(int startRow)
	{
		return this.getNumFakeNotes(startRow, Utils.MAX_NOTE_ROW);
	}
	
	/**
	 * Get the number of fake notes found within the NoteData.
	 * This does not factor timing data or jumps into account.
	 *
	 * @param startRow the start row
	 * @param endRow the end row
	 * @return the number of fake notes
	 */
	public int getNumFakeNotes(int startRow, int endRow)
	{
		int taps = 0;
		for (TrackMap t : this.getNotes())
		{
			if (!t.isEmpty())
				taps += t.getNumFakeNotes(startRow, endRow);
		}
		return taps;
	}
	
	public int getNumRowsWithTap()
	{
		return this.getNumRowsWithTap(0);
	}
	public int getNumRowsWithTap(int startRow)
	{
		return this.getNumRowsWithTap(startRow, Utils.MAX_NOTE_ROW);
	}
	public int getNumRowsWithTap(int startRow, int endRow)
	{
		int num = 0;
		for (int row = startRow - 1;
				(row = this.getNextRowAllTracks(row)) != -1 && (row < endRow);)
		{
			if (this.isTapAtRow(row)) num++;
		}
		return num;
	}
	
	public int getNumRowsWithHold()
	{
		return this.getNumRowsWithHold(0);
	}
	public int getNumRowsWithHold(int startRow)
	{
		return this.getNumRowsWithHold(startRow, Utils.MAX_NOTE_ROW);
	}
	public int getNumRowsWithHold(int startRow, int endRow)
	{
		int num = 0;
		for (int row = startRow - 1;
				(row = this.getNextRowAllTracks(row)) != -1 && (row < endRow);)
		{
			if (this.isHoldAtRow(row)) num++;
		}
		return num;
	}
	
	public int getNumRowsWithMine()
	{
		return this.getNumRowsWithMine(0);
	}
	public int getNumRowsWithMine(int startRow)
	{
		return this.getNumRowsWithMine(startRow, Utils.MAX_NOTE_ROW);
	}
	public int getNumRowsWithMine(int startRow, int endRow)
	{
		int num = 0;
		for (int row = startRow - 1;
				(row = this.getNextRowAllTracks(row)) != -1 && (row < endRow);)
		{
			if (this.isMineAtRow(row)) num++;
		}
		return num;
	}
	
	public int getNumRowsWithAnyTap()
	{
		return this.getNumRowsWithAnyTap(0);
	}
	public int getNumRowsWithAnyTap(int startRow)
	{
		return this.getNumRowsWithAnyTap(startRow, Utils.MAX_NOTE_ROW);
	}
	public int getNumRowsWithAnyTap(int startRow, int endRow)
	{
		int num = 0;
		for (int row = startRow - 1;
				(row = this.getNextRowAllTracks(row)) != -1 && row < endRow;)
		{
			if (this.isTapAtRow(row)
					|| this.isHoldHeadAtRow(row)
					|| this.isRollHeadAtRow(row)
					|| this.isLiftAtRow(row)) num++;
		}
		return num;
	}
	
	public int getFirstTrackWithTap(int row)
	{
		for (int t = 0; t < notes.size(); t++)
		{
			TapNote tn = notes.get(t).getTapNote(row);
			if (tn != null && tn.isTap()) return t;
		}
		return -1;
	}
	
	public boolean isTapAtRow(int row)
	{
		return this.getFirstTrackWithTap(row) != -1;
	}
	
	public boolean isMineAtRow(int row)
	{
		return this.getFirstTrackWithMine(row) != -1;
	}
	
	public boolean isHoldAtRow(int row)
	{
		return this.getFirstTrackWithHoldHead(row) != -1;
	}
	
	public int getFirstTrackWithHoldHead(int row)
	{
		for (int t = 0; t < notes.size(); t++)
		{
			TapNote tn = notes.get(t).getTapNote(row);
			if (tn != null && tn.isHoldHead()) return t;
		}
		return -1;
	}
	
	public int getFirstTrackWithMine(int row)
	{
		for (int t = 0; t < notes.size(); t++)
		{
			TapNote tn = notes.get(t).getTapNote(row);
			if (tn != null && tn.isMine()) return t;
		}
		return -1;
	}
	
	public boolean isHoldHeadAtRow(int row)
	{
		return this.getFirstTrackWithHoldHead(row) != -1;
	}
	
	public int getFirstTrackWithRollHead(int row)
	{
		for (int t = 0; t < notes.size(); t++)
		{
			TapNote tn = notes.get(t).getTapNote(row);
			if (tn != null && tn.isRollHead()) return t;
		}
		return -1;
	}
	
	public boolean isRollHeadAtRow(int row)
	{
		return this.getFirstTrackWithRollHead(row) != -1;
	}
	
	public int getFirstTrackWithLift(int row)
	{
		for (int t = 0; t < notes.size(); t++)
		{
			TapNote tn = notes.get(t).getTapNote(row);
			if (tn != null && tn.isLift()) return t;
		}
		return -1;
	}
	
	public boolean isLiftAtRow(int row)
	{
		return this.getFirstTrackWithLift(row) != -1;
	}
	
	/**
	 * Determine if the note data is composite: that is, it contains
	 * notes for more than just the first player.
	 * @return true if it is composite, false otherwise.
	 */
	public boolean isComposite()
	{
		for (TrackMap t : notes)
		{
			for (TapNote tn : t.getNotes().values())
			{
				if (tn.getPlayerNumber() != PlayerNumber.PN_ONE)
					return true;
			}
		}
		return false;
	}
	
	private ArrayList<NoteData> getPlayerNoteData()
	{
		ArrayList<NoteData> split = new ArrayList<NoteData>();
		if (!this.isComposite())
		{
			split.add(this);
		}
		else
		{
			for (@SuppressWarnings("unused")
			PlayerNumber pn : PlayerNumber.values())
			{
				split.add(new NoteData(this.getNumTracks()));
			}
			
			for (int track = 0; track < this.getNumTracks(); track++)
			{
				TrackMap t = this.getTrack(track);
				for (Map.Entry<Integer, TapNote> entry : 
					t.getNotes().entrySet())
				{
					TapNote tn = entry.getValue();
					int index = tn.getPlayerNumber().getPlayer();
					split.get(index).addNote(track, entry.getKey(), tn);
				}
			}
		}
		return split;
	}
	
	public int getNextRowAllTracks(int start)
	{
		int closest = Utils.MAX_NOTE_ROW;
		boolean hasNext = false;
		for (TrackMap t : this.getNotes())
		{
			if (!t.isEmpty())
			{
				int next = t.getNextNoteRow(start);
				if (next != -1)
				{
					hasNext = true;
					closest = Math.min(closest, next);
				}
			}
		}
		return hasNext ? closest : -1;
	}
	public int getNextRowAllTracks(double start)
	{
		return this.getNextRowAllTracks(Utils.BeatToNoteRow(start));
	}
	public double getNextBeatAllTracks(int start)
	{
		return Utils.NoteRowToBeat(this.getNextRowAllTracks(start));
	}
	public double getNextBeatAllTracks(double start)
	{
		return Utils.NoteRowToBeat(this.getNextRowAllTracks(start));
	}
	
	public int getPrevRowAllTracks(int end)
	{
		int closest = -1;
		boolean hasPrev = false;
		for (TrackMap t : this.getNotes())
		{
			if (!t.isEmpty())
			{
				int prev = t.getPrevNoteRow(end);
				if (prev != -1)
				{
					hasPrev = true;
					closest = Math.max(closest, prev);
				}
			}
		}
		return hasPrev ? closest : -1;
	}
	public int getPrevRowAllTracks(double end)
	{
		return this.getPrevRowAllTracks(Utils.BeatToNoteRow(end));
	}
	public double getPrevBeatAllTracks(int end)
	{
		return Utils.NoteRowToBeat(this.getPrevRowAllTracks(end));
	}
	public double getPrevBeatAllTracks(double end)
	{
		return Utils.NoteRowToBeat(this.getPrevRowAllTracks(end));
	}
	
	public boolean isRowEmpty(final int row)
	{
		for (TrackMap t : this.notes)
		{
			TapNote tn = t.getTapNote(row);
			if (tn != null && tn.getType() != TapNoteType.NONE)
			{
				return false;
			}
		}
		return true;
	}
	
	private NoteSync getSmallestSyncInRange(final int start, final int end)
	{
		for (NoteSync ns : NoteSync.values())
		{
			int rowSpace = ns.toRowSpace();
			boolean foundSmaller = false;
			for (int row = start - 1;
					(row = this.getNextRowAllTracks(start)) != -1 && row < end;)
			{
				if (row % rowSpace == 0)
				{
					continue; // no need to check this.
				}
				if (!this.isRowEmpty(row))
				{
					foundSmaller = true;
					break;
				}
			}
			if (foundSmaller)
				continue; // check the next note sync.
			return ns; // we found the smallest.
			
		}
		return null;
	}
	
	private NoteSync getSmallestSyncForMeasure(final int measure)
	{
		final int measureStart = measure * Utils.ROWS_PER_MEASURE;
		final int measureEnd = (measure + 1) * Utils.ROWS_PER_MEASURE;
		return this.getSmallestSyncInRange(measureStart, measureEnd);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((notes == null) ? 0 : notes.hashCode());
		result = prime * result + numTracks;
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
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		NoteData other = (NoteData) obj;
		if (notes == null)
		{
			if (other.notes != null)
			{
				return false;
			}
		}
		else if (!notes.equals(other.notes))
		{
			return false;
		}
		if (numTracks != other.numTracks)
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
		ArrayList<NoteData> split = this.getPlayerNoteData();
		
		StringBuilder sb = new StringBuilder("\n");
		int lastMeasure = (int)(this.getLastBeat() / Utils.BEATS_PER_MEASURE);
		
		for (NoteData nd : split)
		{
			if (nd != split.get(0))
			{
				sb.append("&\n");
			}
			for (int m = 0; m <= lastMeasure; m++)
			{
				if (m > 0)
				{
					sb.append(",\n");
				}
				// TODO: Should we append measure comment data?
				NoteSync ns = this.getSmallestSyncForMeasure(m);
				int rowSpace = (ns == null) ? 1 : ns.toRowSpace();
				
				final int measureStart = m * Utils.ROWS_PER_MEASURE;
				final int measureEnd = (m + 1) * Utils.ROWS_PER_MEASURE - 1;
				
				for (int r = measureStart; r < measureEnd; r += rowSpace)
				{
					for (TrackMap t : nd.notes)
					{
						TapNote tn = t.getTapNote(r);
						if (tn == null)
							sb.append('0');
						else
						{
							sb.append(tn.getType().getType());
							// TODO: Attacks, Keysounds, other future stuff.
						}
					}
					
					sb.append('\n');
				}
			}
		}
		
		return sb.toString();
	}
}

