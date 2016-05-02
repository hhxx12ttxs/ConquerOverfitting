
package jweslley.ContestVolumes.VolumeCXIV;

import java.util.Scanner;

import jweslley.Problem;
import jweslley.Problem.Status;

/**
 * http://icpcres.ecs.baylor.edu/onlinejudge/external/114/11417.html
 *
 * @author  Jonhnny Weslley
 * @version 1.00, 19/10/2008
 */
@Problem(Status.Accepted)
public class GCD {

	public static void main(String[] args) {
		StringBuilder out = new StringBuilder();
		Scanner in = new Scanner(System.in);
		int n, result;
		while (in.hasNext()) {
			n = in.nextInt();
			if (n == 0) {
				break;
			}
			result = 0;
			for (int i = 1; i < n; i++)
				for (int j = i + 1; j <= n; j++) {
					result += gcd(i, j);
				}
			out
			.append(result)
			.append('\n');
		}
		System.out.print(out);
	}
	
	static int gcd(int n1, int n2) {
		int tmp;
		do {
			if (n1 < n2) {
				tmp = n1;
	            n1 = n2;
	            n2 = tmp;
			}
			n1 = n1 % n2;
		} while (n1 > 0);
		return n2;
	}

}

