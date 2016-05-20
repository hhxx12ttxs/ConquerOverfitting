package tool;

import java.util.ArrayList;
import java.util.List;
import tool.CliHelper.Options;
import tool.CliHelper.OptionsParser;


/**
 * Display range(s) of integers values on to screen.
 * 
 * @author dengz1
 *
 */
public class Range extends CliBase {	
	private int padZero;	

	@Override
	public void initOptionsParser(OptionsParser optsParser) {
		optsParser.
		setOption("help", "Display this help page.").
		setOption("debug", "Show extra debug info.").
		setOption("padZero", "Pad leading zero(s) to the largest number length.").
		setSummary(
			"Generate range(s) of numbers and print each on new line. The start and\n" +
			"stop are inclusive. Default start=1, stop=1, and step=1.\n" +
			"The max int value is " + Integer.MAX_VALUE + ".").
		setUsage("ztool Range [Options] <start>[:stop[:step]]").
		setExamples(
			"  ztool Range 5\n" +
			"  ztool Range 5 0:7 0:64:8\n" +		
			"  ztool Range -5:5\n" +
			"  ztool Range --padZero=10 2147483647\n");
	}

	@Override
	public void run(Options opts) {
		padZero = opts.getInt("padZero", -1);
		
		for (String arg : opts.getArgs()) {
			String[] rangeArgs = arg.split("\\s*,\\s*");
			for (String rangeStr : rangeArgs) {
				IntRange range = IntRange.makeFrom(rangeStr);
				printRange(range);
			}
		}
	}

	private void printRange(IntRange range) {
		debug("Printing " + range);
		
		if (padZero > 0) {
			debug("pad length = " + padZero);
			final String fmt = "%0" + padZero + "d\n";
			range.eachInt(new IntAction() {				
				@Override
				public void onInt(int i) {
					System.out.printf(fmt, i);
				}
			});
		} else {
			range.eachInt(new IntAction() {				
				@Override
				public void onInt(int i) {
					System.out.println(i);
				}
			});
		}
	}
	
	/////////////////////////////////////////////////////////////////////////
	// Supporting Classes and methods
	/////////////////////////////////////////////////////////////////////////
	public static int toInt(String value) {
		return Integer.parseInt(value);
	}
	public static interface IntAction {
		public void onInt(int i);
	}
	/////////////////////////////////////////////////////////////////////////
	// Range Classes
	/////////////////////////////////////////////////////////////////////////
	/** Integer range holder. */
	public static class IntRange {		
		private int start = 1, stop = 1, step = 1;
		public IntRange(int start, int stop, int step) {
			this.start = start;
			this.stop = stop;
			this.step = step;
		}
		/** This will let user process each int. Good way to avoid loading all
		 * range of numbers into memory list.
		 * 
		 * @param action
		 */
		public void eachInt(IntAction action) {
			if (step > 0) {
				for (int i = start; i <= stop; i += step) {
					action.onInt(i);
				}
			} else {
				for (int i = start; i >= stop; i += step) {
					action.onInt(i);
				}				
			}
		}		
		public List<Integer> toList() {
			final List<Integer> result = new ArrayList<Integer>();
			eachInt(new IntAction() {				
				@Override
				public void onInt(int i) {
					result.add(i);
				}
			});
			return result;
		}
		
		/** rangeStr format is <start>[:<stop>[:<step>]], where start
		 * and stop are inclusive. */
		public static IntRange makeFrom(String rangeStr) {
			String[] values = rangeStr.split(":");
			IntRange range = null;
			if (values.length == 1)
				range = new IntRange(1, toInt(values[0]), 1);
			else if (values.length == 2)
				range = new IntRange(toInt(values[0]), toInt(values[1]), 1);
			else if (values.length == 3)
				range = new IntRange(toInt(values[0]), toInt(values[1]), toInt(values[2]));
			return range;
		}
		
		@Override
		public String toString() {
			return "IntRange(" + start + ", " + stop + ", " + step + ")";
		}
		
		public int getStart() {
			return start;
		}

		public void setStart(int start) {
			this.start = start;
		}

		public int getStop() {
			return stop;
		}

		public void setStop(int stop) {
			this.stop = stop;
		}

		public int getStep() {
			return step;
		}

		public void setStep(int step) {
			this.step = step;
		}		
	}

	public static class IntRangeList {
		
		private List<IntRange> ranges = new ArrayList<IntRange>();
		
		/** range string format is <start>[:<stop>[:<step>]], where start
		 * and stop are inclusive. Multiple ranges may be given separated by space or comma. */
		public static IntRangeList makeFrom(String rangeListStr) {
			IntRangeList rangeList = new IntRangeList();
			String[] ranges = rangeListStr.split(" |,");
			for (String rangeStr : ranges) {
				rangeList.ranges.add(IntRange.makeFrom(rangeStr));
			}
			return rangeList;
		}
		
		public void addIntRange(IntRange range) {
			ranges.add(range);
		}
		
		public List<Integer> toList() {
			final List<Integer> result = new ArrayList<Integer>();
			for (IntRange range : ranges) {
				result.addAll(range.toList());
			}
			return result;
		}
		
		public void eachInt(IntAction action) {
			for (IntRange range : ranges) {
				range.eachInt(action);
			}
		}	
	}
}

