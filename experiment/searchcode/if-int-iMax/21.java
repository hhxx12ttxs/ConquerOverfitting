/**
 * 
 */
package p98;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.TreeSet;

/**
 * @author Son-Huy TRAN
 * 
 */
public class P98_AnagramicSquares {

	private static ArrayList<String> readInputs(String fileName)
			throws NumberFormatException, IOException {

		ArrayList<String> grid = new ArrayList<String>();
		FileInputStream fis = new FileInputStream(fileName);
		InputStreamReader in = new InputStreamReader(fis, "UTF-8");
		BufferedReader br = new BufferedReader(in);

		String buffer = null;

		// read the inputs
		while ((buffer = br.readLine()) != null) {
			String[] tokens = buffer.split(",");

			if (tokens.length > 0) {
				for (String token : tokens) {
					token = token.replace("\"", "");
					grid.add(token);
				}
			}
		}

		br.close();
		in.close();
		fis.close();

		return grid;
	}

	private static boolean isValidDigits(String string, int[] digits) {
		for (int i = 0; i < string.length() - 1; i++) {
			for (int j = i + 1; j < digits.length; j++) {
				if ((string.charAt(i) == string.charAt(j) && digits[i] != digits[j])
						|| (digits[i] == digits[j] && string.charAt(i) != string
								.charAt(j))) {
					return false;
				}
			}
		}

		return true;
	}

	private static int tryAnagrams(ArrayList<String> anagrams,
			ArrayList<Integer> squareAnagrams) {
		int max = -1;

		String firstWord = anagrams.get(0);

		// corresponding the first string with each square
		for (int j = 0; j < squareAnagrams.size(); j++) {
			int[] digits = new int[firstWord.length()];
			int temp = squareAnagrams.get(j);
			int digitIndex = digits.length - 1;

			while (temp > 0) {
				digits[digitIndex] = temp % 10;
				temp /= 10;
				digitIndex--;
			}

			if (isValidDigits(firstWord, digits)) {
				// generates the other numbers
				TreeSet<Integer> otherNumbers = new TreeSet<Integer>();
				int[] charNumbers = new int[26];

				for (int k = 0; k < firstWord.length(); k++) {
					char c = firstWord.charAt(k);
					charNumbers[c - 'A'] = digits[k];
				}

				boolean found = true;

				for (int k = 1; k < anagrams.size(); k++) {

					String word = anagrams.get(k);
					int tempNumber = 0;

					for (int l = 0; l < word.length(); l++) {
						int newDigit = charNumbers[word.charAt(l) - 'A'];
						tempNumber = tempNumber * 10 + newDigit;
					}

					if (!squareAnagrams.contains(tempNumber)) {
						found = false;
						break;
					}

					otherNumbers.add(tempNumber);
				}

				if (found) {
					int maxTemp = otherNumbers.last();

					if (maxTemp > max) {
						max = maxTemp;
					}
				}
			}
		}

		return max;
	}

	private static int anagramicSquare(ArrayList<String> anagrams,
			ArrayList<ArrayList<Integer>> squareAnagrams) {
		int max = -1;

		for (int i = 0; i < squareAnagrams.size(); i++) {
			int numberLength = squareAnagrams.get(i).get(0).toString().length();
			int stringLength = anagrams.get(0).length();

			if (numberLength == stringLength) {
				int found = tryAnagrams(anagrams, squareAnagrams.get(i));

				if (found > max) {
					max = found;
				}
			} else if (numberLength > stringLength) {
				break;
			}
		}

		System.out.println(anagrams.get(0) + " - " + max);
		return max;
	}

	private static boolean isAnagram(String string1, String string2) {
		if (string1.length() != string2.length()) {
			return false;
		}

		int[] charCount = new int[26];

		for (int i = 0; i < string1.length(); i++) {
			char c1 = string1.charAt(i);
			char c2 = string2.charAt(i);

			charCount[c1 - 'A']++;
			charCount[c2 - 'A']--;
		}

		for (int i = 0; i < charCount.length; i++) {
			if (charCount[i] != 0) {
				return false;
			}
		}

		return true;
	}

	private static ArrayList<ArrayList<String>> findAnagrams(
			ArrayList<String> inputs) {
		ArrayList<ArrayList<String>> result = new ArrayList<>();

		for (String string : inputs) {
			boolean hasAnagram = false;

			for (ArrayList<String> arrayList : result) {
				if (isAnagram(string, arrayList.get(0))) {
					arrayList.add(string);
					hasAnagram = true;
					break;
				}
			}

			if (!hasAnagram) {
				ArrayList<String> newList = new ArrayList<String>();
				newList.add(string);
				result.add(newList);
			}
		}

		for (int i = 0; i < result.size(); i++) {
			if (result.get(i).size() < 2) {
				result.remove(i);
				i--;
			}
		}

		return result;
	}

	private static boolean isPermutation(int number1, int number2) {
		int[] digits = new int[10];

		while (number1 > 0 && number2 > 0) {
			int digit1 = number1 % 10;
			number1 /= 10;

			int digit2 = number2 % 10;
			number2 /= 10;

			digits[digit1]++;
			digits[digit2]--;
		}

		if (number1 > 0 || number2 > 0) {
			return false;
		}

		for (int i = 0; i < digits.length; i++) {
			if (digits[i] != 0) {
				return false;
			}
		}

		return true;
	}

	private static ArrayList<ArrayList<Integer>> findPermutations(
			int[] squares, int iMin, int iMax) {
		ArrayList<ArrayList<Integer>> result = new ArrayList<>();

		for (int i = iMin; i <= iMax; i++) {
			boolean hasAnagram = false;

			for (ArrayList<Integer> arrayList : result) {
				if (isPermutation(squares[i], arrayList.get(0))) {
					arrayList.add(squares[i]);
					hasAnagram = true;
					break;
				}
			}

			if (!hasAnagram) {
				ArrayList<Integer> newList = new ArrayList<>();
				newList.add(squares[i]);

				result.add(newList);
			}
		}

		for (int i = 0; i < result.size(); i++) {
			if (result.get(i).size() < 2) {
				result.remove(i);
				i--;
			}
		}

		return result;
	}

	private static ArrayList<ArrayList<Integer>> permutationSquares() {
		ArrayList<ArrayList<Integer>> result = new ArrayList<>();
		int[] squares = new int[31619];

		for (int i = 4, j = 0; i <= 31622; i++, j++) {
			squares[j] = i * i;
		}

		int temp = 10, i = 0, iMin = 0, iMax = 0;

		do {
			int squareMin = temp;
			int squareMax = temp * 10 - 1;

			while (i < squares.length && squares[i] < squareMin) {
				i++;
			}

			iMin = i;

			while (i < squares.length && squares[i] <= squareMax) {
				i++;
			}

			iMax = i - 1;

			if (iMin < squares.length && iMax < squares.length) {
				result.addAll(findPermutations(squares, iMin, iMax));
			}

			i = iMax + 1;
			temp *= 10;
		} while (i < squares.length);

		return result;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			ArrayList<String> inputs = readInputs("bin/p98/words.txt");
			ArrayList<ArrayList<String>> anagrams = findAnagrams(inputs);
			System.out.println(anagrams);
			ArrayList<ArrayList<Integer>> squareAnagrams = permutationSquares();

			int max = -1;

			for (ArrayList<String> anagram : anagrams) {
				int i = anagramicSquare(anagram, squareAnagrams);

				if (i > max) {
					max = i;
				}
			}

			System.out.println(max);
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
	}
}

