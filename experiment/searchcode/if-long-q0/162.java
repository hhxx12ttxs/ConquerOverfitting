package tomPack;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import tomPack.unitTest.TomEntity;

/**
 * Development utilities.
 * 
 * @author Tom Brito
 */
public class TomUtils {

	/*
	 * TODO Issue 108 - translate this file's docs to English.
	 */

	/**
	 * Returns <code>true</code> if any argument object is <code>null</code>.
	 */
	public static boolean isNull(final Object... objects) {
		for (Object o : objects) {
			if (o == null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Throws a {@link NullPointerException} if any argument object is
	 * <code>null</code>.
	 * 
	 * @param objects
	 *            - Object to check.
	 */
	public static void assertNotNull(Object... objects) {
		for (Object o : objects) {
			if (o == null) {
				throw new NullPointerException();
			}
		}
	}

	/**
	 * Copia o conte?do do buffer de entrada e grava no buffer de sa?da.
	 * 
	 * @param inputFileName
	 * @param outputFileName
	 * @return <code>true</code> se o conte?do foi copiado com sucesso. Se
	 *         ocorrer qualquer Exception, retorna <code>false</code>.
	 * 
	 * @deprecated use Apache commons-io's {@link IOUtils}.
	 */
	@Deprecated
	public static boolean copyFile(String inputFileName, String outputFileName) {

		boolean success = true;

		// TODO see:
		// http://stackoverflow.com/questions/2699209/java-io-ugly-try-finally-block
		// also very good, by the way:
		// http://stackoverflow.com/questions/341971/what-is-the-execute-around-idiom
		try {
			InputStream in = new FileInputStream(inputFileName);
			OutputStream out = new FileOutputStream(outputFileName);

			try {
				copy(in, out);
			} finally {
				try {
					in.close();
				} catch (Exception e) {
					try {
						out.close();
					} catch (Exception e2) {
					}
					throw e;
				}
				out.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
			success = false;
		}

		return success;

	}

	private static int copy(InputStream input, OutputStream output)
			throws IOException {
		long count = copyLarge(input, output);
		if (count > Integer.MAX_VALUE) {
			return -1;
		}
		return (int) count;
	}

	private static long copyLarge(InputStream input, OutputStream output)
			throws IOException {
		byte[] buffer = new byte[1024 * 4];
		long count = 0;
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}

	/**
	 * Prefira usar {@link #sameValues(WEntity, WEntity)}.<br>
	 * Retorna true se dois objetos forem iguais (.equals()) ou ambos nulos.
	 * Tamb?m funciona com primitivos como parametro.
	 * 
	 * @see #sameValues(WEntity, WEntity)
	 * @param o1
	 * @param o2
	 * @return
	 * 
	 */
	public static boolean equals(Object o1, Object o2) {
		return (o1 == null) ? (o2 == null) : o1.equals(o2);
	}

	/**
	 * Retorna true se os parametros forem iguais. Mais formalmente, retorna
	 * true se ambos forem nulos ou se e1.sameValue(e2) retornar true.
	 * 
	 * @param e1
	 * @param e2
	 * @return
	 * @deprecated use {@link #equals(Object, Object)}.
	 */
	@Deprecated
	public static boolean sameValues(TomEntity e1, TomEntity e2) {

		// if one is null, the other needs to be too.
		if ((e1 == null) || (e2 == null)) {
			return ((e1 == null) && (e2 == null));
		}

		if (e1.getClass() != e2.getClass()) {
			return false;
		}

		return e1.sameValues(e2);

	}

	/**
	 * Search for one, and only one, mask in value. For it, this method use the
	 * regex methods {@link Pattern} and {@link Matcher}.
	 * 
	 * @param mask
	 *            - mask to search at value
	 * @param value
	 *            - value to search the mask
	 * @return <code>true</code> if find one time the mask at the value.
	 */
	public static boolean find(String mask, String value) {
		Pattern p = Pattern.compile(mask); // e.g. I0.0, Q0.0
		Matcher m = p.matcher(value);
		return m.find();
	}

	public static String readFile(String filename) throws IOException {
		String line = null;
		StringBuilder stringBuilder = new StringBuilder();
		String lineSeparator = System.getProperty("line.separator"); //$NON-NLS-1$

		BufferedReader reader = new BufferedReader(new FileReader(filename));
		try {
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
				stringBuilder.append(lineSeparator);
			}
		} finally {
			reader.close();
		}

		return stringBuilder.toString();
	}

}

