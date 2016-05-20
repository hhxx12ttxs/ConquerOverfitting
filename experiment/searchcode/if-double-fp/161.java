package adj_ordering.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.umass.nlp.utils.ICounter;
import edu.umass.nlp.utils.MapCounter;

public class TestStatistics {

	private int total;
	private int correct;
	private BufferedWriter bw; 
	private ICounter<Integer> lentotal;
	private ICounter<Integer> lencorr;
	private double tp = 0;
	private double fp = 0;
	private double fn = 0;

	public TestStatistics(String path) {

		try {

			File f = new File(path);
			if (!f.exists()) {
				
				f.createNewFile();
			}
			
			bw = new BufferedWriter(new FileWriter(f));
			lentotal = new MapCounter<Integer>();
			lencorr = new MapCounter<Integer>();

		} catch (Exception e) {

			e.printStackTrace();
		}


	}


	
	public void addDatapoint(AdjSeq as, List<String> guess) {

		List<String> truth = as.getAdjs();
		lentotal.incCount(truth.size(), 1);
		total++;

		if (truth.equals(guess)) {

			lencorr.incCount(truth.size(), 1);
			correct++;

		} else {

			try {
				
				bw.write("truth is: " + as.toString() + '\n');
				bw.write("guess is: " + guess.toString() + '\n');
				
			} catch (Exception e) {

				e.printStackTrace();
			}
		}
		
		Set<List<String>> tru = PairsGenerator.genPairs(truth);
		Set<List<String>> predicted = PairsGenerator.genPairs(guess);

		double tpdiff = computeTp(tru, predicted);
		double fpdiff = computeFp(tru, predicted);
		double fndiff = computeFn(tru, predicted);

		tp += tpdiff;
		fp += fpdiff;
		fn += fndiff;

	}

	public double getPercentageCorrect() {

		return (double)correct/total;
	}

	public String writeStatistics() {


		StringBuffer sb = new StringBuffer();

		sb.append("Number of sequences is: " + total + '\n');
		sb.append("Number of sequences correctly ordered is: " + correct + '\n');
		sb.append("Percentage correct is: " + this.getPercentageCorrect() + '\n');
		sb.append("Length statistics: \n");

		for (int i = 2; i <= 5; i++) {

			double total = lentotal.getCount(i);
			double cor = lencorr.getCount(i);
			sb.append("seq len " + i + ", total " + total + ", total correct " + cor + ", accuracy " + (double)cor/total + '\n');
		}

		String st = sb.toString();
		try {

			bw.write(st);
			bw.close();

		} catch (Exception e) {

			e.printStackTrace();
		}
		return st;

	}
	
	private double computeTp(Set<List<String>> truth, Set<List<String>> predicted) {

		Set<List<String>> truthcopy = new HashSet<List<String>>(truth);
		truthcopy.retainAll(predicted);
		return truthcopy.size();
	}

	private double computeFp(Set<List<String>> truth, Set<List<String>> predicted) {

		Set<List<String>> predictedcopy = new HashSet<List<String>>(predicted);
		predictedcopy.removeAll(truth);
		return predictedcopy.size();
	}

	private double computeFn(Set<List<String>> truth, Set<List<String>> predicted) {

		Set<List<String>> truthcopy = new HashSet<List<String>>(truth);
		truthcopy.removeAll(predicted);
		return truthcopy.size();
	}

	public double getPrecision() {

		return tp/(tp + fp);
	}

	public double getRecall() {

		return tp/(tp + fn);
	}
	
	public double getF1(double pres, double rec) {

		assert pres + rec > 0.0;
		return (2.0 * pres * rec) / (pres + rec);

	}



}

