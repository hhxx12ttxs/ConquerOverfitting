public class IndentFormatter {

public static void printFormatted(String prefix, String str, int termWidth, PrintStream out, boolean prefixFirstLine) {
int pfxLen = prefix.length();
int maxwidth = termWidth - pfxLen;

