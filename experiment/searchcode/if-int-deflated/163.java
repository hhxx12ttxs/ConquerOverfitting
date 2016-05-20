package org.itx.jbalance;

import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.Log4JLogger;


/**
 * Input/output utility methods.
 *
 */
public class Io {
    private static final Log log = new Log4JLogger(Io.class.getName());

    /** The Constant DEFAULT_BUF_SIZE. */
    public static final int DEFAULT_BUF_SIZE = 8192;

    protected Io() {
        // hide default constructor for classes containing only static methods
    }

    public static String getFilename(String canonicalPath) {
    	 if(canonicalPath == null || canonicalPath.isEmpty()) {
            return null;
        }

        int winPathSeparator = canonicalPath.lastIndexOf("\\");
        int unixPathSeparator = canonicalPath.lastIndexOf("/");
        if(winPathSeparator < 0) {
            // No Windows path separator...
            if(unixPathSeparator < 0) {
                // No unix path separator.  Return the original filename.
                return canonicalPath;
            } else {
                // Unix path separator found.  Return the part after the last
                // separator.
                return canonicalPath.substring(unixPathSeparator + 1);
            }
        } else {
            // Windows path separator found.  Return the part after the last
            // separator.
            return canonicalPath.substring(winPathSeparator + 1);
        }
    }

    /**
     * @param closeable can be null or already closed.
     */
    public static void closeQuietly(Closeable... closeable){
        if(null == closeable){
            return;
        }
        for(Closeable toClose:closeable){
            if (toClose != null) {
                try {
                    toClose.close();
                } catch (IOException e) {
                    log.warn("close(closeable=...) [1] Error closing output stream", e);
                }
            } else {
                log.debug("close(closeable=...) [2] skipping null element");
            }
        }
    }

    public static long copyToStream(File file, OutputStream outputStream) throws IOException {
        if(file == null) {
            throw new IllegalArgumentException("File is required");
        }

        if(outputStream == null) {
            throw new IllegalArgumentException("Output stream is required");
        }

        InputStream in = null;
        try {
            in = new FileInputStream(file);
            return copyStream(in, outputStream);
        } finally {
            closeQuietly(in);
        }
    }

