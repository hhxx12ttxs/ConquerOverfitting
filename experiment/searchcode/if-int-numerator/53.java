import java.util.ArrayList;
import java.util.HashSet;

public class LineDetector {

	public ArrayList<int[]> getCandidateLines(int[][] edgeArray, int threshold) {
		ArrayList<int[]> candidateLines = new ArrayList<int[]>();

		int height = edgeArray.length;
		int width = edgeArray[0].length;

		// top iteration
		for (int n = 0; n < width; n++) {
			int[] edgePointFrom = { 0, n };
			candidateLines.addAll(getCandidatesForSide(edgePointFrom,
					edgeArray, width, height, threshold, false, true, true,
					true));
		}

		// right iteration
		for (int n = 0; n < height; n++) {
			int[] edgePointFrom = { n, width - 1 };
			candidateLines.addAll(getCandidatesForSide(edgePointFrom,
					edgeArray, width, height, threshold, false, false, true,
					true));
		}

		// bottom iteration
		for (int n = 0; n < width; n++) {
			int[] edgePointFrom = { height - 1, n };
			candidateLines.addAll(getCandidatesForSide(edgePointFrom,
					edgeArray, width, height, threshold, false, false, false,
					true));
		}

		return new ArrayList<int[]>(new HashSet<int[]>(candidateLines));
	}

	public ArrayList<int[]> reduceCandidates(ArrayList<int[]> candidateLines) {
		if (candidateLines.size() < 2) {
			return candidateLines;
		}
		while (candidateLines.size() > 20) {
			candidateLines = mergeLine(candidateLines);
		}
		while (candidateLines.size() > 2) {
			candidateLines = mergeGroup(candidateLines);
		}
		return candidateLines;
	}

	public ArrayList<int[]> mergeGroup(ArrayList<int[]> candidateLines) {
		int[] line1 = candidateLines.get(0);
		int[] line2 = candidateLines.get(1);
		int difference = getDifference(line1, line2);

		for (int[] lineA : candidateLines) {
			for (int[] lineB : candidateLines) {
				if (lineA != lineB && difference >= getDifference(lineA, lineB)) {
					difference = getDifference(lineA, lineB);
					line1 = lineA;
					line2 = lineB;
				}
			}
		}
		candidateLines.remove(line1);
		candidateLines.remove(line2);
		candidateLines.add(mergeLines(line1, line2));
		return candidateLines;

	}

	public int getDifference(int[] line1, int[] line2) {
		return getPointDifference(line1[0], line1[1], line1[2], line1[3])
				+ getPointDifference(line2[0], line2[1], line2[2], line2[3]);
	}

	public int getPointDifference(int x1, int y1, int x2, int y2) {
		float dx = x2 - x1;
		float dy = y2 - y1;
		return (int) (Math.pow(dx, 2.0) + Math.pow(dy, 2.0));
	}

	public int[] mergeLines(int[] line1, int[] line2) {
		if (line1[4] > line2[4]) {
			line1[4] += line2[4];
			return line1;
		}
		line2[4] += line1[4];
		return line2;
	}

	public ArrayList<int[]> mergeLine(ArrayList<int[]> candidateLines) {
		int[] mergeLine = candidateLines.get(0);
		for (int[] line : candidateLines) {
			if (line[4] < mergeLine[4]) {
				mergeLine = line;
			}
		}
		candidateLines.remove(mergeLine);
		return candidateLines;
	}

