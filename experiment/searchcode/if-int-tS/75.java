package thilo.profile;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

class Histograms {

	private Map/* <String,Map<String, int[]>> */histogramData = new TreeMap();

	private List unmergedData;

	int getCounter(String name) {
		Map /* <String, int[] */c = (Map) histogramData.get(name);
		if (c == null)
			return 0;
		int count = 0;
		Iterator details = c.values().iterator();
		while (details.hasNext()) {
			int[] times = (int[]) details.next();
			count += times.length;
		}
		return count;
	}

	Set /* <String */getCounters() {
		return histogramData.keySet();
	}

	void addTimedEvent(String name, String detail, int ms) {
		Map /* <String, int[] */c = (Map) histogramData.get(name);
		if (c == null) {
			c = new HashMap();
			histogramData.put(name, c);
		}
		int[] times = (int[]) c.get(detail);
		if (times == null) {
			c.put(detail, new int[] { ms });
		} else {
			int[] t = new int[times.length + 1];
			System.arraycopy(times, 0, t, 0, times.length);
			t[times.length] = ms;
			c.put(detail, t);
		}

	}

	synchronized void add(Histograms h) {
		if (unmergedData == null) {
			unmergedData = new ArrayList(10000);
		}
		// TODO: merge when the list gets full
		unmergedData.add(h);
	}

	private static final Integer ZERO = new Integer(0);

	private void mergeData() {
		if (histogramData.isEmpty()) {
			Histograms n = ((Histograms) unmergedData.remove(unmergedData
					.size() - 1));
			if (unmergedData == null)
				unmergedData = n.unmergedData;
			else if (n.unmergedData != null)
				unmergedData.addAll(n.unmergedData);
			histogramData = n.histogramData;
		}
		if (unmergedData == null)
			return;
		// first pass: add new names, create maps with counters for names that
		// already exist (and need their arrays resized)
		int size = unmergedData.size();
		if (size == 0)
			return;
		Map countNew = new HashMap();
		for (int i = 0; i < size; i++) {
			Histograms h = (Histograms) unmergedData.get(i);
			// push any unmerged data in there on the stack
			if (h.unmergedData != null) {
				unmergedData.addAll(h.unmergedData);
				size += h.unmergedData.size();
			}
			Iterator e = h.histogramData.entrySet().iterator();
			while (e.hasNext()) {
				Entry ee = (Entry) e.next();
				String name = (String) ee.getKey();
				Map mm = (Map) ee.getValue();
				if (histogramData.containsKey(name)) {
					Map counts = (Map) countNew.get(name);
					Map hDetails = (Map) histogramData.get(name);
					if (counts == null) {
						counts = new HashMap();
						countNew.put(name, counts);
					}
					Iterator f = mm.entrySet().iterator();
					while (f.hasNext()) {
						Entry ff = (Entry) f.next();
						String detail = (String) ff.getKey();
						int[] ts = (int[]) ff.getValue();
						if (hDetails.containsKey(detail)) {
							Integer count = (Integer) counts.get(detail);
							if (count == null) {
								count = new Integer(ts.length);
							} else {
								count = new Integer(count.intValue()
										+ ts.length);
							}
							counts.put(detail, count);

						} else {
							// new detail entry
							hDetails.put(detail, ts);
						}
					}
				} else {
					// new name!
					histogramData.put(name, mm);
				}
			}

		}
		if (countNew.isEmpty()) {
			unmergedData.clear();
			return;
		}

		// second pass: resize the arrays and fill in the new data
		Iterator e = countNew.entrySet().iterator();
		while (e.hasNext()) {
			Entry ee = (Entry) e.next();
			String name = (String) ee.getKey();
			Map details = (Map) ee.getValue();
			Map hDetails = (Map) histogramData.get(name);
			Iterator f = details.entrySet().iterator();
			while (f.hasNext()) {
				Entry ff = (Entry) f.next();
				String detail = (String) ff.getKey();
				int[] oldData = (int[]) hDetails.get(detail);
				int[] newData = new int[oldData.length
						+ ((Integer) ff.getValue()).intValue()];
				System.arraycopy(oldData, 0, newData, 0, oldData.length);
				hDetails.put(detail, newData);
				size = unmergedData.size();
				int insertPos = oldData.length;
				int insertMax = newData.length;
				for (int i = 0; i < size; i++) {
					Histograms h = (Histograms) unmergedData.get(i);
					Map d = (Map) h.histogramData.get(name);
					if (d == null)
						continue;
					int[] ts = (int[]) d.get(detail);
					if (ts == null)
						continue;
					System.arraycopy(ts, 0, newData, insertPos, ts.length);
					insertPos += ts.length;
					if (insertPos == insertMax)
						break;
				}
			}
		}

		unmergedData.clear();

	}

	synchronized void dump(PrintWriter w) {
		if (unmergedData != null) {
			mergeData();
		}

		Iterator names = histogramData.keySet().iterator();
		while (names.hasNext()) {
			String n = (String) names.next();
			w.println(n);
			w
					.println("======[cnt min med 90% 95% max avg] ===================================");

			Iterator details = ((Map) histogramData.get(n)).entrySet()
					.iterator();
			while (details.hasNext()) {
				Entry d = (Entry) details.next();
				String ds = (String) d.getKey();
				int[] ts = (int[]) d.getValue();
				int[] sorted = new int[ts.length];
				System.arraycopy(ts, 0, sorted, 0, ts.length);
				Arrays.sort(sorted);

				long sum = 0;
				for (int i = 0; i < ts.length; i++) {
					int t = ts[i];
					sum += t;
				}

				w.print(" ");
				w.print(ts.length);
				w.print(" ");
				w.print(sorted[0]);
				w.print(" ");
				w.print(sorted[ts.length / 2]);
				w.print(" ");
				w.print(sorted[ts.length * 90 / 100]);
				w.print(" ");
				w.print(sorted[ts.length * 95 / 100]);
				w.print(" ");
				w.print(sorted[ts.length - 1]);
				w.print(" ");
				w.println(sum / ts.length);
				w.print("    ");
				w.println(ds);
				w.println();
			}
		}

	}

}

