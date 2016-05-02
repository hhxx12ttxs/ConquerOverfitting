//	Author:	Bradley Wayne Moore
package	org.mozilla.javascript;

class YUIJavaScriptCompressorErrorReporter implements ErrorReporter {
	public YUIJavaScriptCompressorErrorReporter() {

	}

	public void warning(String message, String sourceName,
			int line, String lineSource, int lineOffset) {
		if (line < 0) {
			System.err.println("\n[WARNING] " + message);
		} else {
			System.err.println("\n[WARNING] " + line + ':' + lineOffset + ':' + message);
		}
	}

	public void error(String message, String sourceName,
			int line, String lineSource, int lineOffset) {
		if (line < 0) {
			System.err.println("\n[ERROR] " + message);
		} else {
			System.err.println("\n[ERROR] " + line + ':' + lineOffset + ':' + message);
		}
	}

	public EvaluatorException runtimeError(String message, String sourceName,
			int line, String lineSource, int lineOffset) {
		error(message, sourceName, line, lineSource, lineOffset);
		return new EvaluatorException(message);
	}

}

