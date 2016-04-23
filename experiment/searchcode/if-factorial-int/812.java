package leetcodeOJ.PT;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

public class PascalTriangleII {
	public List<Integer> getRow(int rowIndex) {
		List<Integer> result = new LinkedList<Integer>();

		if (rowIndex==0) {
			result.add(1);
			return result;
		}
		BigInteger[] factorial = factorial(rowIndex);
		result.add(1);
		for (int i = 1; i < rowIndex; i++) {
			result.add((factorial[rowIndex].divide((factorial[rowIndex - i]
					.multiply(factorial[i]))).intValue()));
		}
		result.add(1);
		return result;
	}

	private BigInteger[] factorial(int num) {
		BigInteger[] factorial = new BigInteger[num + 1];
		factorial[1] = BigInteger.valueOf(1);
		for (int i = 2; i < factorial.length; i++) {
			factorial[i] = factorial[i - 1].multiply(BigInteger.valueOf(i));
		}
		return factorial;
	}
}

