package org.andrill.visualizer.core.services.scheme;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.andrill.visualizer.core.internal.Activator;
import org.xml.sax.SAXException;

/**
 * An implementation of the IScheme interface for XML scheme files.
 * 
 * @author Josh Reed (jareed@andrill.org)
 */
public class XMLScheme implements IScheme {
	private final String id;
	private Map<String, SchemeEntry> scheme = null;
	private final URL url;

	/**
	 * Create a new XMLScheme with the specified id and url.
	 * 
	 * @param id
	 *            the id.
	 * @param url
	 *            the url.
	 */
	public XMLScheme(final String id, final URL url) {
		this.id = id;
		this.url = url;
	}

	private SchemeEntry bestGuess(final String[] keywords) {
		SchemeEntry match = null;
		double score = 0.0;
		for (final SchemeEntry entry : scheme.values()) {
			final double newScore = score(entry, keywords);
			if (newScore > score) {
				match = entry;
				score = newScore;
			}
		}
		return match;
	}

	/**
	 * {@inheritDoc}
	 */
	public SchemeEntry getEntry(final String key) {
		// lazily parse
		if (scheme == null) {
			parse();
		}

		// convert to lowercase
		final String search = key.toLowerCase();

		// get the entry
		if (scheme.containsKey(search)) {
			// exact match
			return scheme.get(search);
		} else {
			// find best guess
			final SchemeEntry guess = bestGuess(KeywordUtils.stringToArray(search));
			if (guess != null) {
				scheme.put(search, guess);
			}
			return guess;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getId() {
		return id;
	}

	private void parse() {
		final SchemeXMLHandler parser = new SchemeXMLHandler();
		final SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			factory.newSAXParser().parse(url.openStream(), parser);
			scheme = parser.getScheme();
		} catch (final SAXException e) {
			Activator.getDefault().error("Unable to open [" + url + "] or was not a scheme XML file", e);
		} catch (final IOException e) {
			Activator.getDefault().error("Unable to open [" + url + "] or was not a scheme XML file", e);
		} catch (final ParserConfigurationException e) {
			Activator.getDefault().error("Unable to open [" + url + "] or was not a scheme XML file", e);
		}
	}

	private double score(final SchemeEntry entry, final String[] keywords) {
		final String[] my = KeywordUtils.stringToArray(entry.get(KeywordUtils.KEYWORDS_PROP));

		// need to implement some caching to speed this up
		double score = 0.0;
		for (final String keyword : keywords) {
			for (final String other : my) {
				if (keyword.equals(other)) {
					score += 1.0; // if it is an exact match, it gets +1.0
					break;
				}
			}
		}

		// if it was an exact match, give a bonus
		if ((keywords.length == my.length) && (Math.round(score) == keywords.length)) {
			score += 1.0;
		}
		return score;
	}
}

