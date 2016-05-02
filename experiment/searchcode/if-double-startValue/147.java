package org.andrill.visualizer.app.cores.handlers.psicat;

import java.awt.geom.Rectangle2D;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.andrill.visualizer.app.cores.dataset.Range;
import org.andrill.visualizer.app.cores.dataset.RangeList;
import org.andrill.visualizer.app.cores.handlers.psicat.renderables.LithologyRenderable;
import org.andrill.visualizer.core.services.resource.IResource;
import org.andrill.visualizer.ui.renderable.IRenderable;

import com.sun.opengl.util.texture.Texture;

/**
 * Extracts lithology interval data from
 * 
 * @author Josh Reed (jareed@andrill.org)
 */
public class LithologyParser extends PSICATXMLParser {
	private List<IRenderable> list = new ArrayList<IRenderable>();
	private RangeList<String> ranges = new RangeList<String>();
	private Map<String, IResource> resources = new HashMap<String, IResource>();
	private final URL url;

	/**
	 * Create a new LithologyParser.
	 * 
	 * @param url
	 *            the url.
	 */
	public LithologyParser(final URL url) {
		this.url = url;
	}

	private String findLithology(final List<Model> models) {
		String keywords = null;
		double ratio = Double.MIN_VALUE;
		for (final Model l : models) {
			if (l.type.endsWith("Lithology")) {
				if (l.props.containsKey("keywords") && l.props.containsKey("ratio")) {
					final double r = Double.parseDouble(l.props.get("ratio"));
					if (r > ratio) {
						keywords = l.props.get("keywords");
						ratio = r;
					}
				}
			}
		}
		return keywords;
	}

	/**
	 * Gets the list of parsed renderables.
	 * 
	 * @return the list of renderables.
	 */
	public List<IRenderable> getRenderables() {
		return list;
	}

	private void handle(final Model m) {
		if (m.props.containsKey("depth.top") && m.props.containsKey("depth.base")) {
			try {
				// parse our depths and lithology
				final double top = Double.parseDouble(m.props.get("depth.top"));
				final double base = Double.parseDouble(m.props.get("depth.base"));
				final String lithology = findLithology(m.children);

				// add it to the range list and the resource map
				ranges.add(new Range<String>(top, m.id, base, m.id), false);
				final PSICATModelResource pmr = new PSICATModelResource(m, url);
				pmr.setProperty("lithology", lithology);
				resources.put(m.id, pmr);
			} catch (final NumberFormatException nfe) {
				// ignore
			}
		}
	}

	@Override
	protected void handleModels(final List<Model> models) {
		// build our range list
		for (final Model m : models) {
			if (m.type.endsWith("Interval")) {
				handle(m);

				for (final Model c : m.children) {
					if (m.type.endsWith("Bed")) {
						handle(c);
					}
				}
			}
		}

		// create renderables for all of the ranges
		final Map<String, Texture> textureCache = new ConcurrentHashMap<String, Texture>();
		for (final Range<String> interval : ranges.getRanges()) {
			final IResource r = resources.get(interval.startValue);
			final String keywords = r.getProperty("lithology");
			if (keywords != null) {
				final LithologyRenderable l = new LithologyRenderable(new Rectangle2D.Double(interval.start, 0.0, interval.end - interval.start, 0.10),
						keywords, textureCache);
				l.setResource(r);
				list.add(l);
			}
		}
	}

	@Override
	protected boolean isInterested(final String type) {
		return type.startsWith("psicat.core.interval");
	}
}

