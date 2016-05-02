import java.io.*;
import java.util.*;
import java.text.*;

public class Stats {
	String inputFilename;
	LinkedList<LinkedList<Double>> statsMatrix;

	Stats(String filename) throws java.io.IOException {
		this.inputFilename = filename;
		this.statsMatrix = new LinkedList<LinkedList<Double>>();

		BufferedReader statsInput = new BufferedReader(new FileReader (filename));

		String strLine;
		while ((strLine = statsInput.readLine()) != null)   {
			Scanner lineScanner = new Scanner(strLine);

			LinkedList<Double> row = new LinkedList<Double>();
			this.statsMatrix.add(row);

			while (lineScanner.hasNextDouble()) {
				double val = lineScanner.nextDouble();
				row.add(val);
			}
		}
	}

	public LinkedList<LinkedList<Double>> transposeStatsMatrix()
	{
		LinkedList<LinkedList<Double>> transposedMatrix = new LinkedList<LinkedList<Double>>();

		int numColumns = getNumberOfColumns();
		for (int i = 0; i < numColumns; i++) {
			transposedMatrix.add(new LinkedList<Double>());
		}

		// for each row of the statsMatrix, add each value to a column in the transposedMatrix
		for (LinkedList<Double> row : this.statsMatrix) {
			ListIterator< LinkedList<Double> > colIter = transposedMatrix.listIterator(0);

			LinkedList<Double> col = colIter.next();
			for (double rowVal : row) {
				col.add(rowVal);

				if (colIter.hasNext())
					col = colIter.next();
			}
		}

		return transposedMatrix;


	}

	public int getNumberOfColumns()
	{
		LinkedList<Double> firstRow = this.statsMatrix.getFirst();
		return firstRow.size();
	}

	public double getStandardDeviation(LinkedList<Double> column) {
		double sumOfSquares = 0.0;
		double sum = 0.0;
		int N = column.size();
		for (double val : column) {
			sumOfSquares += val*val;
			sum += val;
		}

		double stddev = Math.sqrt(Math.abs(sumOfSquares -  sum*sum/N) / (N-1));

		return stddev;
	}

	public double get95CILowerBound(double avg, double stddev, int n)
	{
		return avg - 1.96 * stddev / Math.sqrt(n);
	}
	public double get95CIUpperBound(double avg, double stddev, int n)
	{
		return avg + 1.96 * stddev / Math.sqrt(n);
	}

	public double getAverage(LinkedList<Double> column) {
		double sum = 0;
		for (double val : column) {
			sum += val;
		}
		return sum / column.size();
	}

	public void writeStats() throws java.io.IOException {

		String statsOutputFilename = this.inputFilename + "_stats";
		FileWriter statsOutput = new FileWriter(statsOutputFilename);

		LinkedList<LinkedList<Double>> columns = transposeStatsMatrix();
		for (LinkedList<Double> column : columns) {
			int n = column.size();
			double avg = getAverage(column);
			double stddev = getStandardDeviation(column);
			double CILowerBound = get95CILowerBound(avg, stddev, n);
			double CIUpperBound = get95CIUpperBound(avg, stddev, n);
			statsOutput.write(avg + " " + stddev + " " + CILowerBound + " " + CIUpperBound + "\n");
		}

		statsOutput.close();
	}
}


