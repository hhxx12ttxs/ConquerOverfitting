package stepmania.file.notesloader;

import java.util.ArrayList;

import stepmania.attack.Attack;
import stepmania.chart.Song;
import stepmania.chart.Step;
import stepmania.enums.song.SSCVersion;
import stepmania.enums.timing.SpeedSegmentUnit;
import stepmania.timing.BPMSegment;
import stepmania.timing.ComboSegment;
import stepmania.timing.DelaySegment;
import stepmania.timing.FakeSegment;
import stepmania.timing.LabelSegment;
import stepmania.timing.ScrollSegment;
import stepmania.timing.SpeedSegment;
import stepmania.timing.StopSegment;
import stepmania.timing.TickcountSegment;
import stepmania.timing.TimeSignatureSegment;
import stepmania.timing.TimingData;
import stepmania.timing.WarpSegment;
import stepmania.utils.Utils;

public abstract class FileLoader
{
	/**
	 * What version of the file are we processing right now?
	 * 
	 * Note that this is mainly for .ssc files.
	 */
	private double version;
	
	private int rowsPerBeat;
	
	public abstract Song LoadFromSimfile(final String path)
			throws CloneNotSupportedException;
	
	public abstract void setupNoteData(Step step,
			final ArrayList<String> params);
	
	private static final int FAST_BPM_WARP = 9999999;
	
	/**
	 * @return the version
	 */
	public double getVersion()
	{
		return version;
	}
	
	/**
	 * @param version the version to set
	 */
	public void setVersion(double version)
	{
		this.version = version;
	}
	
	/**
     * @return the rowsPerBeat
     */
    public int getRowsPerBeat()
    {
    	return rowsPerBeat;
    }

	/**
     * @param rowsPerBeat the rowsPerBeat to set
     */
    public void setRowsPerBeat(int rowsPerBeat)
    {
    	this.rowsPerBeat = rowsPerBeat;
    }

	private double RowToBeat(String line)
	{
		String backup = line;
		String trimmed = Utils.trim(Utils.trim(line, 'R'), 'r');
		if (backup.equals(trimmed))
		{
			return Double.parseDouble(line);
		}
		return Double.parseDouble(line) / this.getRowsPerBeat();
	}
	
	public boolean ProcessBPMs(TimingData timing, final String line)
	{
		String[] expressions = line.split(",");
		
		// Prepare for negatives for Warp conversion.
		double negBeat = -1;
		double negBPM = 1;
		double highSpeedBeat = -1;
		boolean notEmpty = false;
		
		for (String expression : expressions)
		{
			String[] values = expression.split("=|\\n");
			if (values.length != 2)
				continue;
			
			notEmpty = true;
			
			double beat = this.RowToBeat(values[0]);
			double newBPM = Double.parseDouble(values[1]);
			
			if (newBPM < 0.0)
			{
				negBeat = beat;
				negBPM = newBPM;
			}
			else if (newBPM > 0.0)
			{
				// add in the warp.
				if (negBPM < 0)
				{
					double endBeat = beat + (newBPM / -negBPM) * (beat - negBeat);
					timing.addSegment(new WarpSegment(negBeat,endBeat - negBeat));
					
					negBeat = -1;
					negBPM = -1;
				}
				// too fast: make it a warp.
				if (newBPM > FileLoader.FAST_BPM_WARP)
				{
					highSpeedBeat = beat;
				}
				// add in a warp
				else if (highSpeedBeat > 0)
				{
					timing.addSegment(new WarpSegment(highSpeedBeat, beat - highSpeedBeat));
					highSpeedBeat = -1;
				}
				else
				{
					timing.addSegment(new BPMSegment(beat, newBPM));
				}
			}
		}
		return notEmpty;
	}
	
