package com.vercer.engine.persist;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.AbstractIterator;
import com.vercer.util.Strings;

public class Path implements Comparable<Path>
{
	private final static char FIELD = '.';
	private final static char TYPE = '$';
	private final static char[] SEPERATORS = { FIELD, TYPE };

	public static final Path EMPTY_PATH = new Path("");

	public static class Builder
	{
		private final StringBuilder builder = new StringBuilder();

		public Builder(String property)
		{
			builder.append(property);
		}

		public Builder(Path base)
		{
			builder.append(base.toString());
		}

		public Path build()
		{
			return new Path(builder.toString());
		}

		public Builder field(String name)
		{
			ensureValidPart(name);
			if (builder.length() > 0)
			{
				builder.append(FIELD);
			}
			builder.append(name);
			return this;
		}

		private void ensureValidPart(String name)
		{
			if (Strings.firstIndexOf(name, SEPERATORS) >= 0)
			{
				throw new IllegalArgumentException("Path parts cannot contain " + Arrays.toString(SEPERATORS));
			}
		}

		public Builder meta(String name)
		{
			builder.append(TYPE);
			builder.append(name);
			return this;
		}
		
		public Builder append(Path tail)
		{
			assert tail.isAbsolute() == false;
			builder.append(tail.value);
			return this;
		}
		public Builder append(Part part)
		{
			builder.append(part.text);
			return this;
		}
	}

	public static class Part
	{
		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((text == null) ? 0 : text.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Part other = (Part) obj;
			if (text == null)
			{
				if (other.text != null)
					return false;
			}
			else if (!text.equals(other.text))
				return false;
			return true;
		}

		private final String text;

		private Part(String text)
		{
			this.text = text;
		}

		public boolean isField()
		{
			return text.charAt(0) == FIELD;
		}

		public boolean isMeta()
		{
			return text.charAt(0) == TYPE;
		}
		
		public boolean isRoot()
		{
			char c = text.charAt(0);
			for (char seperator : SEPERATORS)
			{
				if (c == seperator)
				{
					return false;
				}
			}
			return true;
		}

		public String getName()
		{
			if (isRoot())
			{
				return text;
			}
			else
			{
				return text.substring(1);
			}
		}
		
		public int getIndex()
		{
			return Integer.parseInt(getName());
		}
	}

	private final String value;

	public Path(String value)
	{
		this.value = value;
	}

	public List<Part> getParts()
	{
		return new AbstractList<Part>()
		{
			@Override
			public Part get(int index)
			{
				int begin;
				if (index == 0)
				{
					begin = 0;
				}
				else
				{
					begin = Strings.nthIndexOf(value, index, SEPERATORS);
				}
				if (begin >= 0)
				{
					int end = Strings.firstIndexOf(value, begin + 1, SEPERATORS);
					if (end > 0)
					{
						return new Part(value.substring(begin, end));
					}
					else
					{
						return new Part(value.substring(begin));
					}
				}
				else
				{
					return null;
				}
			}

			@Override
			public int size()
			{
				if (value.length() == 0)
				{
					return 0;
				}
				int index = 0;
				int count = 0;
				do
				{
					index = Strings.firstIndexOf(value, index + 2, SEPERATORS);
					count++;
				}
				while (index > 0);

				return count;
			}

			@Override
			public Iterator<Part> iterator()
			{
				return new AbstractIterator<Part>()
				{
					private int index;

					@Override
					protected Part computeNext()
					{
						if (index < 0)
						{
							return null;
						}

						int nextIndex = Strings.firstIndexOf(value, index + 1, SEPERATORS);
						String substring;
						if (nextIndex > 0)
						{
							substring = value.substring(index, nextIndex);
						}
						else
						{
							substring = value.substring(index);
						}
						index = nextIndex;
						return new Part(substring);
					}
				};
			}
		};
	}

	public Path tail(int start)
	{
		int index = Strings.nthIndexOf(value, start, SEPERATORS);
		return new Path(value.substring(index));
	}
	
	public Path head()
	{
		int index = Strings.lastIndexOf(value, value.length() - 1, SEPERATORS);
		return new Path(value.substring(0, index));
	}
	
	public Path head(int end)
	{
		int index = Strings.nthIndexOf(value, end, SEPERATORS);
		return new Path(value.substring(0, index));
	}

	public Part firstPart()
	{
		int index = Strings.firstIndexOf(value, SEPERATORS);
		if (index > 0)
		{
			return new Part(value.substring(0, index));
		}
		else
		{
			return new Part(value);
		}
	}

	public boolean isEmpty()
	{
		return value.length() == 0;
	}
	
	public boolean isAbsolute()
	{
		char c = value.charAt(0);
		for (char seperator : SEPERATORS)
		{
			if (c == seperator)
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString()
	{
		return value;
	}

	public boolean hasPrefix(Path path)
	{
		return path.isEmpty() ||
			(value.startsWith(path.value)
			&& (value.length() == path.value.length() ||
			isSeperator(value.charAt(path.value.length()))));
	}

	private boolean isSeperator(char c)
	{
		for (char sperator : SEPERATORS)
		{
			if (c == sperator)
			{
				return true;
			}
		}
		return false;
	}

	public Part firstPartAfterPrefix(Path prefix)
	{
		assert hasPrefix(prefix);
		return getParts().get(prefix.getParts().size());
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

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
		if (!(obj instanceof Path))
		{
			return false;
		}
		Path other = (Path) obj;
		if (!value.equals(other.value))
		{
			return false;
		}
		return true;
	}

	public int compareTo(Path o)
	{
		return value.compareTo(o.value);
	}

	public static Builder builder(Path path)
	{
		return new Builder(path);
	}

}

