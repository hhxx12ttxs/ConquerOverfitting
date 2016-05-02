package sneer.bricks.hardware.cpu.lang.impl;

import static basis.environments.Environments.my;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrBuilder;

import sneer.bricks.hardware.cpu.lang.Lang;

class LangImpl implements Lang {

	private final Arrays _arrays = new Lang.Arrays(){
		@Override public void reverse(Object[] array) { ArrayUtils.reverse(array);}

		@Override
		public byte[] concat(byte[] a, byte[] b) {
			byte[] ret = new byte[a.length + b.length];
			System.arraycopy(a, 0, ret, 0, a.length);
			System.arraycopy(b, 0, ret, a.length, b.length);
			return ret;
		}
	};
	
	private final Serialization _serialization = new Lang.Serialization(){
		@Override public byte[] serialize(Serializable obj) { return SerializationUtils.serialize(obj); }
		@Override public <T> T serialize(byte[] data) { return (T)SerializationUtils.deserialize(data); }
	};

	private Strings _strings = new Lang.Strings(){
		@Override public boolean isEmpty(String str) { return str == null || str.isEmpty();	}
		@Override public String abbreviate(String str, int maxWidth) { return StringUtils.abbreviate(str, maxWidth); }
		@Override public String join(Collection<?> collection, String separator) { return StringUtils.join(collection, separator); }
		@Override public String trimToNull(String str) { return StringUtils.trimToNull(str); }
		@Override public String remove(String str, String remove) { return StringUtils.remove(str, remove); }
		@Override public String removeStart(String str, String remove) { return StringUtils.removeStart(str, remove); }
		@Override public String chomp(String str, String separator) { return StringUtils.chomp(str, separator); }
		@Override public String strip(String str, String stripChars) { return StringUtils.strip(str, stripChars); }
		@Override public String substringBeforeLast(String str, String separator) {	return StringUtils.substringBeforeLast(str, separator); }
		@Override public String substringAfterLast(String str, String separator) {	return StringUtils.substringAfterLast(str, separator); }

		@Override public String deleteWhitespace(String str) {return StringUtils.deleteWhitespace(str);}

		@Override
		public String insertSpacedSeparators(String str, String separator, int interval) {
			StrBuilder result = new StrBuilder(str);
			int gap = separator.length();
			int numberOfSeparators = (result.length() - 1) / interval;
			for (int index = interval, count = 0; count < numberOfSeparators; ++count, index += interval + gap)
				result.insert(index, separator);
			return result.toString();
		}

		@Override public List<String> readLines(String input) {
	        BufferedReader reader = new BufferedReader(new StringReader(input));
			List<String> list = new ArrayList<String>();
			String line = readLine(reader);
			while (line != null) {
				list.add(line);
				line = readLine(reader);
			}
			return list;		
		}

		private String readLine(BufferedReader reader) {
			try {
				return reader.readLine();
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
		
		@Override	public byte[] toByteArray(String string) { 
			try {
				return string.getBytes("UTF8");
			} catch (UnsupportedEncodingException e) { 
				throw new IllegalStateException(); 
			}
		}

		@Override
		public String[] splitRight(String line, char separator, int maxParts) {
			final List<String> parts = new ArrayList<String>(maxParts);
			int endIndex = line.length();
			
			for (int i = maxParts - 1; i > 0; --i) {
				final int index = line.lastIndexOf(separator, endIndex - 1);
				if (index < 0) break;
				parts.add(line.substring(index + 1, endIndex));
				endIndex = index;
			}
			
			parts.add(line.substring(0, endIndex));
			return reversedArrayGiven(parts);
		}			

		private String[] reversedArrayGiven(final List<String> parts) {
			String[] array = parts.toArray(new String[parts.size()]);
			my(Lang.class).arrays().reverse(array);
			return array;
		}

	};
	
	private SystemUtils _systemUtils = new SystemUtils() {
		@Override
		public boolean isOsLinux() {
			return org.apache.commons.lang.SystemUtils.IS_OS_LINUX;
		}
	};

	@Override public Arrays arrays() { return _arrays; }
	@Override public Serialization serialization() {	 return _serialization;}
	@Override public Strings strings() { return _strings;}
	@Override public SystemUtils system() { return _systemUtils; }

}