    /**
     * Copy stream.
     *
     * @param in
     *            the in
     * @param out
     *            the out
     * @return the long
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static long copyStream(InputStream in, OutputStream out) throws IOException {
        return copyStream(in, out, -1, -1, DEFAULT_BUF_SIZE);
    }

    /**
     * Copy stream.
     *
     * @param in
     *            the in
     * @param out
     *            the out
     * @param from
     *            the from
     * @param to
     *            the to
     * @return the long
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static long copyStream(InputStream in, OutputStream out, int from, int to) throws IOException {
        return copyStream(in, out, from, to, DEFAULT_BUF_SIZE);
    }

    /**
     * Copy stream.
     *
     * @param in
     *            the in
     * @param out
     *            the out
     * @param from
     *            the from
     * @param to
     *            the to
     * @param bufSize
     *            the buf size
     * @return the long
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static long copyStream(InputStream in, OutputStream out, int from, int to, int bufSize) throws IOException {
        if(in == null) {
            throw new IllegalArgumentException("Input stream is required");
        }

        if(out == null) {
            throw new IllegalArgumentException("Output stream is required");
        }

        if (from > 0 && in.skip(from) < from) {
            // EOF
            return 0;
        }
        byte[] buf = new byte[bufSize];
        int copyLen = to - from + 1;
        int copied = 0;
        int readLen = 0;
        while ((readLen = in.read(buf, 0, bufSize)) > 0) {
            int writeLen = readLen;
            if (to > 0) {
                writeLen = Math.min(writeLen, copyLen - copied);
            }
            out.write(buf, 0, writeLen);
            copied += writeLen;
        }
        return copied;
    }

    /**
     * Copy stream.
     *
     * @param in
     *            the in
     * @param out
     *            the out
     * @return the long
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static long copyStream(Reader in, Writer out) throws IOException {
        return copyStream(in, out, -1, -1, DEFAULT_BUF_SIZE);
    }

    /**
     * Copy stream.
     *
     * @param in
     *            the in
     * @param out
     *            the out
     * @param from
     *            the from
     * @param to
     *            the to
     * @return the long
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static long copyStream(Reader in, Writer out, int from, int to) throws IOException {
        return copyStream(in, out, from, to, DEFAULT_BUF_SIZE);
    }

    /**
     * Copy stream.
     *
     * @param in
     *            the in
     * @param out
     *            the out
     * @param from
     *            the from
     * @param to
     *            the to
     * @param bufSize
     *            the buf size
     * @return the long
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static long copyStream(Reader in, Writer out, int from, int to, int bufSize) throws IOException {
        if(in == null) {
            throw new IllegalArgumentException("Input reader is required");
        }

        if(out == null) {
            throw new IllegalArgumentException("Output writer is required");
        }

        if (from > 0 && in.skip(from) < from) {
            // EOF
            return 0;
        }
        char[] buf = new char[bufSize];
        int copyLen = to - from + 1;
        int copied = 0;
        int readLen = 0;
        while ((readLen = in.read(buf, 0, bufSize)) > 0) {
            int writeLen = readLen;
            if (to > 0) {
                writeLen = Math.min(writeLen, copyLen - copied);
            }
            out.write(buf, 0, writeLen);
            copied += writeLen;
        }
        out.flush();
        return copied;
    }

    /**
     * Copy file.
     *
     * @param source
     *            the source
     * @param destination
     *            the destination
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static void copyFile(File source, File destination) throws IOException {
        if(source == null) {
            throw new IllegalArgumentException("Source file is required");
        }

        if(destination == null) {
            throw new IllegalArgumentException("Destination file is required");
        }

        FileInputStream in = null;
        FileOutputStream out = null;
        try{
            in = new FileInputStream(source);
            out = new FileOutputStream(destination);
            copyStream(in, out);
        } finally {
            closeQuietly(in,out);
        }
    }

    /**
     * Read as bytes.
     *
     * @param file
     *            the file
     * @return the byte[]
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static byte[] readAsBytes(File file) throws IOException {
        if(file == null) {
            throw new IllegalArgumentException("File is required");
        }

        FileInputStream in = new FileInputStream(file);
        ByteArrayOutputStream out = new ByteArrayOutputStream((int) file.length());
        try{
            copyStream(in, out);
            return out.toByteArray();
        } finally {
            closeQuietly(in,out);
        }


    }

    /**
     * Read as bytes.
     *
     * @param in
     *            the in
     * @return the byte[]
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static byte[] readAsBytes(InputStream in) throws IOException {
        if(in == null) {
            throw new IllegalArgumentException("Input stream is required");
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try{
            copyStream(in, out);
            return out.toByteArray();
        } finally {
            closeQuietly(out);
        }
    }

    /**
     * Read as bytes.
     *
     * @param in
     *            the in
     * @param initialSize
     *            the initial size
     * @return the byte[]
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static byte[] readAsBytes(InputStream in, int initialSize) throws IOException {
        if(in == null) {
            throw new IllegalArgumentException("Input stream is required");
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream(initialSize);
        try{
            copyStream(in, out);
            return out.toByteArray();
        } finally {
            closeQuietly(out);
        }
    }

    /**
     * Read as string.
     *
     * @param file
     *            the file
     * @return the string
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static String readAsString(File file) throws IOException {
        if(file == null) {
            throw new IllegalArgumentException("File is required");
        }

        FileReader in = null;
        StringWriter out = null;
        try{
            in = new FileReader(file);
            out = new StringWriter((int) file.length());
            copyStream(in, out);
            return out.toString();
        } finally {
            closeQuietly(in,out);
        }
    }

    /**
     * Read as string.
     *
     * @param in
     *            the in
     * @return the string
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static String readAsString(Reader in) throws IOException {
        if(in == null) {
            throw new IllegalArgumentException("Input reader is required");
        }

        StringWriter out = null;
        try {
            out = new StringWriter();
            copyStream(in, out);
            return out.toString();
        } finally {
            closeQuietly(out);
        }
    }

    /**
     * Read as string.
     *
     * @param in
     *            the in
     * @param initialSize
     *            the initial size
     * @return the string
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static String readAsString(Reader in, int initialSize) throws IOException {
        if(in == null) {
            throw new IllegalArgumentException("Input reader is required");
        }

        StringWriter out = null;
        try{
            out = new StringWriter(initialSize);
            copyStream(in, out);
            return out.toString();
        } finally {
            closeQuietly(out);
        }
    }

    /**
     * Zip.
     *
     * @param src
     *            the src
     * @param dst
     *            the dst
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static void zip(File src, File dst) throws IOException {
        zip(src, dst, src.getName());
    }

    /**
     * Zip.
     *
     * @param src
     *            the src
     * @param dst
     *            the dst
     * @param entryName
     *            the entry name
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static void zip(File src, File dst, String entryName) throws IOException {
        if(src == null) {
            throw new IllegalArgumentException("Source file is required");
        }

        if(dst == null) {
            throw new IllegalArgumentException("Destination file is required");
        }

        if(entryName == null || entryName.isEmpty()) {
            throw new IllegalArgumentException("Entry name is required");
        }

        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            in = new FileInputStream(src);
            out = new FileOutputStream(dst);
            ZipEntry entry = new ZipEntry(entryName);
            entry.setMethod(ZipOutputStream.DEFLATED);
            entry.setTime(System.currentTimeMillis());
            doZip(entry, in, out);
        } finally {
            closeQuietly(in,out);
        }
    }

    /**
     * Zip.
     *
     * @param in
     *            the in
     * @param out
     *            the out
     * @param entryName
     *            the entry name
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static void zip(InputStream in, OutputStream out, String entryName) throws IOException {
        if(in == null) {
            throw new IllegalArgumentException("Input stream is required");
        }

        if(out == null) {
            throw new IllegalArgumentException("Output stream is required");
        }

        if(entryName == null || entryName.isEmpty()) {
            throw new IllegalArgumentException("Entry name is required");
        }

        ZipEntry entry = new ZipEntry(entryName);
        entry.setMethod(ZipOutputStream.DEFLATED);
        entry.setTime(System.currentTimeMillis());
        doZip(entry, in, out);
    }

    private static void doZip(ZipEntry entry, InputStream in, OutputStream out) throws IOException {
        ZipOutputStream zipOut = new ZipOutputStream(out);
        zipOut.setMethod(ZipOutputStream.DEFLATED);
        zipOut.setLevel(9);
        zipOut.putNextEntry(entry);
        copyStream(in, zipOut);
        zipOut.closeEntry();
        zipOut.finish();
    }

    /**
     * Checks if is zipped.
     *
     * @param buffer
     *            the buffer
     * @return true, if is zipped
     */
    public static boolean isZipped(byte[] buffer) {
        if(buffer == null) {
            throw new IllegalArgumentException("Buffer is required");
        }

        if (buffer.length < 4) {
            return false;
        }
        // Little endian in ZIP files...
        long signature = (long) (buffer[3] << 24) | (long) ((buffer[2] & 0xFF) << 16) | ((buffer[1] & 0xFF) << 8) | (buffer[0] & 0xff);
        return signature == ZipInputStream.LOCSIG;
    }