	public ArrayList<int[]> getCandidatesForSide(int[] edgePointFrom,
			int[][] edgeArray, int width, int height, int threshold,
			boolean doTop, boolean doRight, boolean doBottom, boolean doLeft) {
		ArrayList<int[]> candidateLinesForSide = new ArrayList<int[]>();

		if (doTop) {
			for (int n = 0; n < width; n++) {
				int[] edgePointTo = { 0, n };
				int[] line = {
						edgePointFrom[0],
						edgePointFrom[1],
						edgePointTo[0],
						edgePointTo[1],
						getStraightLineValue(edgePointFrom, edgePointTo,
								edgeArray) };

				if (line[4] >= threshold) {
					candidateLinesForSide.add(line);
				}
			}
		}
		if (doRight) {
			for (int n = 0; n < height; n++) {
				int[] edgePointTo = { n, width - 1 };
				int[] line = {
						edgePointFrom[0],
						edgePointFrom[1],
						edgePointTo[0],
						edgePointTo[1],
						getStraightLineValue(edgePointFrom, edgePointTo,
								edgeArray) };

				if (line[4] >= threshold) {
					candidateLinesForSide.add(line);
				}
			}
		}
		if (doBottom) {
			for (int n = 0; n < width; n++) {
				int[] edgePointTo = { height - 1, n };
				int[] line = {
						edgePointFrom[0],
						edgePointFrom[1],
						edgePointTo[0],
						edgePointTo[1],
						getStraightLineValue(edgePointFrom, edgePointTo,
								edgeArray) };

				if (line[4] >= threshold) {
					candidateLinesForSide.add(line);
				}
			}
		}
		if (doLeft) {
			for (int n = 0; n < height; n++) {
				int[] edgePointTo = { n, 0 };
				int[] line = {
						edgePointFrom[0],
						edgePointFrom[1],
						edgePointTo[0],
						edgePointTo[1],
						getStraightLineValue(edgePointFrom, edgePointTo,
								edgeArray) };

				if (line[4] >= threshold) {
					candidateLinesForSide.add(line);
				}
			}
		}

		return candidateLinesForSide;
	}

	public int getStraightLineValue(int[] edgePointFrom, int[] edgePointTo,
			int[][] intArray) {
		return getLineSum(intArray, edgePointFrom[0], edgePointFrom[1],
				edgePointTo[0], edgePointTo[1]);
	}

	public int getLineSum(int[][] array, int x, int y, int x2, int y2) {
		int sum = 0;

		int w = x2 - x;
		int h = y2 - y;
		int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0;

		if (w < 0)
			dx1 = -1;
		else if (w > 0)
			dx1 = 1;
		if (h < 0)
			dy1 = -1;
		else if (h > 0)
			dy1 = 1;
		if (w < 0)
			dx2 = -1;
		else if (w > 0)
			dx2 = 1;

		int longest = Math.abs(w);
		int shortest = Math.abs(h);

		if (!(longest > shortest)) {
			longest = Math.abs(h);
			shortest = Math.abs(w);
			if (h < 0)
				dy2 = -1;
			else if (h > 0)
				dy2 = 1;
			dx2 = 0;
		}

		int numerator = longest >> 1;

		for (int i = 0; i <= longest; i++) {
			sum += array[x][y];
			numerator += shortest;
			if (!(numerator < longest)) {
				numerator -= longest;
				x += dx1;
				y += dy1;
			} else {
				x += dx2;
				y += dy2;
			}
		}

		return sum;
	}
	
	public double innerAngle(int[] line1, int[] line2) {
		int line1X1 = line1[0];
		int line1Y1 = line1[1];
		int line1X2 = line1[2];
		int line1Y2 = line1[3];
		int line2X1 = line2[0];
		int line2Y1 = line2[1];
		int line2X2 = line2[2];
		int line2Y2 = line2[3];
		float dx21 = line1X2-line1X1;
		float dy21 = line1Y2-line1Y1;
		float dx31 = line2X2-line2X1;
		float dy31 = line2Y2-line2Y1;
		double m12 = Math.sqrt( dx21*dx21 + dy21*dy21 );
		double m13 = Math.sqrt( dx31*dx31 + dy31*dy31 );
		double theta = Math.acos( (dx21*dx31 + dy21*dy31) / (m12 * m13) );
		if (theta >= Math.PI/2) {
			return Math.PI - theta;
		}
		return theta;
	}

}
