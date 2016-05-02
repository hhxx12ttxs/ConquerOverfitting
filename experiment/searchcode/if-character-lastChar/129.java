
package FI.realitymodeler.common;

import java.io.*;

/** General string conversion filter output stream. */
public class ConvertOutputStream extends FilterOutputStream {
    protected int lastChar = -1, scanPos = 0;
    protected boolean anchored = false, character = false, toLine = false, newLine = false, needCR = false, wasCR = false, wasChar = false;
    protected String charsetName = null, scan = null, replace = null, left = null, right = null;

    /** Constructs conversion output stream.
        @param out output stream to write
        @param scan string to scan (removed when replace string is null)
        @param replace string to replace scan strings or null
        @param left characters scanned strings must follow or null
        @param right characters scanned strings must precede or null
        @param anchored scan string must occur in the beginning of line if this flag is set
        @param character individual characters are scanned and corresponding character is replaced */
    public ConvertOutputStream(OutputStream out, String scan, String replace, String left, String right, boolean anchored, boolean character) {
        super(out);
        this.scan = scan;
        this.replace = replace;
        this.left = left;
        this.right = right;
        this.anchored = anchored;
        this.character = character;
        newLine = toLine = scan.equals("\n") && anchored;
        if (anchored && left != null && left.equals("\r")) {
            needCR = true;
            this.left = null;
        }
    }

    public ConvertOutputStream(OutputStream out, String scan, String replace, String left, boolean anchored) {
        this(out, scan, replace, left, null, anchored, false);
    }

    public ConvertOutputStream(OutputStream out, String scan, String replace, boolean anchored) {
        this(out, scan, replace, null, null, anchored, false);
    }

    public ConvertOutputStream(OutputStream out, String scan, String replace) {
        this(out, scan, replace, null, null, false, false);
    }

    public ConvertOutputStream(OutputStream out, String scan) {
        this(out, scan, null, null, null, false, false);
    }

    public void setCharsetName(String charsetName) {
        this.charsetName = charsetName;
    }

    public String convert(String scan, String replace) throws IOException {
        return replace;
    }

    public String convert(char scan, char replace) throws IOException {
        return String.valueOf(replace);
    }

    public void write(int c) throws IOException {
        lastChar = c;
        boolean checking = right != null;
        if (left != null && scanPos == 0) {
            boolean isChar = left.indexOf(c) == -1;
            if (!isChar || wasChar) {
                wasChar = isChar;
                out.write(c);
                return;
            }
            wasChar = isChar;
        } else if (right != null && scanPos == scan.length()) {
            boolean isChar = right.indexOf(c) == -1;
            if (!isChar) {
                checking = false;
                c = scan.charAt(--scanPos);
            }
            wasChar = isChar;
        }
        if (toLine) {
            if (newLine) {
                if (c == '\r') {
                    wasCR = true;
                    return;
                }
                if (c == '\n') {
                    wasCR = false;
                    Support.writeBytes(out, convert(scan, replace), charsetName);
                    return;
                }
                if (wasCR) out.write('\r');
                wasCR = false;
            } else if (c == '\n' && (!needCR || wasCR)) toLine = false;
            wasCR = c == '\r';
            out.write(c);
            return;
        }
        if (character) {
            int index = scan.indexOf(c);
            if (index != -1) {
                if (replace != null) Support.writeBytes(out, convert(scan.charAt(index), replace.charAt(index)), charsetName);
                return;
            } else out.write(c);
            if (anchored) toLine = true;
            return;
        }
        if (scanPos < scan.length() && scan.charAt(scanPos) == c) {
            if (++scanPos == scan.length() && !checking) {
                scanPos = 0;
                if (anchored) toLine = true;
                if (replace != null) Support.writeBytes(out, convert(scan, replace), charsetName);
                if (right != null) out.write(lastChar);
            }
            return;
        }
        if (!anchored && left == null && right == null)
            for (int i = 1; i < scanPos; i++)
                if (scan.regionMatches(0, scan, i, scanPos - i) && scan.charAt(scanPos - i) == c) {
                    Support.writeBytes(out, scan.substring(0, i), charsetName);
                    scanPos -= i - 1;
                    return;
                }
        if (scanPos > 0) Support.writeBytes(out, scan.substring(0, scanPos), charsetName);
        if (anchored) toLine = true;
        else if (scan.charAt(0) == c) {
            scanPos = 1;
            return;
        }
        scanPos = 0;
        out.write(c);
    }

    public void write(byte b[], int off, int len) throws IOException {
        len += off;
        while (off < len) write(b[off++]);
    }

    public void close() throws IOException {
        out.close();
    }

    public int getLastChar() {
        return lastChar;
    }

}

