package org.andrill.visualizer.app.cores.handlers.psicat;

import java.util.List;
import java.util.concurrent.Callable;

import org.andrill.visualizer.app.cores.dataset.DepthValueDataSet;
import org.andrill.visualizer.app.cores.dataset.DepthValueDatum;
import org.andrill.visualizer.app.cores.dataset.Range;
import org.andrill.visualizer.app.cores.dataset.RangeList;

/**
 * Extracts grain size information from a PSICAT XML file.
 * 
 * @author Josh Reed (jareed@andrill.org)
 */
public class GrainSizeParser extends PSICATXMLParser implements Callable<DepthValueDataSet<Double>> {
	private RangeList<Double> ranges = new RangeList<Double>();

	public DepthValueDataSet<Double> call() throws Exception {
		final DepthValueDataSet<Double> data = new DepthValueDataSet<Double>(Double.NaN);
		for (final Range<Double> r : ranges.getRanges()) {
			data.add(new DepthValueDatum<Double>(r.start, r.startValue));
			data.add(new DepthValueDatum<Double>(r.end, r.endValue));
		}
		return data;
	}

	private Double grainSize(final double gs) {
		final double inverted = 14 - gs;
		if (inverted == 0) {
			return Double.NaN;
		} else {
			return Double.valueOf(inverted);
		}
	}

	@Override
	protected void handleModels(final List<Model> models) {
		for (final Model m : models) {
			final RangeList<Double> local = new RangeList<Double>();
			if (m.type.endsWith("Interval")) {
				// add our interval
				final DepthValueDatum<Double> i1 = parseModel(m, "top");
				final DepthValueDatum<Double> i2 = parseModel(m, "base");
				ranges.add(new Range<Double>(i1.depth, i1.value, i2.depth, i2.value), false);

				// add our tie points
				for (final Model c : m.children) {
					if (c.type.endsWith("GrainSizeTiePoint")) {
						final DepthValueDatum<Double> t = parseModel(c, "top");
						ranges.add(t.depth, t.value);

					}
				}

				// add our beds
				for (final Model c : m.children) {
					if (c.type.endsWith("Bed")) {
						// parse the top and base
						final DepthValueDatum<Double> b1 = parseModel(c, "top");
						final DepthValueDatum<Double> b2 = parseModel(c, "base");
						local.add(new Range<Double>(b1.depth, b1.value, b2.depth, b2.value), false);
					}
				}
				ranges.add(local, false);
			}
		}
	}

	@Override
	protected boolean isInterested(final String type) {
		return type.startsWith("psicat.core.interval");
	}

	private DepthValueDatum<Double> parseModel(final Model m, final String key) {
		if (m.props.containsKey("depth." + key) && m.props.containsKey("grainsize." + key)) {
			try {
				final double top = Double.parseDouble(m.props.get("depth." + key));
				final double gs = grainSize(Double.parseDouble(m.props.get("grainsize." + key)));
				return new DepthValueDatum<Double>(top, gs);
			} catch (final Exception e) {
				// ignore
			}
		}
		return null;
	}
}
