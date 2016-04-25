/* Statement: http://informatics.mccme.ru/moodle/mod/statements/view3.php?chapterid=3338
  Verdict: Accepted
*/
import java.math.*;
import java.util.*;
import java.io.*;
 
public class Main {
 
	public BufferedReader input;
	public StringTokenizer stoken = new StringTokenizer("");
 
	public static void main(String[] args) throws IOException {
		new Main();
	}
 
	public static BigInteger gcd(BigInteger a, BigInteger b) {
		if (a.compareTo(b) == -1) {
			BigInteger t = a;
			a = b;
			b = t;
		} 
		while (b.toString() != "0") {
			a = a.remainder(b);
			BigInteger t = a;
			a = b;
			b = t;
		} 
		return a;
	}
 
	public static BigInteger lcm(BigInteger a, BigInteger b) {
		BigInteger t = a; 
		a = a.multiply(b);
		a = a.divide(gcd(t,b));
		return a;
	}
 
	Main() throws IOException{
		input = new BufferedReader(new InputStreamReader(System.in));
		int n  = nextInt();
		BigInteger cur = new BigInteger("1");
		for (int i = 2; i <= n; i++)
			cur = lcm(cur,BigInteger.valueOf(i));
		System.out.print(cur.toString());
	}
 
	private int nextInt() throws NumberFormatException, IOException {
		return Integer.parseInt(nextString());
	}
 
	private String nextString() throws IOException {
		while(!stoken.hasMoreTokens()){
			String st = input.readLine();
			stoken = new StringTokenizer(st);
		}
		return stoken.nextToken();
	}
 
}

