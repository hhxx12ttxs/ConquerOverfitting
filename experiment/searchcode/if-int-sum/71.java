package opendata.incremental.metrics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class MathLib {
	public static long min(Collection<Long> l) {
		Long min = Long.MAX_VALUE;
		for (Long v : l) {
			if (v < min)
				min = v;
		}
		return min;
	}

	public static double minDouble(Collection<Double> l) {
		Double min = Double.MAX_VALUE;
		for (Double v : l) {
			if (v < min)
				min = v;
		}
		return min;
	}

	public static long max(Collection<Long> l) {
		Long max = Long.MIN_VALUE;
		for (Long v : l) {
			if (v > max)
				max = v;
		}
		return max;
	}

	public static double maxDouble(Collection<Double> l) {
		Double max = Double.MIN_VALUE;
		for (Double v : l) {
			if (v > max)
				max = v;
		}
		return max;
	}

	public static double mean(Collection<Long> l) {
		Long sum = 0L;
		for (Long v : l) {
			sum += v;
		}
		return sum.doubleValue() / l.size();
	}

	public static double meanDouble(Collection<Double> l) {
		Double sum = 0.0;
		for (Double v : l) {
			sum += v;
		}
		return sum.doubleValue() / l.size();
	}

	public static double median(Collection<Long> l) {
		ArrayList<Long> list = new ArrayList<Long>(l);
		Collections.sort(list);

		int middle = (int) Math.floor(((double) list.size()) / 2);

		if (list.size() % 2 == 1) {
			return list.get(middle);
		} else {
			return (list.get(middle - 1) + list.get(middle)) / 2.0;
		}
	}

	public static double medianDouble(Collection<Double> l) {
		ArrayList<Double> list = new ArrayList<Double>(l);
		Collections.sort(list);

		int middle = (int) Math.floor(((double) list.size()) / 2);

		if (list.size() % 2 == 1) {
			return list.get(middle);
		} else {
			return (list.get(middle - 1) + list.get(middle)) / 2.0;
		}
	}

	public static double sdev(Collection<Long> l) {
		ArrayList<Long> list = new ArrayList<Long>(l);
		Collections.sort(list);
		int sum = 0;
		double mean = mean(list);

		for (Long i : list)
			sum += Math.pow((i - mean), 2);
		return list.size() == 1 ? 0 : Math.sqrt(sum / (list.size() - 1));
	}

	public static double sdevDouble(Collection<Double> l) {
		ArrayList<Double> list = new ArrayList<Double>(l);
		Collections.sort(list);
		int sum = 0;
		double mean = meanDouble(list);

		for (Double i : list)
			sum += Math.pow((i - mean), 2);
		return list.size() == 1 ? 0 : Math.sqrt(sum / (list.size() - 1));
	}
}

