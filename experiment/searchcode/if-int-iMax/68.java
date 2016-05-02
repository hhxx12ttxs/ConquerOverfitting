/**
 * 
 */
package p99;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author LG
 * 
 */
public class P99_LargestExponential {

	private static int[][] readInputs(String fileName) throws IOException {
		int[][] inputs = new int[2][1000];

		FileInputStream fis = new FileInputStream(fileName);
		InputStreamReader in = new InputStreamReader(fis, "UTF-8");
		BufferedReader br = new BufferedReader(in);

		String line = null;
		int index = 0;

		while ((line = br.readLine()) != null) {
			String[] strings = line.split(",");
			inputs[0][index] = Integer.valueOf(strings[0]);
			inputs[1][index] = Integer.valueOf(strings[1]);

			index++;
		}

		br.close();
		in.close();
		fis.close();

		return inputs;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			int[][] inputs = readInputs("bin/p99/base_exp.txt" + "");
			double max = -1;
			int imax = -1;

			for (int i = 0; i < 1000; i++) {
				double current = inputs[1][i] * Math.log(inputs[0][i]);

				if (current > max) {
					max = current;
					imax = i;
				}
			}

			System.out.println(imax + 1);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

