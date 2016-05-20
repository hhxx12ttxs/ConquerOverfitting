package org.fpr.obj;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import org.fpr.utils.GrayMatWrapper;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class Template {

	private LinkedHashSet<Minutia> minutiae = new LinkedHashSet<Minutia>();
	private LinkedHashSet<Minutia> singularities = new LinkedHashSet<Minutia>();
	private Point center;

	// 10 degrees of tolerance
	public static final double threshAngle = Math.PI / 9;
	public static final int threshDist = 8;

	public Point getCenter() {
		if (center == null && singularities.size() > 0) {
			int cnt = 0;
			center = new Point(0, 0);
			for (Minutia m : singularities) {
				if (m.type == MinutiaType.WHORL || m.type == MinutiaType.LOOP) {
					center.x += m.pos.x;
					center.y += m.pos.y;
					cnt++;
				}
			}
			if (cnt > 0) {
				center.x /= cnt;
				center.y /= cnt;
			}
		}
		return center;
	}

	public void setCenter(Point p) {
		center = p;
	}

	public boolean minutiaeCalculated() {
		return minutiae.size() > 0;
	}

	public boolean singularitiesCalculated() {
		return singularities.size() > 0;
	}

	public void extractSingularities(Mat src) {
		ArrayList<Minutia> arr = new ArrayList<Minutia>();

		GrayMatWrapper grm = new GrayMatWrapper(src);
		for (int y = 1; y < grm.getBlockHeight() - 1; y++) {
			for (int x = 1; x < grm.getBlockWidth() - 1; x++) {
				// measurement of Poincare index is in pi/4 radian units
				int angle = grm.getPoincareIndex(x, y);

				Minutia m;
				switch (angle) {
				// In case of 360
					case 8:
						m = new Minutia(new Point(x * grm.getBlockSize() + 8, y * grm.getBlockSize() + 8), 0, 0, 0, MinutiaType.WHORL);
						arr.add(m);
						break;
					// In case of 180
					case 4:
						m = new Minutia(new Point(x * grm.getBlockSize() + 8, y * grm.getBlockSize() + 8), 0, 0, 0, MinutiaType.LOOP);
						arr.add(m);
						break;
					// In case of -180
					case -4:
						m = new Minutia(new Point(x * grm.getBlockSize() + 8, y * grm.getBlockSize() + 8), 0, 0, 0, MinutiaType.DELTA);
						arr.add(m);
						break;
				}
			}
		}

		//clearAndSaveSingularities(arr);
		singularities.addAll(arr);
		center = getCenter();
	}

	private void clearAndSaveSingularities(ArrayList<Minutia> arr) {
		while (!arr.isEmpty()) {
			int count = 1;
			Minutia m = arr.remove(0);
			Minutia newm = new Minutia(new Point(m.pos.x, m.pos.y), 0, 0, 0, m.type);
			Iterator<Minutia> it = arr.iterator();
			while (it.hasNext()) {
				Minutia n = it.next();
				if (n.type == m.type && Math.abs(n.pos.x - m.pos.x) <= 16 && Math.abs(n.pos.y - m.pos.y) <= 16) {
					count++;
					newm.pos.x += n.pos.x;
					newm.pos.y += n.pos.y;
					it.remove();
				}
			}
			newm.pos.x /= count;
			newm.pos.y /= count;
			singularities.add(newm);
		}
	}

	public void extractMinutiae(Mat src, Mat mask, int limit) {
		int startx = (int) center.x;
		int starty = (int) center.y;
		GrayMatWrapper grm = new GrayMatWrapper(src);
		GrayMatWrapper grmask = new GrayMatWrapper(mask);
		System.gc();

		// set the limits different way???
		for (int i = threshDist; i < limit && (startx - i > 1 || starty - i > 1 || startx + i < grm.getWidth() - 1 || starty + i < grm.getHeight() - 1); i++) {
			// kernel size is i * 2 + 1
			int ksize = i * 2 + 1;
			int kborderlen = (ksize - 1) * 4;
			// start a bit away from the center
			for (int j = 0; j < kborderlen; j++) {
				int[] pos = grm.getKernelPosByIndex(j, startx, starty, ksize);
				// Works in case of block-wise masks
				if (pos[0] < threshDist || pos[1] < threshDist || pos[0] > grm.getWidth() - threshDist - 1 || pos[1] > grm.getHeight() - threshDist - 1)
					continue;
				if (grmask.getPixel(pos[0] - threshDist, pos[1] - threshDist) == 0 || grmask.getPixel(pos[0] + threshDist, pos[1] + threshDist) == 0) continue;

				int onePixel = grm.getPixel(pos[0], pos[1]);
				if (onePixel != 0) {
					int count = grm.crossingNumber(pos[0], pos[1]);
					double radius = Math.sqrt(Math.pow(pos[0] - startx, 2) + Math.pow(starty - pos[1], 2));
					double azimuth = Math.atan2(starty - pos[1], pos[0] - startx);
					if (count == 1) {
						double angle = grm.findOrientation(pos[0], pos[1], MinutiaType.RIDGE_ENDING);
						Minutia m = new Minutia(new Point(pos[0], pos[1]), angle, radius, azimuth, MinutiaType.RIDGE_ENDING);
						minutiae.add(m);
					} else if (count >= 3) {
						double angle = grm.findOrientation(pos[0], pos[1], MinutiaType.BIFURCATION);
						Minutia m = new Minutia(new Point(pos[0], pos[1]), angle, radius, azimuth, MinutiaType.BIFURCATION);
						minutiae.add(m);
					}
				}
			}
		}

		System.gc();
		clearFalseMinutiae();
	}

	private void clearFalseMinutiae() {
		HashSet<Minutia> removed = new HashSet<Minutia>();
		Iterator<Minutia> it = minutiae.iterator();
		while (it.hasNext()) {
			Minutia temp = it.next();
			if (removed.contains(temp)) continue;
			if (temp.type == MinutiaType.RIDGE_ENDING) {
				Minutia el = findFalseMinutiae(minutiae, temp, MinutiaType.RIDGE_ENDING);
				if (el != null && !removed.contains(el)) {
					it.remove();
					removed.add(el);
				}
			}
		}
		minutiae.removeAll(removed);
		removed.clear();

		it = minutiae.iterator();
		while (it.hasNext()) {
			Minutia temp = it.next();
			if (removed.contains(temp)) continue;
			if (temp.type == MinutiaType.RIDGE_ENDING) {
				Minutia el = findFalseMinutiae(minutiae, temp, MinutiaType.BIFURCATION);
				if (el != null && !removed.contains(el)) {
					it.remove();
					removed.add(el);
				}
			}
		}
		minutiae.removeAll(removed);
		removed.clear();

		it = minutiae.iterator();
		while (it.hasNext()) {
			Minutia temp = it.next();
			if (removed.contains(temp)) continue;
			if (temp.type == MinutiaType.BIFURCATION) {
				Minutia el = findFalseMinutiae(minutiae, temp, MinutiaType.BIFURCATION);
				if (el != null && !removed.contains(el)) {
					it.remove();
					removed.add(el);
				}
			}
		}
		minutiae.removeAll(removed);
	}

	private Minutia findFalseMinutiae(Collection<Minutia> arr, Minutia m, MinutiaType type) {
		Minutia min = null;
		Iterator<Minutia> it = arr.iterator();
		while (it.hasNext()) {
			Minutia temp = it.next();
			if (temp.type == type && !temp.equals(m) && m.distanceBetween(temp) < threshDist && m.facingAngle(temp) < threshAngle) {
				if (min == null || m.distanceBetween(temp) < m.distanceBetween(min)) min = temp;
			}
		}

		return min;
	}

	public void drawMinutiae(Mat src) {
		drawPoints(src, minutiae);
	}

	public void drawSingularities(Mat src) {
		drawPoints(src, singularities);
	}

	private void drawPoints(Mat src, Collection<Minutia> points) {
		Point p1;
		Point p2;
		int radius = 2;
		Core.circle(src, center, radius, new Scalar(255, 0, 255), -1);
		for (Minutia m : points) {
			switch (m.type) {
				case BIFURCATION:
					p1 = new Point(m.pos.x + Math.cos(m.angle) * radius, m.pos.y - Math.sin(m.angle) * radius);
					p2 = new Point(m.pos.x + Math.cos(m.angle) * radius * 3, m.pos.y - Math.sin(m.angle) * radius * 3);
					Core.line(src, p1, p2, new Scalar(255, 0, 0));
					Core.rectangle(src, new Point(m.pos.x - radius, m.pos.y - radius), new Point(m.pos.x + radius, m.pos.y + radius), new Scalar(255, 0, 0), 1);
					break;
				case RIDGE_ENDING:
					p1 = new Point(m.pos.x + Math.cos(m.angle) * radius, m.pos.y - Math.sin(m.angle) * radius);
					p2 = new Point(m.pos.x + Math.cos(m.angle) * radius * 3, m.pos.y - Math.sin(m.angle) * radius * 3);
					Core.line(src, p1, p2, new Scalar(0, 255, 0));
					Core.rectangle(src, new Point(m.pos.x - radius, m.pos.y - radius), new Point(m.pos.x + radius, m.pos.y + radius), new Scalar(0, 255, 0), 1);
					break;
				case LOOP:
				case WHORL:
					Core.circle(src, m.pos, radius, new Scalar(0, 0, 255), -1);
					break;
				case DELTA:
					Core.rectangle(src, new Point(m.pos.x - radius, m.pos.y - radius), new Point(m.pos.x + radius, m.pos.y + radius), new Scalar(0, 0, 255), -1);
					break;
			}
		}
	}

	public LinkedHashMap<Minutia, Minutia> getMatches(Template t) {
		LinkedHashMap<Minutia, Minutia> matches = new LinkedHashMap<Minutia, Minutia>();
		for (Minutia p : minutiae) {
			double maxScore = 0;
			Minutia matched = null;
			for (Minutia q : t.minutiae) {
				if (matches.containsKey(q)) continue;
				double diff = Minutia.getAbsAngleDiff(p.azimuth - p.angle, q.azimuth - q.angle);
				double dist = Math.abs(p.radius - q.radius);
				if (p.type == q.type && dist < threshDist && diff < threshAngle) {
					double tempScore = 2 * (threshDist - dist) / (3 * threshDist) + (threshAngle - diff) / (3 * threshAngle);
					if (tempScore > maxScore) {
						maxScore = tempScore;
						matched = q;
					}
				}
			}

			if (matched != null) {
				matches.put(matched, p);
			}
		}

		return matches;
	}

	public Mat drawReference(Mat src, LinkedHashMap<Minutia, Minutia> matches) {
		Mat temp = new Mat();
		if (src.channels() == 1) Imgproc.cvtColor(src, temp, Imgproc.COLOR_GRAY2BGR);
		else src.copyTo(temp);
		drawPoints(temp, matches.values());
		Integer i = 0;
		for (Minutia m : matches.values()) {
			i++;
			Core.putText(temp, i.toString(), new Point(m.pos.x + 4, m.pos.y - 4), Core.FONT_HERSHEY_PLAIN, 0.5, new Scalar(255,0,255));
		}
		return temp;
	}

	public Mat drawProbe(Mat src, LinkedHashMap<Minutia, Minutia> matches) {
		Mat temp = new Mat();
		if (src.channels() == 1) Imgproc.cvtColor(src, temp, Imgproc.COLOR_GRAY2BGR);
		else src.copyTo(temp);
		drawPoints(temp, matches.keySet());
		Integer i = 0;
		for (Minutia m : matches.keySet()) {
			i++;
			Core.putText(temp, i.toString(), new Point(m.pos.x + 4, m.pos.y - 4), Core.FONT_HERSHEY_PLAIN, 0.5, new Scalar(255,0,255));
		}
		return temp;
	}

	public double calculateScore(LinkedHashMap<Minutia, Minutia> matches, ArrayList<Double> scores, ArrayList<Double> azimuths) {
		double score = 0;
		for (Minutia q : matches.keySet()) {
			Minutia p = matches.get(q);
			double diff = Minutia.getAbsAngleDiff(p.azimuth - p.angle, q.azimuth - q.angle);
			double dist = Math.abs(p.radius - q.radius);
			double tempScore = 2 * (threshDist - dist) / (3 * threshDist) + (threshAngle - diff) / (3 * threshAngle);

			score += tempScore;
			if (scores != null) scores.add(tempScore);

			double azdiff = Minutia.getAbsAngleDiff(p.azimuth, q.azimuth);
			if (azimuths != null) azimuths.add(azdiff);
		}
		return score;
	}

	public int numMinutiae() {
		return minutiae.size();
	}
}