	public void ProcessStops(TimingData timing, final String line)
	{
		String[] expressions = line.split(",");
		// Prepare for negative stops -> warps.
		double negBeat = -1;
		double negPause = 0;
		
		for (String expression : expressions)
		{
			String[] values = expression.split("=|\\n");
			if (values.length != 2)
				continue;
			
			double pauseBeat = this.RowToBeat(values[0]);
			double pauseSecs = Double.parseDouble(values[1]);
			
			// check if the prior stop was negative.
			if (negPause > 0)
			{
				double oldBPM = timing.getBPMAtBeat(negBeat);
				double secondsPerBeat = 60 / oldBPM;
				double skipBeats = negPause / secondsPerBeat;
				
				if (negBeat + skipBeats > pauseBeat)
				{
					skipBeats = pauseBeat - negBeat;
				}
				
				timing.addSegment(new WarpSegment(negBeat, skipBeats));
				
				negBeat = -1;
				negPause = 0;
			}
			
			if (pauseSecs < 0)
			{
				negBeat = pauseBeat;
				negPause = -pauseSecs;
			}
			else if (pauseSecs > 0)
			{
				timing.addSegment(new StopSegment(pauseBeat, pauseSecs));
			}
		}
		
		// if there was a prior stop, finish it.
		if (negPause > 0)
		{
			double oldBPM = timing.getBPMAtBeat(negBeat);
			double secondsPerBeat = 60 / oldBPM;
			double skipBeats = negPause / secondsPerBeat;
			timing.addSegment(new WarpSegment(negBeat, skipBeats));
		}
	}
	
	public void ProcessDelays(TimingData timing, final String line)
	{
		String[] expressions = line.split(",");
		for (String expression : expressions)
		{
			String[] values = expression.split("=|\\n");
			if (values.length != 2) continue;
			
			double pauseBeat = this.RowToBeat(values[0]);
			double pauseSecs = Double.parseDouble(values[1]);
			
			if (pauseSecs > 0)
			{
				timing.addSegment(new DelaySegment(pauseBeat, pauseSecs));
			}
		}
	}
	
	public void ProcessTimeSignatures(TimingData timing, final String line)
	{
		String[] expressions = line.split(",");
		for (String expression : expressions)
		{
			String[] values = expression.split("=|\\n");
			
			if (values.length < 3)
			{
				continue;
			}
			double beat = this.RowToBeat(values[0]);
			int num = Integer.parseInt(values[1]);
			int den = Integer.parseInt(values[2]);
			
			if (beat < 0)
			{
				continue;
			}
			if (num < 1)
			{
				continue;
			}
			if (den < 1)
			{
				continue;
			}
			timing.addSegment(new TimeSignatureSegment(beat, num, den));
		}
	}
	
	public void ProcessTickcounts(TimingData timing, final String line)
	{
		String[] expressions = line.split(",");
		for (String expression : expressions)
		{
			String[] values = expression.split("=|\\n");
			if (values.length != 2)
			{
				continue;
			}
			double beat = this.RowToBeat(values[0]);
			int ticks = Integer.parseInt(values[1]);
			
			if (ticks < 0) ticks = 0;
			// TODO: Try to avoid magic numbers.
			if (ticks > 48) ticks = 48;
			timing.addSegment(new TickcountSegment(beat, ticks));
		}
	}
	
	public void ProcessSpeeds(TimingData timing, final String line)
	{
		String[] expressions = line.split(",");
		for (String expression : expressions)
		{
			String[] values = expression.split("=|\\n");
			if (values.length < 2)
			{
				continue;
			}
			
			double beat = this.RowToBeat(values[0]);
			
			if (beat < 0)
			{
				continue;
			}
			
			double ratio = Double.parseDouble(values[1]);
			double delay;
			
			if (values.length == 2)
			{
				delay = 0;
			}
			else
			{
				delay = Double.parseDouble(values[2]);
			}
			
			if (delay < 0)
			{
				continue;
			}
			int unit;
			
			if (values.length <= 3)
			{
				unit = 0;
			}
			else
			{
				unit = Integer.parseInt(values[3]);
			}
			
			timing.addSegment(new SpeedSegment(beat, ratio, delay, 
					SpeedSegmentUnit.getType(unit)));
		}
	}
	
