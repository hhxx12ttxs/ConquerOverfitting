/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.l2emuproject.util.concurrent;

import java.io.PrintStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import javolution.text.TextBuilder;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author NB4L1
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public final class RunnableStatsManager
{
	private static final Log _log = LogFactory.getLog(RunnableStatsManager.class);

	private static final Map<Class<?>, ClassStat> _classStats = new HashMap<Class<?>, ClassStat>();

	private static final class ClassStat
	{
		private final String _className;
		private final MethodStat _runnableStat;

		private String[] _methodNames = new String[0];
		private MethodStat[] _methodStats = new MethodStat[0];

		private ClassStat(Class<?> clazz)
		{
			_className = clazz.getName().replace("net.l2emuproject.gameserver.", "");
			_runnableStat = new MethodStat(_className, "run()");

			_methodNames = new String[] { "run()" };
			_methodStats = new MethodStat[] { _runnableStat };

			_classStats.put(clazz, this);
		}

		private MethodStat getRunnableStat()
		{
			return _runnableStat;
		}

		private MethodStat getMethodStat(String methodName, boolean synchronizedAlready)
		{
			// method names will be interned automatically because of compiling, so this gonna work
			if (methodName == "run()")
				return _runnableStat;

			for (int i = 0; i < _methodNames.length; i++)
				if (_methodNames[i].equals(methodName))
					return _methodStats[i];

			if (!synchronizedAlready)
			{
				synchronized (this)
				{
					return getMethodStat(methodName, true);
				}
			}

			methodName = methodName.intern();

			final MethodStat methodStat = new MethodStat(_className, methodName);

			_methodNames = (String[])ArrayUtils.add(_methodNames, methodName);
			_methodStats = (MethodStat[])ArrayUtils.add(_methodStats, methodStat);

			return methodStat;
		}
	}

	private static final class MethodStat
	{
		private final ReentrantLock _lock = new ReentrantLock();

		private final String _className;
		private final String _methodName;

		private long _count;
		private long _total;
		private long _min = Long.MAX_VALUE;
		private long _max = Long.MIN_VALUE;

		private MethodStat(String className, String methodName)
		{
			_className = className;
			_methodName = methodName;
		}

		private void handleStats(long runTime)
		{
			_lock.lock();
			try
			{
				_count++;
				_total += runTime;
				_min = Math.min(_min, runTime);
				_max = Math.max(_max, runTime);
			}
			finally
			{
				_lock.unlock();
			}
		}
	}

	private static ClassStat getClassStat(Class<?> clazz, boolean synchronizedAlready)
	{
		ClassStat classStat = _classStats.get(clazz);

		if (classStat != null)
			return classStat;

		if (!synchronizedAlready)
		{
			synchronized (RunnableStatsManager.class)
			{
				return getClassStat(clazz, true);
			}
		}

		return new ClassStat(clazz);
	}

	public static void handleStats(Class<? extends Runnable> clazz, long runTime)
	{
		getClassStat(clazz, false).getRunnableStat().handleStats(runTime);
	}

	public static void handleStats(Class<?> clazz, String methodName, long runTime)
	{
		getClassStat(clazz, false).getMethodStat(methodName, false).handleStats(runTime);
	}

	public static enum SortBy
	{
		AVG("average"),
		COUNT("count"),
		TOTAL("total"),
		NAME("class"),
		METHOD("method"),
		MIN("min"),
		MAX("max"), ;
		
		private final String _xmlAttributeName;
		
		private SortBy(String xmlAttributeName)
		{
			_xmlAttributeName = xmlAttributeName;
		}
		
		private final Comparator<MethodStat> _comparator = new Comparator<MethodStat>() {
			@Override
			public int compare(MethodStat o1, MethodStat o2)
			{
				final Comparable c1 = getComparableValueOf(o1);
				final Comparable c2 = getComparableValueOf(o2);
				
				if (c1 instanceof Number)
					return c2.compareTo(c1);
				
				final String s1 = (String)c1;
				final String s2 = (String)c2;
				
				final int len1 = s1.length();
				final int len2 = s2.length();
				final int n = Math.min(len1, len2);
				
				for (int k = 0; k < n; k++)
				{
					char ch1 = s1.charAt(k);
					char ch2 = s2.charAt(k);
					
					if (ch1 != ch2)
					{
						if (Character.isUpperCase(ch1) != Character.isUpperCase(ch2))
							return ch2 - ch1;
						else
							return ch1 - ch2;
					}
				}
				
				final int result = len1 - len2;
				
				if (result != 0)
					return result;
				
				switch (SortBy.this)
				{
					case METHOD:
						return NAME._comparator.compare(o1, o2);
					default:
						return 0;
				}
			}
		};
		
		private Comparable getComparableValueOf(MethodStat stat)
		{
			switch (this)
			{
				case AVG:
					return stat._total / stat._count;
				case COUNT:
					return stat._count;
				case TOTAL:
					return stat._total;
				case NAME:
					return stat._className;
				case METHOD:
					return stat._methodName;
				case MIN:
					return stat._min;
				case MAX:
					return stat._max;
				default:
					throw new InternalError();
			}
		}
		
		private static final SortBy[] VALUES = SortBy.values();
	}
	
	public static void dumpClassStats()
	{
		dumpClassStats(null);
	}
	
	public static void dumpClassStats(final SortBy sortBy)
	{
		final List<MethodStat> methodStats = new ArrayList<MethodStat>();
		
		synchronized (RunnableStatsManager.class)
		{
			for (ClassStat classStat : _classStats.values())
				for (MethodStat methodStat : classStat._methodStats)
					if (methodStat._count > 0)
						methodStats.add(methodStat);
		}

		if (sortBy != null)
			Collections.sort(methodStats, sortBy._comparator);

		final List<String> lines = new ArrayList<String>();

		lines.add("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
		lines.add("<entries>");
		lines.add("\t<!-- This XML contains statistics about execution times. -->");
		lines.add("\t<!-- Submitted results will help the developers to optimize the server. -->");

		final String[][] values = new String[SortBy.VALUES.length][methodStats.size()];
		final int[] maxLength = new int[SortBy.VALUES.length];

		for (int i = 0; i < SortBy.VALUES.length; i++)
		{
			final SortBy sort = SortBy.VALUES[i];

			for (int k = 0; k < methodStats.size(); k++)
			{
				final Comparable c = sort.getComparableValueOf(methodStats.get(k));

				final String value;

				if (c instanceof Number)
					value = NumberFormat.getInstance(Locale.ENGLISH).format(((Number)c).longValue());
				else
					value = String.valueOf(c);

				values[i][k] = value;

				maxLength[i] = Math.max(maxLength[i], value.length());
			}
		}

		for (int k = 0; k < methodStats.size(); k++)
		{
			TextBuilder sb = TextBuilder.newInstance();
			sb.append("\t<entry ");

			EnumSet<SortBy> set = EnumSet.allOf(SortBy.class);

			if (sortBy != null)
			{
				switch (sortBy)
				{
				case NAME:
				case METHOD:
					appendAttribute(sb, SortBy.NAME, values[SortBy.NAME.ordinal()][k], maxLength[SortBy.NAME.ordinal()]);
					set.remove(SortBy.NAME);

					appendAttribute(sb, SortBy.METHOD, values[SortBy.METHOD.ordinal()][k], maxLength[SortBy.METHOD.ordinal()]);
					set.remove(SortBy.METHOD);
					break;
				default:
					appendAttribute(sb, sortBy, values[sortBy.ordinal()][k], maxLength[sortBy.ordinal()]);
					set.remove(sortBy);
					break;
				}
			}

			for (SortBy sort : SortBy.VALUES)
				if (set.contains(sort))
					appendAttribute(sb, sort, values[sort.ordinal()][k], maxLength[sort.ordinal()]);

			sb.append("/>");

			lines.add(sb.toString());
			TextBuilder.recycle(sb);
		}

		lines.add("</entries>");

		PrintStream ps = null;
		try
		{
			ps = new PrintStream("MethodStats-" + System.currentTimeMillis() + ".log");

			for (String line : lines)
				ps.println(line);
		}
		catch (Exception e)
		{
			_log.warn("", e);
		}
		finally
		{
			IOUtils.closeQuietly(ps);
		}
	}

	private static void appendAttribute(TextBuilder sb, SortBy sortBy, String value, int fillTo)
	{
		sb.append(sortBy._xmlAttributeName);
		sb.append("=");

		if (sortBy != SortBy.NAME && sortBy != SortBy.METHOD)
			for (int i = value.length(); i < fillTo; i++)
				sb.append(" ");

		sb.append("\"");
		sb.append(value);
		sb.append("\" ");

		if (sortBy == SortBy.NAME || sortBy == SortBy.METHOD)
			for (int i = value.length(); i < fillTo; i++)
				sb.append(" ");
	}
}

