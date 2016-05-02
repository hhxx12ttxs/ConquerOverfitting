package bigIntegerMultiplication;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class BigIntegerMultiplication {

	// use a map so the sum associated with all
	// other shifts of equal amount can be retrieved
	public static Map<Integer, Long> simpleResult = new TreeMap<Integer, Long>();

	// do the same for the clever result
	public static Map<Integer, Long> cleverResult = new TreeMap<Integer, Long>();

	public static void main(String[] args) {

		try {
			FileWriter fstream = new FileWriter("/tmp/data.txt");
			BufferedWriter out = new BufferedWriter(fstream);
	
			// populate the array with random numbers up to 2^8-1
			// (0 to 255 or in binary 00000000 to 11111111)
			Random rn = new Random();
			for (int i = 2; i <= 1024; i *= 2) {
	
				List<Long> a = new ArrayList<Long>();
				List<Long> b = new ArrayList<Long>();
	
				for (int j = 0; j < i; j++) {
					a.add(new Long(rn.nextInt(256)));
					b.add(new Long(rn.nextInt(256)));
				}
	
				System.out.println("Size: " + a.size());
	
				long startSimple = System.currentTimeMillis();
				simple(a, b);
				long stopSimple = System.currentTimeMillis();
				System.out.println("simple: " + (stopSimple - startSimple));
	
				long startClever = System.currentTimeMillis();
				clever(a, b);
				long stopClever = System.currentTimeMillis();
				System.out.println("clever: " + (stopClever - startClever));
				
				out.write(i + "\t" + (stopSimple - startSimple) + "\t" + (stopClever - startClever) + "\n");
	
			}
	
			// Close the output stream
			out.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void simple(List<Long> a, List<Long> b) {

		for (int i = 0; i < a.size(); i++) {
			for (int j = 0; j < b.size(); j++) {

				long value = a.get(i) * b.get(j);

				// 2^8(i+j)
				int shift = (i + j);

				// add or store the value in it's "shift" element
				Long r = simpleResult.get(shift);
				if (r != null) {
					r += value;
				}
				else {
					r = value;
				}
				simpleResult.put(i + j, r);
			}
		}
	}

	public static Long clever(List<Long> i, List<Long> j) {

		// base case (we're down to one element in each array)
		if (i.size() == 1 && j.size() == 1) {
			return i.get(0) * j.get(0);
		}
		else {

			int n = i.size();
			int n2 = n / 2;

			List<Long> iLow = i.subList(0, n2);
			List<Long> iHigh = i.subList(n2, n);

			List<Long> jLow = j.subList(0, n2);
			List<Long> jHigh = j.subList(n2, n);

			Long iLjL = clever(iLow, jLow);
			Long iHjH = clever(iHigh, jHigh);

			List<Long> iHiL = add(iHigh, iLow);
			List<Long> jHjL = add(jHigh, jLow);

			Long p1 = clever(iHiL, jHjL);

			Long q1 = p1 - iHjH - iLjL;

			long result = (iHjH << 8 * n) + (q1 << 8 * n2) + iLjL;

			// store the data in the collection
			if (cleverResult.get(n) != null)
				cleverResult.put(n, iHjH + cleverResult.get(n));
			else
				cleverResult.put(n, iHjH);

			if (cleverResult.get(n2) != null)
				cleverResult.put(n, q1 + cleverResult.get(n2));
			else
				cleverResult.put(n2, q1);

			if (cleverResult.get(1) != null)
				cleverResult.put(1, iLjL + cleverResult.get(1));
			else
				cleverResult.put(1, iLjL);

			return result;

		}
	}

	public static List<Long> add(List<Long> x, List<Long> y) {

		List<Long> z = new ArrayList<Long>();

		for (int i = 0; i < x.size(); i++) {
			z.add(x.get(i) + y.get(i));
		}

		return z;
	}

}