	public void ProcessFakes(TimingData timing, final String line)
	{
		String[] expressions = line.split(",");
		for (String expression : expressions)
		{
			String[] values = expression.split("=|\\n");
			if (values.length != 2)
			{
				continue;
			}
			double beat = this.RowToBeat(values[0]);
			double skip = Double.parseDouble(values[1]);
			
			if (skip > 0)
			{
				timing.addSegment(new FakeSegment(beat, skip));
			}
			else
			{
				// TODO: Log? Throw an exception?
			}
		}
	}
	
	public void ProcessWarps(TimingData timing, final String line)
	{
		String[] expressions = line.split(",");
		for (String expression : expressions)
		{
			String[] values = expression.split("=|\\n");
			if (values.length != 2)
			{
				continue;
			}
			double beat = this.RowToBeat(values[0]);
			double dest = Double.parseDouble(values[1]);
			
			// Early versions were absolute. Now, it's relative.
			if (this.getVersion() < SSCVersion.VERSION_WARP_SEGMENT.getVersion()
					&& dest > beat)
			{
				timing.addSegment(new WarpSegment(beat, dest - beat));
			}
			else if (dest > 0)
			{
				timing.addSegment(new WarpSegment(beat, dest));
			}
			else
			{
				// TODO: Log? Throw an exception?
			}
		}
	}
	
	public void ProcessLabels(TimingData timing, final String line)
	{
		String[] expressions = line.split(",");
		for (String expression : expressions)
		{
			String[] values = expression.split("=|\\n");
			if (values.length != 2)
			{
				continue;
			}
			double beat = this.RowToBeat(values[0]);
			String label = values[1].trim();
			if (beat > 0)
			{
				timing.addSegment(new LabelSegment(beat, label));
			}
			else
			{
				// TODO: Log? Throw an exception?
			}
		}
	}
	
	public void ProcessCombos(TimingData timing, final String line)
	{
		String[] expressions = line.split(",");
		for (String expression : expressions)
		{
			String[] values = expression.split("=|\\n");
			if (values.length == 2 || values.length == 3)
			{
				double beat = this.RowToBeat(values[0]);
				int combos = Integer.parseInt(values[1]);
				int misses = (values.length == 3) ?
						Integer.parseInt(values[2]) : combos;
				timing.addSegment(new ComboSegment(beat, combos, misses));
			}
		}
	}
	
	public void ProcessScrolls(TimingData timing, final String line)
	{
		String[] expressions = line.split(",");
		for (String expression : expressions)
		{
			String[] values = expression.split("=|\\n");
			if (values.length != 2)
			{
				continue;
			}
			double beat = this.RowToBeat(values[0]);
			double ratio = Double.parseDouble(values[1]);
			
			if (beat < 0)
			{
				// TODO: Log? Throw an exception?
			}
			else
			{
				timing.addSegment(new ScrollSegment(beat, ratio));
			}
		}
	}
	
	public void ProcessAttacks(ArrayList<Attack> attacks,
			final ArrayList<String> params)
	{
		if (params.size() % 3 != 0)
		{
			// TODO: Log on error? There needs to be 3 params per attack.
			return;
		}
		for (int i = 0; i < params.size(); i += 3)
		{
			double start = Double.parseDouble(
					params.get(i).substring(5).trim());
			double length = Double.parseDouble(
					params.get(i + 1).substring(4).trim());
			
			if (params.get(i + 1).startsWith("END"))
			{
				length -= start;
			}
			attacks.add(new Attack(start, length,
					params.get(i + 2).substring(5).trim()));
		}
		
	}
	
	public void setOffset(TimingData timing, final double offset)
	{
		timing.setBeat0OffsetSeconds(offset);
	}
}