    /**
     * Checks if is zipped.
     *
     * @param in
     *            the in
     * @return true, if is zipped
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static boolean isZipped(InputStream in) throws IOException {
        if(in == null) {
            throw new IllegalArgumentException("Input stream is required");
        }

        if (!in.markSupported()) {
            throw new UnsupportedOperationException("The specified InputStream does not support marking");
        }
        // Mark the stream and allow reading of up to 4 bytes
        in.mark(4);
        byte[] signatureBytes = new byte[4];
        if (in.read(signatureBytes) < 4) {
            return false;
        }
        // Reset back to the mark point
        in.reset();
        return isZipped(signatureBytes);
    }

    public static String stripReservedCharacters(String filename) {
    	if(filename == null || filename.isEmpty()) {
            return filename;
        }

        char[] reservedCharacters = new char[] {'/', '\\', '?', '%', '*', ':', '<', '>', '.'};
        StringBuilder reservedCharacterRegex = new StringBuilder("[");
        for(char reservedCharacter: reservedCharacters) {
            reservedCharacterRegex.append('\\');
            reservedCharacterRegex.append(reservedCharacter);
        }
        reservedCharacterRegex.append("]");

        return filename.replaceAll(reservedCharacterRegex.toString(), "");
    }

    public static String getStackTraceString(Throwable t) {
        StringWriter sw = new StringWriter(t.getStackTrace().length * 100);
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }

    public static Dimension getImageDimensions(byte[] image) {
        ImageInputStream in = null;
        Dimension dimension = null;
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(image);
            in = ImageIO.createImageInputStream(bais);
            final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                try {
                    reader.setInput(in);
                    dimension = new Dimension(reader.getWidth(0), reader.getHeight(0));
                } finally {
                    reader.dispose();
                }
            }
        } catch (IOException e) {
            log.info(": error while getting image dimensions", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.warn("close() [1] Error closing input stream", e);
                }
            }
        }
        return dimension;
    }
}

