import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
//import java.math.BigInteger;
import java.util.Arrays;

public class Runs {
	public static void main(String[] args) throws IOException {
		Runs r = new Runs(450000, 100);
		r.run("A-large.in", "A-large.out");
	}
	
	public void run(String input, String output) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(input));
		FileWriter fw = new FileWriter(output);
		int N = new Integer(br.readLine());
		for (int cases = 1; cases <= N; cases++) {
			String s = br.readLine();
			long result = process(s);
			fw.write("Case #" + cases + ": " + result + "\n");
			System.out.println("Case #" + cases + ": " + result);
		}
		fw.flush();
		fw.close();
	}
	
	public long process(String s) throws IOException {
		int[] charCount = new int[26];
		int runGoal = 1;
		int totalChars = 0;
		charCount[s.charAt(0) - 'a']++;
		for (int i = 1; i < s.length(); i++) {
			if (s.charAt(i) != s.charAt(i - 1))
				runGoal++;
			charCount[s.charAt(i) - 'a']++;			
		}
		long[] runCount = new long[runGoal + 1];
		long[] previousRunCount = new long[runGoal + 1];
		runCount[0] = 1;
		for (int i = 0; i < 26; i++) {
			if (charCount[i] > 0) {
				System.arraycopy(runCount, 0, previousRunCount, 0, runCount.length);
				for (int j = 0; j < runCount.length; j++)
					runCount[j] = 0;
				for (int r1 = 0; r1 <= runGoal; r1++) {
					if (previousRunCount[r1] > 0) {
						for (int r2 = r1 + 1; r2 <= runGoal; r2++) {
							long ways = countTransitions(totalChars, charCount[i], r1, r2);
							assert(ways >= 0);
							runCount[r2] += ways * previousRunCount[r1];
							runCount[r2] %= 1000003;
							assert(runCount[r2] >= 0);
						}
					}
				}
				totalChars += charCount[i];
			}
		}
		return runCount[runGoal];
	}
	
	public static long factorial(int x) {
		long result = 1;
		while (x > 0) {
			result *= x;
			x--;
		}
		return result;
	}
	
	/*
	 * choose() function implemented by array
	 */
	private long[][] tables;
	
	private Runs(int totalChars, int runs) {
		tables = new long[totalChars + 1][runs + 1];
		long[] array = new long[runs + 1];
		long previousValue, currentValue;
		Arrays.fill(array, 0);
		array[0] = 1;
		array[1] = 1;
		for (int i = 2; i <= totalChars; i++) {
			previousValue = array[0];
			for (int j = 1; j <= runs; j++) {
				currentValue = array[j];
				long value = (previousValue + currentValue) % 1000003;
				array[j] = value;
				previousValue = currentValue;
			}
			if (i <= runs)
				array[i] = 1;
			for (int k = 0; k <= runs; k++) {
				tables[i][k] = array[k];
			}
		}
	}

	public long choose(int x, int y) {
		if (y < 0 || y > x) return 0;
		if (y == 0 || y == x) return 1;
		if (y == 1) return x;
		return tables[x][y];
	}

	/*
	 * choose() function implemented by BigInteger
	 */
	/*
	public static long choose(int x, int y) {
		if (y < 0 || y > x) return 0;
		if (y == 0 || y == x) return 1;
		
		BigInteger answer = BigInteger.ONE;
		for (int i = x - y + 1; i <= x; i++) {
			answer = answer.multiply(BigInteger.valueOf(i));
		}
		for (int j = 1; j <= y; j++) {
			answer = answer.divide(BigInteger.valueOf(j));
		}
		return answer.mod(new BigInteger("1000003")).longValue();
	}
	*/
	
	public long countTransitions(int totalChars, int charCount, int currentRun, int targetRun) throws IOException {
		long result = 0;
		if (currentRun == 0)
			return (targetRun == 1)? 1 : 0;
		for (int y = 0; currentRun + 2 * y <= targetRun; y++) {
			int x = targetRun - (currentRun + 2 * y);
			assert(x >= 0);
			assert(choose(currentRun + 1, x) >= 0);
			assert(choose(totalChars - currentRun, y) >= 0);
			assert(choose(charCount - 1, x + y - 1) >= 0);
			result += choose(currentRun + 1, x) * choose(totalChars - currentRun, y) * choose(charCount - 1, x + y - 1);
			result %= 1000003;
		}
		return result;
	}
}

