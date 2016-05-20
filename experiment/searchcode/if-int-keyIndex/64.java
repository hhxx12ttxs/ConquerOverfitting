/**
 * 
 */
package p59;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author LG
 * 
 */
public class P59_XORDecryption {

	private static byte[] readInputs(String fileName) throws IOException {
		FileInputStream fis = new FileInputStream(fileName);
		InputStreamReader in = new InputStreamReader(fis, "UTF-8");
		BufferedReader br = new BufferedReader(in);

		StringBuilder stringBuilder = new StringBuilder();
		String line = null;

		while ((line = br.readLine()) != null) {
			stringBuilder.append(line);
		}

		br.close();
		in.close();
		fis.close();

		String[] byteStrings = stringBuilder.toString().split(",");
		byte[] inputs = new byte[byteStrings.length];

		for (int i = 0; i < byteStrings.length; i++) {
			inputs[i] = Byte.parseByte(byteStrings[i]);
		}

		return inputs;
	}

	private static char[] findPlainText(byte[] inputs) {
		char[] key = { 'a', 'a', 'a' };

		for (key[0] = 'a'; key[0] <= 'z'; key[0]++) {
			for (key[1] = 'a'; key[1] <= 'z'; key[1]++) {
				for (key[2] = 'a'; key[2] <= 'z'; key[2]++) {
					int keyIndex = 0;
					char[] plainText = new char[inputs.length];

					for (int i = 0; i < inputs.length; i++, keyIndex = (keyIndex + 1) % 3) {
						plainText[i] = (char) (inputs[i] ^ key[keyIndex]);
					}

					if (plainText[1] == 'T' && plainText[2] == 'h'
							&& plainText[3] == 'e') {
						return plainText;
					}
				}
			}
		}

		return null;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			byte[] inputs = readInputs("bin/p59/cipher1.txt");
			char[] plainText = findPlainText(inputs);

			if (plainText != null) {
				System.out.println(String.valueOf(plainText));

				int sum = 0;

				for (int i = 0; i < plainText.length; i++) {
					sum += plainText[i];
				}

				System.out.println(sum);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
