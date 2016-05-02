package net.libcode.www.openlibrary;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.common.io.Files;

public class OpenLibrary {

	protected final JSONParser parser = new JSONParser();

	protected final String OLID_DELIM = "\t";
	protected final String OLID_IDENT = "O";
	protected final String SMALL_JPG = "-S.jpg";
	protected final String MED_JPG = "-M.jpg";

	protected String CSV_FIELD_DELIM = "\t";
	protected String CSV_CELL_DELIM = "|";
	protected String NL = String.format("%n");

	protected Set<String> lines;

	public OpenLibrary() {
	}

	public String getHeader() {
		return null;
	}

	public String parse(String line) throws ParseException {
		return null;
	}

	public void clearFile(File file) throws IOException {
		Files.write(new byte[] {}, file);
	}

	public void appendToFile(File file, String data, Charset charset)
			throws IOException {
		Files.append(data + NL, file, charset);
	}

	public void readSearchTermsFile(File file, int capacity) throws IOException {
		lines = new HashSet<String>(capacity);
		BufferedReader reader = Files.newReader(file, Charset.defaultCharset());
		String line;
		while ((line = reader.readLine()) != null) {
			lines.add(line);
			System.out.println(lines.size());
		}
		reader.close();
	}

	public int processData(File file, File output_file, boolean unique,
			boolean print) throws IOException, ParseException {
		clearFile(output_file);
		BufferedReader reader = Files.newReader(file, Charset.defaultCharset());
		String line;
		String term;
		int count = 0;
		int found = 0;
		appendToFile(output_file, getHeader(), Charset.defaultCharset());
		while ((line = reader.readLine()) != null) {
			count += 1;
			for (Iterator<String> i = lines.iterator(); i.hasNext();) {
				term = i.next();
				if (line.contains(term)) {
					found += 1;
					appendToFile(output_file, parse(line),
							Charset.defaultCharset());
					if (unique)
						i.remove();
					if (print)
						System.out.println(count);
					break;
				}
			}
		}
		reader.close();
		return found;
	}

	/**
	 * Search by OLID. Fast (6 minutes for 100,000 OLID edition searches on 25gb
	 * editions dump)
	 */
	public int processDataByOLID(File file, File output_file, boolean print)
			throws IOException, ParseException {
		clearFile(output_file);
		BufferedReader reader = Files.newReader(file, Charset.defaultCharset());
		String line;
		String[] parts;
		int count = 0;
		int found = 0;
		appendToFile(output_file, getHeader(), Charset.defaultCharset());
		while ((line = reader.readLine()) != null) {
			count += 1;
			parts = line.split(OLID_DELIM);
			if (lines.contains(parts[1])) {
				found += 1;
				appendToFile(output_file, parse(line), Charset.defaultCharset());
				if (print)
					System.out.println(count);
			}
		}
		reader.close();
		return found;
	}

	/**
	 * Get a json string value by key
	 * 
	 * @param json
	 *            A json object
	 * @param key
	 *            The json property key used for retrieval
	 * @return The value associated with key or an empty string
	 */
	public String getJsonString(JSONObject json, String key) {
		String value = "";
		if (json != null) {
			String result = null;
			try {
				result = (String) json.get(key);
			} catch (ClassCastException e) {
				result = String.valueOf(json.get(key));
			}
			if (result != null)
				value = result;
		}
		return value;
	}

	/**
	 * Get a json string value by key from sub-json objects (a hash)
	 * 
	 * @param json
	 *            A json object
	 * @param json_obj
	 *            A key that identifies a sub-json object (a hash)
	 * @param key
	 *            The json property key used for retrieval from the sub-json
	 * @param delimiter
	 *            A delimiter used to join values from the sub-json (if array)
	 * @return The value associated with key or an empty string
	 */
	public String getJsonHashArrayString(JSONObject json, String json_obj,
			String key, String delimiter) {
		String value = "";
		if (json != null) {
			JSONObject a = (JSONObject) json.get(json_obj);
			if (a != null)
				value = getJsonArrayString(a, key, delimiter);
		}
		return value;
	}

	/**
	 * Get a json string value by key from a json array (joined by delimiter)
	 * 
	 * @param json
	 *            A json object
	 * @param key
	 *            A key that identifies a sub-json object (a hash)
	 * @param delimiter
	 *            A delimiter used to join values from the json array
	 * @return The value associated with key or an empty string
	 */
	public String getJsonArrayString(JSONObject json, String key,
			String delimiter) {
		String value = "";
		if (json != null) {
			JSONArray result = (JSONArray) json.get(key);
			if (result != null)
				value = join(result, delimiter);
		}
		return value;
	}

	/**
	 * Get a json string value by key from a json array containing hash(es)
	 * (joined by delimiter)
	 * 
	 * @param json
	 *            A json object
	 * @param key
	 *            A key that identifies a json array
	 * @param sub_key
	 *            A key that identifies a value within array-hash
	 * @param delimiter
	 *            A delimiter used to join values from the json array
	 * @param olid
	 *            Boolean to flag string as an OLID (will strip all but ID)
	 * @return The value associated with key or an empty string
	 */
	public String getJsonHashString(JSONObject json, String key,
			String sub_key, String delimiter, boolean olid) {
		String value = "";
		JSONArray result = (JSONArray) json.get(key);
		if (result != null) {
			String element;
			for (Object x : result) {
				JSONObject a = (JSONObject) x;
				element = getJsonString(a, sub_key);
				if (olid) {
					int idStart = element.indexOf(OLID_IDENT);
					element = element.substring(idStart, element.length());
				}
				value = value + element + delimiter;
			}
		}
		if (!value.isEmpty())
			value = value.substring(0, value.length() - 1);
		return value;
	}

	/**
	 * Get a json string value by key from a json array containing hash(es)
	 * (joined by delimiter)
	 * 
	 * @param json
	 *            A json object
	 * @param key
	 *            A key that identifies a json array
	 * @param sub_key
	 *            A key that identifies an array-hash
	 * @param sub_sub_key
	 *            A key that identifies a value within hash
	 * @param delimiter
	 *            A delimiter used to join values from the json array
	 * @param olid
	 *            Boolean to flag string as an OLID (will strip all but ID)
	 * @return The value associated with key or an empty string
	 */
	public String getJsonHashString(JSONObject json, String key,
			String sub_key, String sub_sub_key, String delimiter, boolean olid) {
		String value = "";
		JSONArray result = (JSONArray) json.get(key);
		if (result != null) {
			String element;
			for (Object x : result) {
				JSONObject a = (JSONObject) x;
				JSONObject b = (JSONObject) a.get(sub_key);
				element = getJsonString(b, sub_sub_key);
				if (olid) {
					int idStart = element.indexOf(OLID_IDENT);
					element = element.substring(idStart, element.length());
				}
				value = value + element + delimiter;
			}
		}
		if (!value.isEmpty())
			value = value.substring(0, value.length() - 1);
		return value;
	}

	public String join(List<?> list, String delimiter) {
		StringBuilder l = new StringBuilder();
		for (Object x : list) {
			if (l.length() != 0)
				l.append(delimiter);
			l.append(x.toString());
		}
		return l.toString();
	}

	public String removeNewLines(String text) {
		return text.replaceAll("\\r\\n|\\r|\\n", " ");
	}

	public long millisToSeconds(long milli) {
		return (long) (milli * 0.001);
	}

}

