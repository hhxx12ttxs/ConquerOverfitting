package com.fringe81.lib.formatter.tag;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

public class YUICompressorUtils {

	// 縮小化のみ
	public static final boolean MUNGE_TRUE = true;

	public static final boolean MUNGE_FALSE = false;

	// 不要なセミコロン削除しない
	public static final boolean PRESERVE_ALL_SEMI_COLONS_TRUE = true;

	public static final boolean PRESERVE_ALL_SEMI_COLONS_FALSE = false;

	// 最適化しない
	public static final boolean DISABLE_OPTIMIZATIONS_TRUE = true;

	public static final boolean DISABLE_OPTIMIZATIONS_FALSE = false;

	// 折り返しなし
	public static final int LINEBREAKPOS_NONE = -1;

	// 詳細メッセージを出力しない
	public static final boolean VERBOSE_TRUE = false;

	public static final boolean VERBOSE_FALSE = false;

	public static String compress(String str, String charset, int lineBreakpos,
			boolean munge, boolean verbose, boolean preserveAllSemiColons,
			boolean disableOptimizations) throws IOException {
		Reader in = null;
		Writer out = null;

		try {
			in = new StringReader(str);

			JavaScriptCompressor compressor = new JavaScriptCompressor(in,
					new ErrorReporter() {

						public void warning(String message, String sourceName,
								int line, String lineSource, int lineOffset) {
							if (line < 0) {
								System.err.println("\n[WARNING] " + message);
							} else {
								System.err.println("\n[WARNING] " + line + ':'
										+ lineOffset + ':' + message);
							}
						}

						public void error(String message, String sourceName,
								int line, String lineSource, int lineOffset) {
							if (line < 0) {
								System.err.println("\n[ERROR] " + message);
							} else {
								System.err.println("\n[ERROR] " + line + ':'
										+ lineOffset + ':' + message);
							}
						}

						public EvaluatorException runtimeError(String message,
								String sourceName, int line, String lineSource,
								int lineOffset) {
							error(message, sourceName, line, lineSource,
									lineOffset);
							return new EvaluatorException(message);
						}
					});

			// Close the input stream first, and then open the output stream,
			// in case the output file should override the input file.
			in.close();
			in = null;

			out = new StringWriter();

			compressor.compress(out, lineBreakpos, munge, verbose,
					preserveAllSemiColons, disableOptimizations);

			return out.toString();
		} finally {
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
		}

	}

}

