
package FI.realitymodeler;

import FI.realitymodeler.common.*;
import java.applet.*;
import java.io.*;
import java.math.*;
import java.net.*;
import java.security.*;
import java.text.*;
import java.util.*;
import java.util.zip.*;
import javax.servlet.*;
import javax.servlet.http.*;

class CacheCleaning extends Thread {
    W3File dir;
    ServletContext servletContext;
    long expirationTime;

    CacheCleaning(ServletContext servletContext, W3File dir, long expirationTime) {
        this.servletContext = servletContext;
        this.dir = dir;
        this.expirationTime = expirationTime;
    }

    int cleanCache(W3File dir)
        throws IOException {
        W3File file;
        int nFiles = 0;
        String list[] = dir.list();
        for (int i = 0; i < list.length; i++)
            if ((file = new W3File(dir, list[i])).isDirectory()) {
                nFiles += cleanCache(file);
                if (file.list().length == 0 && file.lastAccess() < expirationTime) {
                    file.setReadAndWrite();
                    file.delete();
                    nFiles++;
                }
            } else if (file.lastAccess() < expirationTime) {
                file.setReadAndWrite();
                file.delete();
                nFiles++;
            }
        return nFiles;
    }

    public void run() {
        try {
            int nFiles = cleanCache(dir);
            W3URLConnection.log(servletContext, nFiles + " files in cache " + dir.getPath() + " removed.");
        } catch (Exception ex) {
            W3URLConnection.log(servletContext, getClass().getName(), ex);
        }
    }

}

class CacheFillingThread extends PoolThread {
    W3URLConnection uc;
    HeaderList headerList, requestHeaderFields;
    InputStream source;
    W3File cacheFile;
    PipedInputStream in;
    PipedOutputStream out;
    boolean cacheHeaders;
    boolean fillCaches;
    boolean useCaches;
    int contentLength;
    long lastModified;

    CacheFillingThread(W3URLConnection uc) {
        super(W3URLConnection.cacheGroup, Thread.currentThread().getName());
        if (uc != null) setup(uc);
    }

    public Object clone() {
        return new CacheFillingThread(null);
    }

    public void setup(W3URLConnection uc) {
        this.uc = uc;
        this.headerList = uc.getHeaderList();
        this.requestHeaderFields = uc.getRequestHeaderFields();
        this.source = uc.in;
        this.cacheFile = uc.cacheFile;
        this.cacheHeaders = uc.cacheHeaders;
        this.fillCaches = uc.fillCaches;
        this.useCaches = uc.getUseCaches();
        if (uc.getDoOutput() || !uc.isConnected()) return;
        this.contentLength = uc.getContentLength();
        this.lastModified = uc.getLastModified();
        if (this.lastModified == 0L) this.lastModified = System.currentTimeMillis();
    }

    public void run() {
        byte b[] = new byte[Support.bufferLength];
        while (!isInterrupted())
            try {
                boolean multiple = uc instanceof W3HttpURLConnection ||
                    uc instanceof W3FtpURLConnection || uc.usingProxy(),
                    append = multiple && useCaches && cacheFile.isOffline();
                long length = 0L;
                if (append) {
                    // sending part of file already in cache to the client
                    length = cacheFile.length();
                    if (!fillCaches) {
                        BufferedInputStream fin = new BufferedInputStream(new FileInputStream(cacheFile));
                        long l = Math.min(uc.rangeStart, length);
                        try {
                            for (int n; l > 0 && (n = fin.read(b, 0, l < b.length ? (int)l : b.length)) > 0; l -= n);
                            if (uc.rangeStart < length)
                                for (int n; (n = fin.read(b)) > 0;)
                                    out.write(b, 0, n);
                        } finally {
                            fin.close();
                        }
                    }
                } else length = uc.rangeStart;
                int repeats = 0;
                BufferedOutputStream fout = null;
                if (useCaches) {
                    fout = new BufferedOutputStream(new FileOutputStream(cacheFile.getPath(), append));
                    if (!append) cacheFile.setOffline();
                }
                long lastLength = length;
                for (boolean continued = false;; continued = true)
                    try {
                        lastLength = length;
                        if (continued) {
                            // retrying to get rest of entity data
                            if (uc.verbose) uc.log("Retrying to get entity");
                            if (!uc.mayKeepAlive) uc.close();
                            uc.clear();
                            uc.rangeStart = length;
                            if (uc.rangeStart > 0L) uc.setRequestProperty("Range", "bytes=" + uc.rangeStart + "-");
                            uc.unsetRequestProperty("if-modified-since");
                            uc.unsetRequestProperty("if-none-match");
                            source = uc.getInput(true);
                        }
                        LimitInputStream limitIn = uc.limitIn;
                        if (uc.rangeStartReceived < uc.rangeStart) {
                            long l = uc.rangeStart - uc.rangeStartReceived;
                            for (int n; l > 0 && (n = source.read(b, 0, l < b.length ? (int)l : b.length)) > 0; l -= n);
                        }
                        for (boolean done = false; !done;) {
                            // Sending body data received from remote server to the client and writing it to cache file
                            done = true;
                            if (fillCaches)
                                for (int n; (n = source.read(b)) > 0;) {
                                    fout.write(b, 0, n);
                                    length += (long)n;
                                }
                            else for (int n; (n = source.read(b)) > 0;)
                                     try {
                                         out.write(b, 0, n);
                                     } catch (IOException ex) {
                                         if (uc.verbose) uc.log(ex.toString(), ex);
                                         if (!uc.autoCaches || !useCaches) {
                                             multiple = false;
                                             throw ex;
                                         }
                                         fillCaches = true;
                                         done = false;
                                         try {
                                             if (in != null)
                                                 synchronized (in) {
                                                     in.close();
                                                     in.notifyAll();
                                                 }
                                         } catch (IOException ex1) {}
                                         break;
                                     } finally {
                                         // In case only auto resume is used, data is not written to cache file
                                         if (useCaches) fout.write(b, 0, n);
                                         length += (long)n;
                                     }
                        }
                        // If remote server has closed the connection and there should be still data, exception must be signalled
                        if (uc.chunkedIn != null && uc.chunkedIn.hasLeft() || uc.limitIn != null && uc.limitIn.getRemaining() > 0) throw new SocketException("Unexpected end of stream");
                        break;
                    } catch (Exception ex) {
                        if (uc.verbose) uc.log(ex.toString(), ex);
                        if (ex instanceof SocketException && multiple) {
                            if (contentLength == -1 || length < contentLength && (length > lastLength || ++repeats <= uc.numberOfRepeats)) {
                                append = true;
                                continue;
                            }
                        }
                        if (uc.verbose) uc.log("Interrupting cache fill");
                        // marking cache file to be incomplete
                        if (useCaches) {
                            fout.close();
                            if (length > 0L && cacheFile.setOffline()) {
                                cacheFile.setLastTimes(System.currentTimeMillis(), lastModified);
                                cacheFile.setReadOnly();
                            } else cacheFile.delete();
                        }
                        throw ex;
                    }
                if (useCaches) {
                    fout.close();
                    cacheFile.setLastTimes(System.currentTimeMillis(), lastModified);
                    if (contentLength != -1 && length < contentLength)
                        cacheFile.setOffline();
                    else cacheFile.setOnline();
                    cacheFile.setReadOnly();
                }
                if (!fillCaches) out.close();
            } catch (Exception ex) {
                try {
                    if (in != null)
                        synchronized (in) {
                            in.close();
                            in.notifyAll();
                        }
                } catch (IOException ex1) {} finally {
                    uc.log(getClass().getName(), ex);
                }
            } finally {
                if (fillCaches) uc.disconnect();
                if (W3URLConnection.cachePool != null)
                    try {
                        if (W3URLConnection.cachePool.threadPool.release(this)) break;
                    } catch (InterruptedException ex) {
                        break;
                    }
                else break;
            }
    }

    public final void writeBytes(String str)
        throws IOException {
        Support.writeBytes(out, str, uc.charsetName);
    }

}

class CacheFillingInputStream extends InputStream {
    BufferedInputStream fin = null;
    BufferedOutputStream fout = null;
    W3URLConnection uc;
    HeaderList headerList, requestHeaderFields;
    InputStream source;
    W3File cacheFile;
    boolean append;
    boolean cacheHeaders;
    boolean multiple;
    boolean useCaches;
    boolean eof = false;
    int contentLength;
    int repeats;
    long lastModified;
    long length;
    long l;
    long lastLength;
    byte b[] = new byte[Support.bufferLength];
    byte temp[] = new byte[1];

    CacheFillingInputStream(W3URLConnection uc)
        throws IOException {
        if (uc != null) setup(uc);
    }

    public void setup(W3URLConnection uc)
        throws IOException {
        this.uc = uc;
        this.headerList = uc.getHeaderList();
        this.requestHeaderFields = uc.getRequestHeaderFields();
        this.source = uc.in;
        this.cacheFile = uc.cacheFile;
        this.cacheHeaders = uc.cacheHeaders;
        this.useCaches = uc.getUseCaches();
        this.contentLength = uc.getContentLength();
        this.lastModified = uc.getLastModified();
        if (this.lastModified == 0L) this.lastModified = System.currentTimeMillis();
        multiple = uc instanceof W3HttpURLConnection ||
            uc instanceof W3FtpURLConnection || uc.usingProxy();
        append = multiple && useCaches && cacheFile.isOffline();
        length = 0L;
        if (append) {
            // sending part of file already in cache to the client
            length = cacheFile.length();
            fin = new BufferedInputStream(new FileInputStream(cacheFile));
            l = Math.min(uc.rangeStart, length);
            for (int n; l > 0 && (n = fin.read(b, 0, l < b.length ? (int)l : b.length)) > 0; l -= n);
        } else length = uc.rangeStart;
        repeats = 0;
        fout = null;
        if (useCaches) {
            fout = new BufferedOutputStream(new FileOutputStream(cacheFile.getPath(), append));
            if (!append) cacheFile.setOffline();
        }
        lastLength = length;
    }

    public final int read(byte buf[])
        throws IOException {
        return read(buf, 0, buf.length);
    }

    public final int read(byte buf[], int off, int len)
        throws IOException {
        if (append) {
            // sending part of file already in cache to the client
            try {
                if (uc.rangeStart < length && l > 0) {
                    int n = fin.read(buf, off, l < len ? (int)l : len);
                    off += n;
                    len -= n;
                    l -= n;
                    if (len <= 0) return n;
                }
            } finally {
                if (l <= 0) fin.close();
            }
        }
        for (boolean continued = false;; continued = true)
            try {
                lastLength = length;
                if (continued) {
                    // retrying to get rest of entity data
                    if (uc.verbose) uc.log("Retrying to get entity");
                    if (!uc.mayKeepAlive) uc.close();
                    uc.clear();
                    uc.rangeStart = length;
                    if (uc.rangeStart > 0L) uc.setRequestProperty("Range", "bytes=" + uc.rangeStart + "-");
                    uc.unsetRequestProperty("if-modified-since");
                    uc.unsetRequestProperty("if-none-match");
                    source = uc.getInput(true);
                }
                LimitInputStream limitIn = uc.limitIn;
                if (uc.rangeStartReceived < uc.rangeStart) {
                    long l = uc.rangeStart - uc.rangeStartReceived;
                    for (int n; l > 0 && (n = source.read(b, 0, l < b.length ? (int)l : b.length)) > 0; l -= n);
                }
                // Sending body data received from remote server to the client and writing it to cache file
                int n = source.read(buf, off, len);
                if (n > 0) {
                    if (useCaches) fout.write(buf, off, n);
                    length += (long)n;
                } else {
                    eof = true;
                    // If remote server has closed the connection and there should be still data, exception must be signalled
                    if (uc.chunkedIn != null && uc.chunkedIn.hasLeft() || uc.limitIn != null && uc.limitIn.getRemaining() > 0) throw new SocketException("Unexpected end of stream");
                }
                return n;
            } catch (Exception ex) {
                if (uc.verbose) uc.log(ex.toString(), ex);
                if (ex instanceof SocketException && multiple) {
                    if (contentLength == -1 || length < contentLength && (length > lastLength || ++repeats <= uc.numberOfRepeats)) {
                        append = true;
                        continue;
                    }
                }
                if (uc.verbose) uc.log("Interrupting cache fill");
                // marking cache file to be incomplete
                if (useCaches) {
                    fout.close();
                    fout = null;
                    if (length > 0L && cacheFile.setOffline()) {
                        cacheFile.setLastTimes(System.currentTimeMillis(), lastModified);
                        cacheFile.setReadOnly();
                    } else cacheFile.delete();
                }
                throw new IOException(ex);
            }
    }

    public final int read()
        throws IOException {
        if (eof) return -1;
        int n = read(temp, 0, 1);
        if (n > 0) return temp[0] & 255;
        eof = true;
        return -1;
    }

    public final long skip(long n)
        throws IOException {
        if (n <= 0L) return n;
        byte b[] = new byte[(int)Math.min(1024, n)];
        long l = n;
        while (l > 0) {
            int k = read(b, 0, (int)Math.min(l, b.length));
            if (k < 1) break;
            l -= k;
        }
        return n - l;
    }

    public final int available()
        throws IOException {
        return source.available();
    }

    public final void close()
        throws IOException {
        if (useCaches && fout != null) {
            fout.close();
            cacheFile.setLastTimes(System.currentTimeMillis(), lastModified);
            if (contentLength != -1 && length < contentLength)
                cacheFile.setOffline();
            else cacheFile.setOnline();
            cacheFile.setReadOnly();
        }
        eof = true;
    }

}

class FeederInputStream extends FilterInputStream {
    W3URLConnection uc;
    W3Requester requester;

    FeederInputStream(W3URLConnection uc, InputStream in, W3Requester requester) {
        super(in);
        this.uc = uc;
        this.requester = requester;
        uc.parts = new HashMap<Integer, Integer>();
        uc.index = uc.index0;
    }

    public int read()
        throws IOException {
        if (this.in == null) return -1;
        for (;;) {
            int c = this.in.read();
            if (c == -1)
                try {
                    if ((this.in = uc.getNextInputStream(requester)) == null) return -1;
                    throw new NextInputStreamException();
                } catch (ParseException ex) {
                    throw new IOException(Support.stackTrace(ex));
                }
            return c;
        }
    }

    public int read(byte b[], int off, int len)
        throws IOException {
        if (this.in == null) return -1;
        for (;;) {
            int n = this.in.read(b, off, len);
            if (n <= 0)
                try {
                    if ((this.in = uc.getNextInputStream(requester)) == null) return -1;
                    throw new NextInputStreamException();
                } catch (ParseException ex) {
                    throw new IOException(Support.stackTrace(ex));
                }
            return n;
        }
    }

    public int read(byte b[])
        throws IOException {
        return read(b, 0, b.length);
    }

}

class CachePool {
    ThreadPool threadPool;
    PoolThread poolThread;
}

/** Base class for URL connection implementations. This class can be
    used with partial requests, but response can be whole entity or
    partial entity depending on cache use. If responses must
    correspond to requests, input stream must be processed before
    sending to end user.
*/
public abstract class W3URLConnection extends HttpURLConnection implements FileFilter, HttpSessionBindingListener {
    public static final String protocol_1_0 = "HTTP/1.0";
    public static final String protocol_1_1 = "HTTP/1.1";

    public static boolean cacheHeaders = false, useMobile = false, logging = false, verbose = false;
    public static int maxCacheEntryLength = 512 * 1024;
    public static int numberOfRepeats = 3;
    public static int numberOfTries = 5;
    public static ResourceBundle messages;
    public static SimpleDateFormat userDateFormats[], cookieDateFormat, listDateFormat, mobileDateFormat;
    public static String addressHeaders[] = {"to", "cc"};
    public static String version = "FI.realitymodeler.W3URLConnection/2012-10-27";
    public static String userAgent = "Mozilla/4.0 (compatible; " + version + ")";
    public static String defaultMimeType = null;
    public static ThreadGroup cacheGroup = null;
    public static CachePool cachePool = null;

    static final char charArray[] = {48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 97, 98, 99, 100, 101, 102};

    static HeaderList defaultRequestHeaderFields = new HeaderList();
    static ListValue gzip = new ListValue("gzip", null, 0.0f),
        xgzip = new ListValue("x-gzip", null, 0.0f),
        deflate = new ListValue("deflate", null, 0.0f),
        xdeflate = new ListValue("x-deflate", null, 0.0f);
    static OutputStream logStream;
    static {
        try {
            messages = getMessages("FI.realitymodeler.resources.Messages");
            int i = 0;
            userDateFormats = new SimpleDateFormat[6];
            userDateFormats[i++] = new SimpleDateFormat("dd-MM-yy HH:mm", Locale.US);
            userDateFormats[i++] = new SimpleDateFormat("dd-MM-yy", Locale.US);
            userDateFormats[i++] = new SimpleDateFormat("dd-MM", Locale.US);
            userDateFormats[i++] = new SimpleDateFormat("dd", Locale.US);
            userDateFormats[i++] = new SimpleDateFormat("HH:mm", Locale.US);
            userDateFormats[i++] = new SimpleDateFormat("HH:mm:ss", Locale.US);
            cookieDateFormat = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss 'GMT'", Locale.US);
            listDateFormat = new SimpleDateFormat("EEE, dd MMM yy HH:mm:ss", Locale.US);
            mobileDateFormat = new SimpleDateFormat("dd-MM-yy HH:mm", Locale.US);
            TimeZone tz = new SimpleTimeZone(0, "GMT");
            cookieDateFormat.setTimeZone(tz);
            for (i = 0; i < userDateFormats.length; i++) userDateFormats[i].setTimeZone(tz);
            setDefaultRequestProperty("MIME-Version", "1.0");
            cacheGroup = new ThreadGroup("FI.realitymodeler.W3URLConnection");
            W3URLConnection.setFollowRedirects(true);
        } catch (Exception ex) {
            throw new Error(Support.stackTrace(ex));
        }
    }

    public Map<String, W3URLConnection> connectionPool = null;
    public String connectionName = null;
    public W3URLConnection uc = null;
    public int numberInUse = 0;
    public long requestNumber = 0L;

    protected String protocol = protocol_1_1;
    protected String message = null;
    protected int status = -1;

    CacheFillingThread filling = null;
    Decoder decoder = null;
    Checking checking = null;
    Map<String,String> params = null;
    Map<Integer,Integer> parts = null;
    Map<String, String> servletPathTable = null;
    HeaderList cacheHeaderList = null, headerFields = null, headerList = null, requestHeaderFields = null, requestHeaderList = null;
    List<HeaderList> continueHeaderFieldsList = null;
    HttpSession httpSession = null;
    InetAddress localAddress = null;
    InputStream in = null, origin = null;
    ChunkedInputStream chunkedIn = null;
    LimitInputStream limitIn = null;
    InputStream plainIn = null;
    ConvertInputStream convertIn = null;
    OutputStream sink = null;
    W3File cacheDir = null, cacheFile = null, cacheHeadersFile = null;
    W3Requester requester = null;
    ServletContext servletContext = null;
    String cachePath = null, contentType = null, charsetName = "UTF-8", host = null, path = null, query = null, subject = null, title = null, username = "", password = "", requestRoot = null, varyId = null;
    boolean autoCaches = false, autoResume = false, cached = false, cacheSetIfModifiedSince = false, cacheSetIfNoneMatch = false, checkCaches = false, closed = false, connecting = false, disablePersistentConnection = false, doingInput = false, eTagMatches = false, fillCaches = false, forceCaches = false, forceResume = false, hasContentEncoding = false, hasTransferEncoding = false, inputDone = false, isPartialRequest = false, loggedIn = false, mayKeepAlive = false, mayPipeline = false, mustRevalidate = false, partial = false, redirected = false, retried = false, tryOnce = false;
    int index0 = 0, index = 0, first = 0, last = 0, number = 0, total = 0;
    int contentLength = -1;
    int defaultPort, localPort = 0;
    int connectTimeout = 0, timeout = 0;
    long cacheFileLastModified = 0L, lastModified = 0L;
    long rangeLast = 0L, rangeLength = 0L, rangeStart = 0L, rangeStartReceived = 0L;
    long requestTime = 0L;
    long clientTime = 0L;
    long keepAliveTimeoutMillis = 0L;
    long keepAliveCount = 0L;

    /** Cleans cache tree in specified root. */
    public static long cleanCache(ServletContext servletContext, String cacheRoot, long cacheCleaningInterval) {
        long cacheCleaningTime = System.currentTimeMillis();
        W3File dir = new W3File(cacheRoot);
        if (dir.exists()) new CacheCleaning(servletContext, dir, cacheCleaningTime - cacheCleaningInterval).start();
        return cacheCleaningTime;
    }

    public static void useCachePool(long timeout) {
        W3URLConnection.cachePool = new CachePool();
        W3URLConnection.cachePool.threadPool = new ThreadPool(timeout);
        W3URLConnection.cachePool.poolThread = new CacheFillingThread(null);
    }

    /** Tries to guess content type from file name. */
    public static final String guessContentType(String name) {
        String mimeType = guessContentTypeFromName(name);
        if (mimeType == null) mimeType = defaultMimeType;
        return mimeType;
    }

    public static String guessContentTypeFromHtmlStream(InputStream in)
        throws IOException {
        String contentType = null;
        boolean found = false, inHead = false, quoted = false;
        int c, count = 4096, n = 0;
        in.mark(count);
        BufferedInputStream bis = new BufferedInputStream(in, count);
        while ((c = bis.read()) != -1) {
            if (c == '<') {
                found = true;
                if (!quoted) {
                    if (inHead) {
                        if ((c = Support.scanTag(bis, "meta", false)) == Support.TAG_WITH_VALUES) {
                            Map<String, String> values = new HashMap<String, String>();
                            c = Support.scanValues(bis, values);
                            String httpEquiv = values.get("http-equiv");
                            if (httpEquiv != null && httpEquiv.equalsIgnoreCase("content-type")) {
                                contentType = values.get("content");
                                if (contentType != null) {
                                    String ct = Support.getParameters(contentType, null).toLowerCase();
                                    if ("text/xhtml".equals(ct)) contentType = null;
                                }
                                break;
                            }
                            if (c == -1) break;
                        }
                    } else if ((c = Support.scanTag(bis, "head", false)) == Support.TAG_WITH_VALUES || c == Support.TAG_WITHOUT_VALUES) {
                        inHead = true;
                        while (c > -1 && c != '>') c = bis.read();
                        if (c == -1) break;
                        continue;
                    }
                    if ((c = Support.scanTag(bis, "body", true)) == Support.TAG_WITH_VALUES || c == Support.TAG_WITHOUT_VALUES || (c = Support.scanTag(bis, "/head", true)) == Support.TAG_WITH_VALUES || c == Support.TAG_WITHOUT_VALUES) break;
                    if ((c = Support.scanTag(bis, "xmp", true)) == Support.TAG_WITH_VALUES || c == Support.TAG_WITHOUT_VALUES) quoted = true;
                    else if ((c = Support.scanTag(bis, "!--", true)) == Support.TAG_WITH_VALUES || c == Support.TAG_WITHOUT_VALUES) {
                        // comment
                        for (;;) {
                            bis.mark(3);
                            if ((c = Support.scanTag(bis, "--", false)) < 0) break;
                            bis.reset();
                            if ((c = bis.read()) == -1) break;
                        }
                        if (c == -1) break;
                        continue;
                    }
                } else if ((c = Support.scanTag(bis, "/xmp", false)) == Support.TAG_WITH_VALUES || c == Support.TAG_WITHOUT_VALUES) quoted = false;
                while (c > -1 && c != '>') c = bis.read();
                if (c == -1) break;
            } else if (!found && ++n > 1024) break;
        }
        in.reset();
        return contentType;
    }

    /** Sets common log stream for URL connections. */
    public static void setLogStream(OutputStream stream, boolean verbose) {
        W3URLConnection.logStream = stream;
        W3URLConnection.verbose = verbose;
        logging = true;
    }

    /** Selects whether message headers are saved to cache. */
    public static final void setCacheHeaders(boolean cacheHeaders) {
        W3URLConnection.cacheHeaders = cacheHeaders;
    }

    public static void log(ServletContext servletContext, String msg) {
        if (servletContext != null) {
            servletContext.log(msg);
            return;
        }
        if (logging)
            try {
                Support.writeBytes(logStream, msg + " in " + Thread.currentThread().getName() + "\n", null);
            } catch (IOException ex) {
                logging = false;
            }
    }

    public static void log(ServletContext servletContext, String msg, Throwable throwable) {
        if (servletContext != null) {
            servletContext.log(msg, throwable);
            return;
        }
        if (logging)
            try {
                Support.writeBytes(logStream, msg + " in " + Thread.currentThread().getName() + "\n" + Support.stackTrace(throwable), null);
            } catch (IOException ex) {
                logging = false;
            }
    }

    /** Parses query input stream storing possible multiple values to
        vectors and values without name with empty key string.

        @param query query input stream to be parsed.
        @param values Map where values are stored.
        @param keys vector where original order of keys are stored. */
    public static void parseQuery(InputStream in, Map<String, Object> values, Vector<String> keys, String charsetName)
        throws IOException {
        FI.realitymodeler.common.URLDecoder urld = new FI.realitymodeler.common.URLDecoder(true);
        do {
            String key = new String(urld.decodeStream(in), charsetName), value;
            if (urld.c != '=') {
                value = key;
                key = "";
            } else value = new String(urld.decodeStream(in), charsetName);
            Object obj = values.get(key);
            if (obj != null) {
                Vector<String> vector;
                if (obj instanceof String) {
                    vector = new Vector<String>();
                    vector.addElement((String)obj);
                    values.put(key, vector);
                } else vector = (Vector<String>)obj;
                vector.addElement(value);
            } else {
                values.put(key, value);
                if (keys != null) keys.addElement(key);
            }
        } while (urld.c != -1);
    }

    public static void parseQuery(InputStream in, Map<String,Object> values, Vector<String> keys)
        throws IOException {
        parseQuery(in, values, keys, "UTF-8");
    }

    public static void parseQuery(String query, Map<String,Object> values, Vector<String> keys)
        throws IOException {
        parseQuery(new ByteArrayInputStream(query.getBytes()), values, keys);
    }

    public static void parseQuery(String query, Map<String,Object> values)
        throws IOException {
        parseQuery(query, values, null);
    }

    public static String getValue(Map<String,Object> values, String name) {
        Object value = values.get(name);
        if (value == null) return "";
        return (String)(value instanceof Vector ? ((Vector)value).firstElement() : value);
    }

    /** Checks if specified string is coming from input stream. Position is restored to original.
        @param in input stream to read
        @param s string to check
        @return number of not matched or -1. */
    public static int check(InputStream in, String string)
        throws IOException {
        int length = string.length(),
            number = Math.max(256, length);
        in.mark(number);
        byte b[] = new byte[number];
        int index = 0;
        while (index < length) {
            int n = in.read(b, index, number);
            if (n <= 0) break;
            index += n;
            number -= n;
        }
        number = Math.min(length, index);
        for (index = 0; index < number; index++)
            if (b[index] != string.charAt(index)) break;
        in.reset();
        return number <= 0 ? -1 : length - index;
    }

    /** Parses from HTML-documents links and searches keywords from text files.
        @param in input stream to read from
        @param urls vector of urls to start from
        @param kws vector of keywords to search or null
        @param forms vector where forms should be stored or null
        @param url url of document coming from input stream to be used as a base in following links or null
        @param nameParts parts of paths names collected URLs must (not) contain or null
        @param doneUrls Map containing handled urls or null
        @param same int array of same length as kws-vector
        @param sames int array of same length as kws-vector containing number of hits
        @param applet applet if showing documents when parsing
        @param delay long array containing delay in ms. to wait between showing documents or null
        @param lock lock to be turned between documents or null
        @param html specifies if stream is html-document
        @param test specifies that all alternate values are gathered for testing purposes
        @return number of words found in document */
    public static int parse(InputStream in, Vector<URL> urls, Vector kws, Vector<HtmlForm> forms, URL url, RegexpPool nameParts[], Map<String, Object> doneUrls, int same[], int sames[], Applet applet, RegexpPool filter, long delay[], W3Lock lock, boolean html, boolean test)
        throws IOException, InterruptedException {
        String s;
        URL url0 = null;
        HtmlForm form = null;
        boolean eof[] = new boolean[1], inForm = false, lastWasChar = false, quoted = false;
        int c, nWords = 0;
        if (kws != null) for (int i = 0; i < kws.size(); i++) same[i] = sames[i] = 0;
        if (url != null) {
            String urlKey = url.toString();
            int i = urlKey.indexOf('?');
            url0 = i != -1 ? new URL(urlKey.substring(0, i)) : url;
            if (doneUrls != null && !doneUrls.containsKey(urlKey)) doneUrls.put(urlKey, Boolean.TRUE);
        } while ((c = in.read()) != -1) {
            if (html && c == '<')
                if (!quoted) {
                    String link = null, name = null;
                    if ((c = Support.scanTag(in, "a", false)) == Support.TAG_WITH_VALUES ||
                        (c = Support.scanTag(in, "area", true)) == Support.TAG_WITH_VALUES) name = "href";
                    else if ((c = Support.scanTag(in, "img", true)) == Support.TAG_WITH_VALUES ||
                             (c = Support.scanTag(in, "frame", true)) == Support.TAG_WITH_VALUES) name = "src";
                    else if ((c = Support.scanTag(in, "meta", true)) == Support.TAG_WITH_VALUES) {
                        Map<String, String> values = new HashMap<String, String>();
                        c = Support.scanValues(in, values);
                        if ((s = values.get("http-equiv")) != null && s.equalsIgnoreCase("refresh") &&
                            (s = values.get("content")) != null) {
                            Map<String, String> params = new HashMap<String, String>();
                            Support.getParameters(s, params, true, null);
                            if ((s = params.get("url")) != null) link = s;
                        }
                    } else if ((c = Support.scanTag(in, "xmp", true)) == Support.TAG_WITH_VALUES || c == Support.TAG_WITHOUT_VALUES) quoted = true;
                    else if (inForm) {
                        if ((c = Support.scanTag(in, "input", true)) == Support.TAG_WITH_VALUES) {
                            Map<String, String> values = new HashMap<String, String>();
                            c = Support.scanValues(in, values);
                            if ((name = values.get("name")) != null) {
                                boolean multiple;
                                String type = values.get("type"), value = values.get("value");
                                if (value == null) value = "";
                                if (type == null || (type = type.toLowerCase()).equals("text") || type.equals("hidden") ||
                                    type.equals("password") || type.equals("submit")) form.putField(name, value);
                                else if (((multiple = type.equals("checkbox")) || type.equals("radio")) &&
                                         (test || values.containsKey("checked")))
                                    if (multiple) form.addField(name, value);
                                    else form.putField(name, value);
                            }
                        } else if ((c = Support.scanTag(in, "select", true)) == Support.TAG_WITH_VALUES) {
                            Map<String, String> values = new HashMap<String, String>();
                            if ((c = Support.scanValues(in, values)) == -1) break;
                            if ((name = values.get("name")) != null) {
                                StringBuffer sb = new StringBuffer();
                                String firstValue = null;
                                boolean multiple = values.containsKey("multiple"),
                                    selected = multiple || (s = values.get("size")) != null && !s.equals("1");
                                values = null;
                                while ((c = in.read()) != -1)
                                    if (c == '<') {
                                        if (values != null) {
                                            boolean flag = values.containsKey("selected") || test;
                                            if (flag || firstValue == null) {
                                                String value = values.get("value");
                                                if (value == null) value = sb.toString();
                                                if (flag) {
                                                    if (multiple) form.addField(name, value);
                                                    else form.putField(name, value);
                                                    selected = true;
                                                }
                                                if (firstValue == null) firstValue = value;
                                                values = null;
                                            }
                                        }
                                        if ((c = Support.scanTag(in, "option", false)) == Support.TAG_WITH_VALUES || c == Support.TAG_WITHOUT_VALUES) {
                                            values = new HashMap<String, String>();
                                            if (c == Support.TAG_WITH_VALUES) c = Support.scanValues(in, values);
                                        } else if ((c = Support.scanTag(in, "/select", true)) <= 0) break;
                                        while (c > -1 && c != '>') c = in.read();
                                        if (c == -1) break;
                                        sb.setLength(0);
                                    } else if (c >= 32) sb.append((char)c);
                                if (!selected && firstValue != null) form.putField(name, firstValue);
                            }
                        } else if ((c = Support.scanTag(in, "textarea", true)) == Support.TAG_WITH_VALUES) {
                            Map<String, String> values = new HashMap<String, String>();
                            if ((c = Support.scanValues(in, values)) == -1) break;
                            if ((name = values.get("name")) != null) {
                                StringBuffer sb = new StringBuffer();
                                while ((c = in.read()) != -1)
                                    if (c == '<') {
                                        if ((c = Support.scanTag(in, "/textarea", false)) <= 0) break;
                                        while (c > -1 && c != '>') c = in.read();
                                        if (c == -1) break;
                                    } else sb.append((char)c);
                                form.putField(name, sb.toString());
                            }
                        } else if ((c = Support.scanTag(in, "/form", true)) <= 0) {
                            if (doneUrls == null) forms.addElement(form);
                            else if (!doneUrls.containsKey(s = form.toString())) {
                                doneUrls.put(s, url);
                                forms.addElement(form);
                            }
                            inForm = false;
                        } while (c > -1 && c != '>') c = in.read();
                        if (c == -1) break;
                        continue;
                    } else if ((c = Support.scanTag(in, "!--", true)) == Support.TAG_WITH_VALUES || c == Support.TAG_WITHOUT_VALUES) {
                        // comment
                        for (;;) {
                            in.mark(3);
                            if ((c = Support.scanTag(in, "--", false)) < 0) break;
                            in.reset();
                            if ((c = in.read()) == -1) break;
                        }
                        if (c == -1) break;
                        continue;
                    } else if (forms != null && (c = Support.scanTag(in, "form", true)) == Support.TAG_WITH_VALUES) name = "action";
                    Map<String, String> values = new HashMap<String, String>();
                    if (name != null) {
                        c = Support.scanValues(in, values);
                        link = values.get(name);
                    } while (c > -1 && c != '>') c = in.read();
                    if (link == null)
                        if (c == -1) break;
                        else continue;
                    if (kws != null) {
                        s = link.toUpperCase();
                        for (int i = 0; i < kws.size(); i++)
                            if (s.indexOf((String)kws.elementAt(i)) != -1) sames[i]++;
                    }
                    int i;
                    if ((i = link.indexOf('#')) != -1) link = link.substring(0, i);
                    try {
                        URL url1 = url0 != null ? new URL(url0, link) : new URL(link);
                        if (nameParts != null) {
                            String urlKey = url1.toString();
                            if ((i = urlKey.indexOf('?')) != -1) urlKey = urlKey.substring(0, i).trim();
                            if (nameParts[0] != null && nameParts[0].match(urlKey) == null ||
                                nameParts[1] != null && nameParts[1].match(urlKey) != null)
                                if (c == -1) break;
                                else continue;
                        }
                        if (name != null && name.equals("action")) {
                            form = new HtmlForm(values.get("method"),
                                                values.get("enctype"), url1);
                            inForm = true;
                            continue;
                        }
                        if (doneUrls == null) urls.addElement(url1);
                        else if (!doneUrls.containsKey(s = url1.toString())) {
                            doneUrls.put(s, url);
                            urls.addElement(url1);
                        }
                        if (applet != null && name != null && name.equals("href") &&
                            (filter == null || filter.match(url1.getFile()) != null)) {
                            applet.getAppletContext().showDocument(url1, "document");
                            if (delay != null && delay[0] != -1L) {
                                Thread.sleep(delay[0]);
                                if (lock != null) {
                                    lock.lock();
                                    lock.release();
                                }
                            } else if (lock != null) lock.lock();
                        }
                    } catch (MalformedURLException ex) {}
                    if (c == -1) break;
                    continue;
                } else if ((c = Support.scanTag(in, "/xmp", false)) == Support.TAG_WITH_VALUES || c == Support.TAG_WITHOUT_VALUES) {
                    quoted = false;
                    while (c > -1 && c != '>') c = in.read();
                    if (c == -1) break;
                    continue;
                } else {
                    c = '<';
                    in.reset();
                }
            if (kws == null) continue;
            if (html && !quoted && c == '&') c = Support.convert(in, eof);
            boolean isChar = Support.whites.indexOf(c) == -1;
            char c1 = Character.toUpperCase((char)c);
            for (int i = 0; i < kws.size(); i++) {
                String kw = (String)kws.elementAt(i);
                if (same[i] == kw.length()) {
                    same[i] = 0;
                    if (!isChar) sames[i]++;
                } else if (kw.charAt(same[i]) != c1) same[i] = 0;
                else if (same[i] > 0 || !lastWasChar) same[i]++;
            }
            if (!lastWasChar && isChar) nWords++;
            lastWasChar = isChar;
            if (eof[0]) break;
        }
        if (inForm)
            if (doneUrls == null) forms.addElement(form);
            else if (!doneUrls.containsKey(s = form.toString())) {
                doneUrls.put(s, url);
                forms.addElement(form);
            }
        if (kws != null)
            for (int i = 0; i < kws.size(); i++)
                if (same[i] == ((String)kws.elementAt(i)).length()) sames[i]++;
        return nWords;
    }

    /** Writes directory listing line. */
    public static void writeLine(OutputStream out, Date date, boolean mayHaveAttachments, String size, String link, String target, String name, String charsetName, boolean mobile)
        throws IOException {
        Support.writeBytes(out, (date != null ? (mobile ? mobileDateFormat : listDateFormat).format(date) : "&nbsp;") + (mayHaveAttachments ? "&nbsp;&Dagger;" : "") + "</span>", charsetName);
        Support.writeBytes(out, "<span class=\"listcolumn\">" + (size != null ? size : "") + "</span>", charsetName);
        Support.writeBytes(out, (link != null ? "<a href=\"" + link + "\"" + (target != null ? " target=\"" + target + "\"" : "") + ">" + name + "</a>" : name) + "<br>\n", charsetName);
    }

    public static void writeLine(OutputStream out, Date date, String size, String link, String target, String name, String charsetName, boolean mobile)
        throws IOException {
        writeLine(out, date, false, size, link, target, name, charsetName, mobile);
    }

    /** Writes directory listing line. */
    public static void writeLine(PrintWriter writer, Date date, String size, String link, String target, String name, boolean mobile)
        throws IOException {
        writer.print((date != null ? (mobile ? mobileDateFormat : listDateFormat).format(date) : "&nbsp;") + "</span>");
        if (size != null) writer.print("<span class=\"listcolumn\">" + size + "</span>");
        writer.println((link != null ? "<a href=\"" + link + "\"" + (target != null ?
                                                                     " target=\"" + target + "\"" : "") + ">" + name + "</a>" : name) + "<br>");
    }

    /** Normalizes file path stripping relative references.
        @param path file path
        @return normalized path or null if path underflows. */
    public static String normalize(String path) {
        path = path.trim();
        StringBuffer sb = new StringBuffer();
        if (path.endsWith("/")) sb.append('/');
        int i = path.length(), j, k = 0;
        while (i > 0) {
            j = path.lastIndexOf('/', i - 1);
            String s = path.substring(j + 1, i).trim();
            i = j;
            if (s.equals("") || s.equals(".")) continue;
            if (s.equals("..")) k++;
            else if (k == 0) sb.insert(0, j == -1 ? s : "/" + s);
            else k--;
        }
        if (k > 0) return null;
        if (sb.length() == 0) return "/";
        return sb.toString();
    }

    @Deprecated
    public static final void setDefaultRequestProperty(String key, String value) {
        defaultRequestHeaderFields.replace(new Header(key, value));
    }

    @Deprecated
    public static final String getDefaultRequestProperty(String key) {
        return defaultRequestHeaderFields.getHeaderValue(key);
    }

    public static String hash(MessageDigest md, String string) {
        byte b[] = string.getBytes();
        md.reset();
        md.update(b, 0, b.length);
        b = md.digest();
        int i = 0;
        char a[] = new char[32];
        for (int k = 0; k < 16; k++) {
            int j = b[k] >>> 4 & 15;
            a[i++] = charArray[j];
            j = b[k] & 15;
            a[i++] = charArray[j];
        }
        return new String(a);
    }

    public static String compute(MessageDigest md, String username, String realm, String password, String nonce, String method, String uri) {
        return hash(md, hash(md, username + ":" + realm + ":" + password) + ":" + nonce + ":" + hash(md, method + ":" + uri));
    }

    /** Return basic credentials for given parameters. */
    public static String basicCredentials(String username, String password) {
        return "Basic " + new BASE64Encoder().encode((username + ":" + (password != null ? password : "")).getBytes());
    }

    /** Return digest credentials for given parameters. */
    public static String digestCredentials(String username, String password, String realm, String method, String nonce, String uri)
        throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        return "Digest username=\"" + username + "\",\n" +
            "\trealm=\"" + realm + "\",\n" +
            "\tnonce=\"" + nonce + "\",\n" +
            "\turi=\"" + uri + "\",\n" +
            "\tresponse=\"" + compute(md5, username, realm, password != null ? password : "", nonce, method, uri) + "\"";
    }

    public static Vector<ListValue> getList(String list, String defaultValue, String anyValue) {
        StringTokenizer st = new StringTokenizer(list, ",");
        if (!st.hasMoreTokens()) return null;
        Vector<ListValue> listVector = new Vector<ListValue>();
        while (st.hasMoreTokens()) {
            Map<String, String> params = new HashMap<String, String>();
            String name = Support.getParameters(st.nextToken(), params).toLowerCase(), q = params.get("q");
            if (anyValue != null && name.equals(anyValue)) return null;
            float qualityValue;
            if (q != null) {
                params.remove("q");
                if ((qualityValue = Float.valueOf(q).floatValue()) <= 0.0) continue;
            } else qualityValue = 1.0f;
            int i, n = listVector.size();
            for (i = 0; i < n; i++) {
                ListValue listValue = listVector.elementAt(i);
                if (listValue.qualityValue == qualityValue) {
                    int delta;
                    if (name.equalsIgnoreCase(defaultValue) || (delta = listValue.name.compareTo(name)) < 0 ||
                        delta == 0 && (listValue.params == null || listValue.params.size() <= params.size())) break;
                } else if (listValue.qualityValue < qualityValue) break;
            }
            listVector.insertElementAt(new ListValue(name, params, qualityValue), i);
        }
        return listVector;
    }

    public static Vector<ListValue> getList(String list) {
        return getList(list, null, null);
    }

    public static ResourceBundle getMessages(String baseName) {
        try {
            return ResourceBundle.getBundle(baseName);
        } catch (MissingResourceException ex) {}
        return ResourceBundle.getBundle(baseName, Locale.ENGLISH);
    }

    public static Vector<Locale> getLocales(String acceptLanguage) {
        if (acceptLanguage == null) return null;
        Vector<ListValue> languageRanges = getList(acceptLanguage);
        Vector<Locale> locales = new Vector<Locale>();
        if (languageRanges == null) return null;
        boolean defaultAdded = false;
        Locale defaultLocale = Locale.getDefault();
        Enumeration languageRangeItems = languageRanges.elements();
        while (languageRangeItems.hasMoreElements()) {
            ListValue range = (ListValue)languageRangeItems.nextElement();
            if (range.name.equals("*")) {
                if (!defaultAdded) {
                    locales.addElement(defaultLocale);
                    defaultAdded = true;
                }
                continue;
            }
            String language, country;
            int i = range.name.indexOf('-');
            if (i != -1) {
                language = range.name.substring(0, i).trim().toLowerCase();
                country = range.name.substring(i + 1).trim().toUpperCase();
            } else {
                language = range.name.toLowerCase();
                country = "";
            }
            Locale locale = new Locale(language, country);
            if (locale.equals(defaultLocale))
                if (defaultAdded) continue;
                else defaultAdded = true;
            locales.addElement(locale);
        }
        return locales;
    }

    public static ResourceBundle getLanguage(String acceptLanguage, String baseName) {
        if (acceptLanguage == null) return null;
        Vector<ListValue> languageRanges = getList(acceptLanguage);
        if (languageRanges == null) return null;
        Locale defaultLocale = Locale.getDefault();
        Enumeration languageRangeItems = languageRanges.elements();
        while (languageRangeItems.hasMoreElements()) {
            ListValue range = (ListValue)languageRangeItems.nextElement();
            if (range.name.equals("*")) break;
            String language, country;
            int i = range.name.indexOf('-');
            if (i != -1) {
                language = range.name.substring(0, i).trim().toLowerCase();
                country = range.name.substring(i + 1).trim().toUpperCase();
            } else {
                language = range.name.toLowerCase();
                country = "";
            }
            Locale locale = new Locale(language, country);
            if (locale.equals(defaultLocale)) break;
            try {
                ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale);
                if (bundle.getString("LANGUAGE").equalsIgnoreCase(language)) return bundle;
            } catch (MissingResourceException ex) {}
        }
        return null;
    }

    public static ResourceBundle getLanguage(HttpServletRequest req, HttpServletResponse res, ResourceBundle defaultMessages, String baseName, String textType)
        throws IOException {
        ResourceBundle messages = null;
        String acceptLanguage = req.getHeader("accept-language");
        try {
            messages = getLanguage(acceptLanguage, baseName);
        } catch (MissingResourceException ex) {
            res.sendError(HTTP_NOT_ACCEPTABLE, "Not acceptable from " + acceptLanguage);
            return null;
        }
        String charset = null;
        if (messages != null)
            try {
                charset = messages.getString("CHARSET");
            } catch (MissingResourceException ex) {}
        res.setContentType(charset != null ? textType + "; charset=" + charset : textType);
        if (messages == null) messages = defaultMessages;
        String language = null;
        try {
            language = messages.getString("LANGUAGE");
        } catch (MissingResourceException ex) {}
        if (language != null) res.setHeader("Content-Language", language);
        return messages;
    }

    public static final ResourceBundle getLanguage(HttpServletRequest req, HttpServletResponse res, ResourceBundle defaultMessages, String baseName)
        throws IOException {
        return getLanguage(req, res, defaultMessages, baseName, "text/html");
    }

    /** Gets name and address in header with format 'Name &lt;address&gt;' or 'address (Name)' or
        remaps address from X-400 format. */
    public static String[] getAddress(String address[], String header, String host)
        throws IOException {
        address[0] = address[1] = null;
        if (header == null) return address;
        int i, j, n = (header = Support.decodeWords(header.trim())).length() - 1;
        if (n < 0) return address;
        if ((i = header.lastIndexOf('<', n)) != -1 || (i = header.lastIndexOf('(', n)) != -1) {
            char c = header.charAt(i);
            address[c == '<' ? 1 : 0] = header.substring(0, i).trim();
            address[c == '<' ? 0 : 1] = header.substring(i + 1,
                                                         (j = header.lastIndexOf(c == '<' ? '>' : ')', n)) != -1 && j > i ?
                                                         j : header.length()).trim();
            address[0] = Support.unquoteString(address[0]).trim();
            address[1] = Support.unquoteString(Support.decodeWords(address[1])).trim();
        } else if (header.indexOf('=') != -1 || header.indexOf(':') != -1) {
            // maybe X.400 address
            int orgPos = -1;
            Vector<String> vector = new Vector<String>();
            String head = null, tail = null, country = null;
            StringTokenizer st = new StringTokenizer(header, ",;/");
            while (st.hasMoreTokens()) {
                StringTokenizer st1 = new StringTokenizer(st.nextToken(), "=:");
                String name = Support.unquoteString(st1.nextToken()).trim();
                if (!st1.hasMoreTokens()) {
                    if (name.indexOf('@') != -1) tail = name;
                    continue;
                }
                name = name.toUpperCase();
                String value = Support.unquoteString(st1.nextToken()).trim();
                if (name.equals("C")) country = value;
                else if (name.equals("O")) {
                    orgPos = vector.size();
                    vector.addElement(name);
                } else if (name.equals("OU")) vector.addElement(value);
                else if (name.equals("S")) head = value;
                else if (name.endsWith("RFC-822")) return getAddress(address, value, host);
            }
            StringBuffer sb = new StringBuffer();
            if (head != null) sb.append(head);
            if (tail != null) sb.append(tail);
            else sb.append('@');
            if (orgPos != -1) {
                for (i = 0; i < orgPos; i++) {
                    if (sb.length() > 0) sb.append('.');
                    sb.append(vector.elementAt(i));
                }
                for (i = vector.size() - 1; i > orgPos; i--) {
                    if (sb.length() > 0) sb.append('.');
                    sb.append(vector.elementAt(i));
                }
                if (sb.length() > 0) sb.append('.');
                sb.append(vector.elementAt(orgPos));
            }
            else
                for (i = 0; i < vector.size(); i++) {
                    if (sb.length() > 0) sb.append('.');
                    sb.append(vector.elementAt(i));
                }
            if (country != null) {
                if (sb.length() > 0) sb.append('.');
                sb.append(country);
            }
            address[0] = address[1] = sb.toString();
        } else if ((address[0] = Support.unquoteString(header).trim()).indexOf('@') == -1) address[1] = address[0] += "@" + host;
        else address[1] = address[0];
        if (address[0] != null && address[0].equals("")) address[0] = null;
        if (address[1].equals("")) address[1] = address[0];
        return address;
    }

    /** Gets name and address in from- and reply-to-headers. */
    public static String getAddress(HeaderList headerList, String address[], String host)
        throws IOException {
        address[0] = address[1] = null;
        String header = headerList.getHeaderValue("reply-to");
        if (header != null) getAddress(address, header, host);
        if ((address[0] == null || address[0] == address[1]) &&
            (header = headerList.getHeaderValue("from")) != null) {
            String to = address[0];
            getAddress(address, header, host);
            if (to != null) {
                address[0] = to;
                if (address[1] == null) address[1] = to;
            }
        }
        return address[0] != null ? address[1] != null && !address[1].equalsIgnoreCase(address[0]) ?
            address[0] + " (" + address[1] + ")" : address[0] : null;
    }

    /** Gets from header list to- and cc-addresses omitting from-address and to-address. */
    public static String[] getOthers(HeaderList headerList, String fromAddress, String toAddress, String  host, String others[])
        throws IOException {
        String address[] = new String[2], s;
        StringBuffer sb = new StringBuffer(), sb1 = new StringBuffer();
        for (int i = 0; i < others.length; i++) others[i] = null;
        for (int i = 0; i < addressHeaders.length; i++)
            if ((s = headerList.getHeaderValue(addressHeaders[i])) != null) {
                StringTokenizer st = new StringTokenizer(Support.decodeWords(s), ",");
                while (st.hasMoreTokens()) {
                    if (getAddress(address, st.nextToken(), host)[0] == null ||
                        toAddress != null && toAddress.equalsIgnoreCase(address[0])) continue;
                    boolean isFromAddress = fromAddress != null && fromAddress.equalsIgnoreCase(address[0]);
                    if (others.length > addressHeaders.length) {
                        if (address[1] != null) {
                            if (sb1.length() > 0) sb1.append(',');
                            sb1.append(isFromAddress ? "#" : address[1]);
                        }
                    }
                    if (isFromAddress) continue;
                    if (sb.length() > 0) sb.append(',');
                    sb.append(address[0]);
                    if (address[1] != null && !address[1].equalsIgnoreCase(address[0])) sb.append(" (" + address[1] + ")");
                }
                if (sb.length() > 0) others[i] = sb.toString();
                sb.setLength(0);
                if (others.length <= addressHeaders.length) continue;
                if (sb1.length() > 0) others[i + 2] = sb1.toString();
                sb1.setLength(0);
            }
        return others;
    }

    public static String cookieSetHeader(Cookie cookie) {
        StringBuffer header = new StringBuffer();
        header.append(cookie.getName()).append('=').append(cookie.getValue());
        if (cookie.getMaxAge() != -1) header.append("; expires=").append(cookieDateFormat.format(new Date(System.currentTimeMillis() + (long)cookie.getMaxAge() * 1000L)));
        if (cookie.getPath() != null) header.append("; path=").append(cookie.getPath());
        if (cookie.getDomain() != null) header.append("; domain=").append(cookie.getDomain());
        if (cookie.getSecure()) header.append("; secure");
        return header.toString();
    }

    public static Cookie[] getCookies(HeaderList headerList) {
        Header cookieHeaders[] = headerList.getHeaders("cookie");
        if (cookieHeaders == null) return null;
        Map<String, String> table = new HashMap<String, String>();
        Vector<Cookie> cookieVec = new Vector<Cookie>();
        Vector<Assignment> vector = new Vector<Assignment>();
        for (int i = 0; i < cookieHeaders.length; i++) {
            Support.parse(cookieHeaders[i].getValue(), table, ";", false, true, vector);
            Enumeration assignmentEnum = vector.elements();
            while (assignmentEnum.hasMoreElements()) {
                Assignment assignment = (Assignment)assignmentEnum.nextElement();
                try {
                    cookieVec.addElement(new Cookie(assignment.name, assignment.value));
                } catch (IllegalArgumentException ex) {}
            }
            table.clear();
            vector.removeAllElements();
        }
        if (cookieVec.isEmpty()) return null;
        Cookie cookies[] = new Cookie[cookieVec.size()];
        for (int i = cookieVec.size() - 1; i >= 0; i--) cookies[i] = cookieVec.elementAt(i);
        return cookies;
    }

    public static Cookie getCookie(String name, Cookie cookies[]) {
        if (cookies != null)
            for (int i = 0; i < cookies.length; i++)
                if (cookies[i].getName().equals(name)) return cookies[i];
        return null;
    }

    public static W3URLConnection openConnection(URL url)
        throws IOException {
        return W3Factory.openConnection(url);
    }

    public W3URLConnection(URL url) {
        super(url);
        set(url);
    }

    public String getVersion() {
        return version;
    }

    public void setRequestMethod(String method)
        throws ProtocolException {
        this.method = method;
        tryOnce = method.equals("XTENSION");
    }

    public void setRequestMethod()
        throws ProtocolException {
        if (requestHeaderList != null) return;
        Support.copyHeaderList(requestHeaderFields, requestHeaderList = new HeaderList());
        if (method == null) setRequestMethod(doOutput ? "POST" : "GET");
        Header header = requestHeaderFields.getFirst();
        if (header == null || !header.getName().equals("")) requestHeaderFields.prepend(new Header("", method + " " + (usingProxy() ? url.toString() : (url.getFile().equals("") ? "/" : url.getFile())) + " " + (useMobile ? protocol_1_1 : protocol)));
        if (!disablePersistentConnection) {
            String request = getRequestProperty("");
            int i = request.indexOf(' ');
            if (i != -1) {
                int j = request.lastIndexOf(' ');
                String connection = getRequestProperty("connection");
                mayKeepAlive = j > i && !request.substring(i + 1, j).trim().equals("") &&
                    request.substring(j + 1).trim().compareTo(protocol_1_1) >= 0 ?
                    connection == null || !connection.equalsIgnoreCase("close") :
                    (connection != null || (connection = getRequestProperty("proxy-connection")) != null) &&
                    connection.equalsIgnoreCase("keep-alive") && getRequestProperty("content-length") != null;
            }
        }
        //connecting = true;
    }

    public int setDefaultRequestFields() {
        Iterator items = defaultRequestHeaderFields.iterator();
        int i = 1;
        while (items.hasNext()) {
            Header header = (Header)items.next();
            if (requestHeaderFields.hasHeader(header.getName())) continue;
            requestHeaderFields.insert(header, i++);
        }
        return i;
    }

    public void removeConnectionValues(HeaderList headerList) {
        List<String> connectionValueList = new ArrayList<String>();
        int headerListLength = headerList.size();
        for (int index = 0; index < headerListLength; index++) {
            Header header = headerList.getHeader(index);
            String headerKey = header.getName().toLowerCase(),
                value = header.getValue();
            if ("connection".equals(headerKey)) {
                StringTokenizer tokens = new StringTokenizer(value, ",");
                while (tokens.hasMoreTokens())
                    connectionValueList.add(tokens.nextToken().trim());
            }
        }
        Iterator connectionValues = connectionValueList.iterator();
        while (connectionValues.hasNext())
            headerList.removeAll((String)connectionValues.next());
        headerList.removeAll("connection");
    }

    public void setRequestFields() {
        int i = setDefaultRequestFields();
        if (getRequestProperty("host") == null) requestHeaderFields.insert(new Header("Host", url.getHost() + (url.getPort() != -1 ? ":" + url.getPort() : "")), i++);
        if (getRequestProperty("accept") == null) requestHeaderFields.insert(new Header("Accept", "text/html, */*"), i++);
        if (getRequestProperty("user-agent") == null) requestHeaderFields.insert(new Header("User-Agent", userAgent), i);
        if (getRequestProperty("authorization") == null && url.getUserInfo() != null) setRequestProperty("Authorization", "Basic " + new BASE64Encoder().encode(url.getUserInfo().getBytes()));
        removeConnectionValues(requestHeaderFields);
        unsetRequestProperty("keep-alive");
        unsetRequestProperty("proxy-connection");
        setRequestProperty(usingProxy() ? "Proxy-Connection" : "Connection", "Keep-Alive");
    }

    public void parseRange()
        throws IOException {
        String range;
        if ((range = getRequestProperty("range")) == null) return;
        try {
            StringTokenizer st = new StringTokenizer(range, "=");
            String rangeType;
            if (!(rangeType = st.nextToken().trim()).equalsIgnoreCase("bytes")) throw new IOException("Bad range type " + rangeType);
            int minPos = -1, maxPos = -1;
            st = new StringTokenizer(st.nextToken(), ",");
            while (st.hasMoreTokens()) {
                StringTokenizer st1 = new StringTokenizer(st.nextToken(), "-", true);
                if (!st1.hasMoreTokens()) continue;
                String s = st1.nextToken().trim();
                int firstPos, lastPos;
                try {
                    if (s.equals("-")) {
                        // Suffix byte range specifier
                        if (contentLength == -1) return;
                        if (!st1.hasMoreTokens()) continue;
                        firstPos = Math.max(contentLength - Integer.parseInt(st1.nextToken().trim()), 0);
                        lastPos = contentLength - 1;
                    } else {
                        // Byte range specifier
                        if (!st1.hasMoreTokens()) continue;
                        st1.nextToken();
                        firstPos = Integer.parseInt(s);
                        if (contentLength > 0) firstPos = Math.min(firstPos, contentLength - 1);
                        if (!st1.hasMoreTokens()) continue;
                        lastPos = st1.hasMoreTokens() ? Integer.parseInt(st1.nextToken().trim()) : -1;
                        if (contentLength > 0) lastPos = Math.min(lastPos, contentLength - 1);
                    }
                } catch (NumberFormatException ex) {
                    continue;
                }
                minPos = minPos == -1 ? firstPos : Math.min(firstPos, minPos);
                maxPos = maxPos == -1 ? lastPos : Math.max(lastPos, maxPos);
            }
            rangeStart = minPos;
            rangeLast = maxPos;
        } catch (NoSuchElementException ex) {
            throw new IOException("Bad range " + range);
        }
    }

    /** Gets status code of response. */
    public int getResponseCode()
        throws IOException {
        if (status != -1) return status;
        protocol = protocol_1_0;
        status = HTTP_OK;
        message = "";
        try {
            String s = getHeaderField(0);
            if (s != null) {
                StringTokenizer st = new StringTokenizer(s);
                if ((protocol = st.nextToken()).lastIndexOf('/') == -1) protocol += "/1.0";
                try {
                    status = Integer.parseInt(st.nextToken());
                } catch (NumberFormatException ex) {}
                message = st.nextToken("").trim();
            }
        } catch (NoSuchElementException ex) {}
        catch (RuntimeException ex) {
            if (ex.getCause() instanceof IOException)
                throw (IOException)ex.getCause();
            throw ex;
        }
        return status;
    }

    /** Gets status message of response. */
    public final String getResponseMessage()
        throws IOException {
        getResponseCode();
        return message;
    }

    public void clear() {
        headerFields = null;
        contentLength = -1;
        contentType = null;
        lastModified = 0L;
        mayKeepAlive = false;
        protocol = protocol_1_1;
        rangeLength = rangeStart = rangeStartReceived = 0L;
        status = -1;
        convertIn = null;
        chunkedIn = null;
        limitIn = null;
        plainIn = null;
        closed = false;
        connected = false;
        inputDone = false;
        in = null;
        sink = null;
        origin = null;
        requestTime = 0L;
    }

    public void open()
        throws IOException, ParseException {
    }

    public void close() {
        if (requester == null) return;
        try {
            requester.close();
        } catch (IOException ex) {}
        finally {
            requester = null;
            loggedIn = false;
            clear();
        }
    }

    /** Closes the connection */
    public synchronized void disconnect() {
        if (!closed) {
            if (verbose) log("Disconnecting " + url);
            closed = true;
            if (autoCaches && fillingCaches()) return;
        }
        try {
            closeStreams();
        } finally {
            close();
        }
    }

    public boolean usingProxy() {
        return false;
    }

    public int getDefaultPort() {
        return -1;
    }

    public InputStream getErrorStream() {
        try {
            return getInputStream();
        } catch (IOException ex) {
            return null;
        }
    }

    public boolean accept(File pathname) {
        return pathname.getName().endsWith("#");
    }

    public void valueBound(HttpSessionBindingEvent event) {
    }

    public void valueUnbound(HttpSessionBindingEvent event) {
        disconnect();
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void setHttpSession(HttpSession httpSession) {
        this.httpSession = httpSession;
    }

    public void setChecking(Checking checking) {
        this.checking = checking;
    }

    public void log(String msg) {
        log(servletContext, msg);
    }

    public void log(String msg, Throwable throwable) {
        log(servletContext, msg, throwable);
    }

    /** Tries to parse date string with given formats. */
    public Date parse(String dateString, DateFormat dateFormats[]) {
        Date date = Support.parse(dateString.trim(), dateFormats);
        if (date == null) log("Failed to parse date: " + dateString);
        return date;
    }

    /** Tries to parse date string with multiple formats. */
    public Date parse(String dateString) {
        return parse(dateString, Support.dateFormats);
    }

    /** Returns true if content is html. */
    public boolean isHtml() {
        String ct = getContentType();
        return ct != null &&
            ((ct = Support.getParameters(ct, null)).equalsIgnoreCase(Support.htmlType) ||
             ct.equalsIgnoreCase(Support.shtmlType));
    }

    /** Returns true if content is text. */
    public boolean isText() {
        String ct = getContentType();
        return ct != null && ((ct = ct.toLowerCase()).startsWith("text/") || ct.equals("text"));
    }

    public long getLastModified() {
        if (lastModified != 0L) return lastModified;
        return lastModified = super.getLastModified();
    }

    public int getContentLength() {
        if (contentLength != -1) return contentLength;
        return contentLength = super.getContentLength();
    }

    public String getContentType() {
        if (contentType != null) return contentType;
        return contentType = super.getContentType();
    }

    public String getHeaderField(String name) {
        check();
        return headerFields.getHeaderValue(name);
    }

    public String getHeaderFieldKey(int n) {
        check();
        Header header = headerFields.getHeader(n);
        if (header == null) return null;
        return header.getName();
    }

    public String getHeaderField(int n) {
        check();
        Header header = headerFields.getHeader(n);
        if (header == null) return null;
        return header.getValue();
    }

    public Object getContent()
        throws IOException {
        check();
        return super.getContent();
    }

    public Object getContent(Class classes[])
        throws IOException {
        check();
        return super.getContent(classes);
    }

    public void checkCacheCleaning() {
    }

    /** Connects to proxy server. */
    public void proxyConnect(String host, int port)
        throws IOException {
        if (useMobile) {
            if (verbose) log("Connecting to mobile server " + host + ":" + port);
            requester = new W3Requester(host, port, W3Socket.COMPRESSED, localAddress, localPort);
            return;
        }
        if (verbose) log("Connecting to proxy server " + host + ":" + port);
        requester = new W3Requester(host, port, 0, localAddress, localPort);
        if (timeout > 0) requester.setTimeout(timeout);
    }

    /** Sets local address and port where connection socket is bound. */
    public final void setLocalAddress(InetAddress localAddress, int localPort) {
        this.localAddress = localAddress;
        this.localPort = localPort;
    }

    public Map<String,List<String>> getHeaderFields() {
        check();
        return headerFields.getMap();
    }

    public HeaderList getHeaderFieldList() {
        check();
        return headerFields;
    }

    public List<HeaderList> getContinueHeaderFieldsList() {
        check();
        return continueHeaderFieldsList;
    }

    public final void addRequestProperty(String key, String value) {
        requestHeaderFields.append(new Header(key, value));
    }

    public final void setRequestProperty(String key, String value) {
        requestHeaderFields.replace(new Header(key, value));
    }

    public final void unsetRequestProperty(String key) {
        requestHeaderFields.removeAll(key);
    }

    public final String getRequestProperty(String key) {
        return requestHeaderFields.getHeaderValue(key);
    }

    public Map<String,List<String>> getRequestProperties() {
        try {
            setRequestMethod();
        } catch (ProtocolException ex) {
            throw new RuntimeException(ex);
        }
        return requestHeaderList.getMap();
    }

    public final long getRequestPropertyDate(String key, long defaultValue) {
        String value = getRequestProperty(key);
        if (value == null) return defaultValue;
        Date date = Support.parse(Support.getParameters(value, null));
        if (date == null) return defaultValue;
        return date.getTime();
    }

    public final HeaderList getRequestHeaderFields() {
        return requestHeaderFields;
    }

    /** Returns true if request is from mobile phone. */
    public boolean isMobile() {
        String s = getRequestProperty("user-agent");
        return s != null && s.trim().equalsIgnoreCase("mobile");
    }

    public InputStream getUnauthorizedStream(String msg)
        throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        String title = HTTP_UNAUTHORIZED + " " + Support.htmlString(messages.getString("unauthorized"));
        Support.writeBytes(bout, "<html><head><title>" + title + "</title></head><body>\n", charsetName);
        Support.writeBytes(bout, "<h1>" + title + "</h1>\n", charsetName);
        if (msg != null) Support.writeBytes(bout, Support.htmlString(msg) + "\n", charsetName);
        Support.writeBytes(bout, "</body></html>", charsetName);
        byte b[] = bout.toByteArray();
        headerFields = new HeaderList();
        setResponse(HTTP_UNAUTHORIZED, "Unauthorized", false, false);
        headerFields.append(new Header("Www-Authenticate", "Basic realm=\"" + (host != null ? host : url.getHost()) + (getDefaultPort() != -1 ? ":" + (url.getPort() != -1 ? url.getPort() : getDefaultPort()) : "") + "\""));
        headerFields.append(new Header("Content-Type", Support.htmlType));
        headerFields.append(new Header("Content-Length", String.valueOf(b.length)));
        inputDone = true;
        return in = new ByteArrayInputStream(b);
    }

    public long getHeaderFieldDate(String name, long def) {
        return parseDate(getHeaderField(name), def);
    }

    public long getHeaderFieldLong(String name, long def) {
        return parseLong(getHeaderField(name), def);
    }

    public long parseDate(String value, long def) {
        if (value == null) return def;
        Date date = parse(value);
        return date != null ? date.getTime() : def;
    }

    public long parseLong(String value, long def) {
        if (value == null) return def;
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ex) {
            return def;
        }
    }

    public W3Requester getRequester() {
        return requester;
    }

    public void setRequester(W3Requester requester) {
        this.requester = requester;
    }

    public String getProxyScheme() {
        return "http";
    }

    public String getProxyHost() {
        return null;
    }

    public int getProxyPort() {
        return -1;
    }

    public HeaderList getHeader(W3Requester requester, MsgItem msgItem)
        throws IOException, ParseException {
        return null;
    }

    public InputStream getEntity(W3Requester requester, MsgItem msgItem)
        throws IOException, ParseException {
        return null;
    }

    public String getNumbers(String s) {
        int i = Math.max(s.lastIndexOf('('), s.lastIndexOf('['));
        if (i == -1) return null;
        int j = s.indexOf(s.charAt(i) == '(' ? ')' : ']', i + 1);
        if (j == -1) return null;
        String t = s.substring(i + 1, j);
        if ((j = t.indexOf('/')) == -1) return null;
        try {
            number = Integer.parseInt(t.substring(0, j).trim());
            total = Integer.parseInt(t.substring(j + 1).trim());
        } catch (NumberFormatException ex) {
            return null;
        }
        StringTokenizer st = new StringTokenizer(s.substring(0, i), "\t \"");
        StringBuffer buffer = new StringBuffer();
        while (st.hasMoreTokens()) buffer.append(st.nextToken());
        return buffer.toString();
    }

    public InputStream getNextInputStream(W3Requester requester)
        throws IOException, ParseException {
        String id = null, s;
        if (partial) {
            s = params.get("total");
            if (s != null && number >= Integer.parseInt(s) ||
                (id = params.get("id")) == null) return null;
        } else if (title == null || number == total) return null;
        int index1, number1 = number + 1;
        Integer key = new Integer(number1);
        MsgItem msgItem = new MsgItem();
        if (!parts.containsKey(key))
            for (boolean inverse = false;;) {
                if (inverse) {
                    if (++index > last || index - index0 > total + 1000) return null;
                } else if (--index < first || index0 - index > total + 1000) {
                    index = index0;
                    inverse = true;
                    continue;
                }
                msgItem.index = index;
                HeaderList headerList = getHeader(requester, msgItem);
                if (headerList == null) continue;
                if (partial)
                    if ((s = headerList.getHeaderValue("content-type")) == null ||
                        !Support.getParameters(s, params = new HashMap<String, String>()).equalsIgnoreCase(Support.partialType) ||
                        params.get("id") != id || (s = params.get("number")) == null) continue;
                    else number = Integer.parseInt(s);
                else if ((s = headerList.getHeaderValue("subject")) == null ||
                         (s = getNumbers(s)) == null || !s.startsWith(title)) continue;
                if (number != number1) {
                    parts.put(new Integer(number), new Integer(index));
                    continue;
                }
                index1 = index;
                break;
            } else {
            index1 = parts.get(key).intValue();
            parts.remove(key);
        }
        msgItem.index = index1;
        InputStream source = getEntity(requester, msgItem);
        if (partial) {
            params = new HashMap<String, String>();
            if ((s = msgItem.headerList.getHeaderValue("content-type")) != null) Support.getParameters(s, params);
            new HeaderList(source);
        } else {
            getNumbers(msgItem.headerList.getHeaderValue("subject"));
            int c;
            do source.mark(1);
            while ((c = source.read()) != -1 && Support.whites.indexOf(c) != -1);
            source.reset();
        }
        return source;
    }

    public boolean getBasicCredentials()
        throws IOException {
        String credentials = getRequestProperty("authorization");
        if (credentials == null) return false;
        StringTokenizer st = new StringTokenizer(credentials);
        if (!st.hasMoreTokens() || !st.nextToken().equalsIgnoreCase("basic") || !st.hasMoreTokens()) return false;
        st = new StringTokenizer(new String(new BASE64Decoder().decodeStream(st.nextToken())), ":");
        if (!st.hasMoreTokens()) return false;
        username = st.nextToken();
        password = st.hasMoreTokens() ? st.nextToken() : "";
        return true;
    }

    public void setBasicCredentials(String username, String password) {
        this.username = username;
        this.password = password;
        setRequestProperty("Authorization", basicCredentials(username, password));
    }

    public void setDigestCredentials(String username, String password, String realm, String method, String nonce, String uri)
        throws NoSuchAlgorithmException {
        this.username = username;
        this.password = password;
        setRequestProperty("Authorization", digestCredentials(username, password, realm, method, nonce, uri));
    }

    public HeaderList getHeaderList() {
        return headerList;
    }

    /** Gets appropriate fields from first headers to second headers and
        checks header fields for partial message and decoding.
        @return true if header is first or only one of possible multiple parts. */
    public boolean getHeader(InputStream source, HeaderList headerFields, HeaderList headerFields1, boolean html)
        throws IOException, ParseException {
        if (verbose)
            headerFields.dump(logStream);
        partial = false;
        String contentType = headerFields.getHeaderValue("content-type");
        if (contentType != null) {
            Map<String, String> params = new HashMap<String, String>();
            if (Support.getParameters(contentType, params).equalsIgnoreCase(Support.partialType)) {
                this.params = params;
                partial = true;
                number = 0;
                String s = params.get("number");
                if (s != null)
                    try {
                        number = Integer.parseInt(s);
                    } catch (NumberFormatException ex) {}
            }
        }
        if (headerFields1 != null) {
            headerFields1.replace(new Header("Content-Type", (html ? Support.htmlType : Support.plainType) + "; charset=" + charsetName));
            if (partial && source != null) {
                HeaderList headerFields2 = new HeaderList(source);
                decoder = Support.getDecoder(headerFields2, headerFields1);
            } else decoder = Support.getDecoder(headerFields, headerFields1);
        }
        headerFields.removeAll("content-length");
        headerFields.removeAll("content-transfer-encoding");
        if ((subject = headerFields.getHeaderValue("subject")) != null && (subject = Support.decodeWords(subject)).equals("")) subject = null;
        return partial ? number == 1 : subject != null && (title = getNumbers(subject)) != null ? number == 1 : true;
    }

    public void decode(InputStream in, OutputStream out, W3Requester requester, HeaderList headerList, HeaderList headerFields, Decoder decoder, String filePath, String request, String path, boolean header, boolean delete, boolean form, boolean html)
        throws IOException, ParseException {
        OutputStream htmlOut;
        if (html) {
            String s = headerList.getHeaderValue("subject");
            if (s == null) s = request;
            Support.writeBytes(out, "<html><head><title>" + s + "</title>\n", charsetName);
            Support.writeBytes(out, "<base target=\"another\">\n", charsetName);
            Support.writeBytes(out, "</head><body>\n", charsetName);
            htmlOut = Support.getHtmlOutputStream(out, servletPathTable, charsetName);
        } else htmlOut = null;
        if (header) Support.writeHeader(out, htmlOut, headerList, charsetName, html);
        if (html && request != null) {
            if (!header) Support.writeBytes(out, "<a href=\"" + request + "&header=\" target=message>" + Support.htmlString(messages.getString("showHeader")) + "</a><br>\n", charsetName);
            if (delete) Support.writeBytes(out, "<a href=\"" + request + "&delete=\" target=message>" + Support.htmlString(messages.getString("deleteItem")) + "</a><br>\n", charsetName);
            if (form) {
                Support.writeBytes(out, "<a href=\"" + request + "&form=\" target=composition>" + Support.htmlString(messages.getString("sendReply")) + "</a><br>\n", charsetName);
                Support.writeBytes(out, "<a href=\"" + request + "&form=&also=\" target=composition>" + Support.htmlString(messages.getString("sendReplyToAll")) + "</a><br>\n", charsetName);
            }
            Support.writeBytes(out, "<a href=\"" + request + "&forward=\" target=source>" + Support.htmlString(messages.getString("showSource")) + "</a><br>\n", charsetName);
        }
        if (html && (header || request != null)) Support.writeBytes(out, "<hr>\n", charsetName);
        Support.decodeBody(requester instanceof W3JarRequester ? in : new FeederInputStream(this, in, requester), out, htmlOut, headerList, headerFields, decoder, charsetName, path, requester instanceof W3JarRequester ? (EntityMemory)new EntityArchive(filePath) : (EntityMemory)new EntityCache(filePath), html, true, false, null);
        if (html) {
            htmlOut.write('\n');
            htmlOut.flush();
            Support.writeBytes(out, "</body></html>\n", charsetName);
        }
        out.flush();
    }

    /** Returns true if connection may be kept alive. */
    public boolean mayKeepAlive() {
        return mayKeepAlive;
        //return mayKeepAlive && (requester == null || requester.isConnected());
    }

    public boolean mayPipeline() {
        return mayPipeline;
    }

    boolean getCacheFile(boolean cacheHeaders)
        throws IOException {
        try {
            headerFields = new HeaderList();
            setResponse(HTTP_OK, "OK", false, false);
            if (cacheHeaders) {
                if (!cacheHeadersFile.exists() || cacheHeadersFile.isWriteable()) return false;
                InputStream in0 = null;
                try {
                    in0 = new BufferedInputStream(new FileInputStream(cacheHeadersFile));
                    Support.copyHeaderList(headerList = new HeaderList(in0), headerFields);
                } catch (ParseException ex) {
                    throw new IOException(Support.stackTrace(ex));
                } finally {
                    try {
                        if (in0 != null) in0.close();
                    } catch (IOException ex1) {}
                }
            }
            return true;
        } catch (IOException ex) {
            if (ex instanceof FileNotFoundException)
                if (checkCaches) throw new IOException("Cache copy not found");
                else return false;
            throw ex;
        }
    }

    boolean setCacheFile(boolean cacheHeaders)
        throws IOException {
        setResponse(HTTP_OK, "OK", false, false);
        try {
            in = origin = new BufferedInputStream(new FileInputStream(cacheFile));
        } catch (IOException ex) {
            try {
                if (in != null) in.close();
            } catch (IOException ex1) {}
            finally {
                in = null;
            }
            if (ex instanceof FileNotFoundException)
                if (checkCaches) throw new IOException("Cache copy not found");
                else return false;
            throw ex;
        }
        if (cacheFileLastModified > 0L) headerFields.replace(new Header("Last-Modified", Support.format(new Date(lastModified = cacheFileLastModified))));
        long ctm = System.currentTimeMillis(),
            cacheFileAge = parseLong(headerFields.getHeaderValue("age"), 0L),
            dateValue = parseDate(headerFields.getHeaderValue("date"), -1L);
        if (dateValue != -1L) {
            long delta = ctm - dateValue;
            if (delta > 0L) cacheFileAge += delta;
        } else cacheFileAge = 0L;
        //if (requestTime != 0L) cacheFileAge += ctm - requestTime;
        long age = (cacheFileAge + 500L) / 1000L;
        headerFields.replace(new Header("Age", age));
        if (age > 24L * 60L * 60L)
            headerFields.append(new Header("Warning", "113 Keppi \"Heuristic expiration\""));
        cached = connected = inputDone = true;
        in = checkContentEncoding(in);
        return true;
    }

    void appendAll(HeaderList headerList, String name, List<String> values) {
        Iterator<String> valueIter = values.iterator();
        while (valueIter.hasNext())
            headerList.append(new Header(name, valueIter.next()));
    }

    void setResponse(int responseCode, String responseMessage, boolean filterHeaders, boolean isWeak) {
        headerFields.replace(new Header("", protocol + " " + responseCode + " " + responseMessage));
        if (filterHeaders)
            for (int index = headerFields.size() - 1; index > 0; index--) {
                Header header = headerFields.getHeader(index);
                String key = header.getName().toLowerCase();
                if (!key.equals("date") && !key.equals("etag") && !key.equals("content-location") && (!key.equals("cache-control") && !key.equals("expires") && !key.equals("vary") || isWeak)) headerFields.remove(index);
            }
        status = -1;
    }

    void setNotModified(boolean isWeak) {
        setResponse(HTTP_NOT_MODIFIED, "Not Modified", true, isWeak);
        cached = false;
    }

    void setPreconFailed(boolean isWeak) {
        setResponse(HTTP_PRECON_FAILED, "Precondition failed", true, isWeak);
        cached = false;
    }

    boolean reloadNeeded() {
        boolean noCache = false;
        long maxAge = -1L;
        List<String> pragmaList = requestHeaderFields.get((Object)"pragma"),
            cacheControlList = requestHeaderFields.get((Object)"cache-control");
        if (!pragmaList.isEmpty() || !cacheControlList.isEmpty()) {
            Iterator valueIter = new IteratorSequence(new Iterator[] {pragmaList.iterator(), cacheControlList.iterator()}, false);
            while (valueIter.hasNext()) {
                StringTokenizer st = new StringTokenizer((String)valueIter.next(), ",=", true);
                while (st.hasMoreTokens()) {
                    String directive = st.nextToken().trim().toLowerCase();
                    if (directive.equals(",")) continue;
                    boolean quoted = false;
                    StringBuffer buffer = new StringBuffer();
                    while (st.hasMoreTokens()) {
                        String token = st.nextToken(), trimmedToken = token.trim();
                        if (quoted) {
                            if (trimmedToken.endsWith("\"")) quoted = false;
                        } else if (trimmedToken.startsWith("\"") && !trimmedToken.endsWith("\"")) quoted = true;
                        if (!quoted)
                            if (trimmedToken.trim().equals(",")) break;
                            else if (buffer.length() == 0 && !directive.equals("=") && trimmedToken.equals("=")) continue;
                        buffer.append(token);
                    }
                    String value = buffer.length() > 0 ? buffer.toString() : null;
                    if (directive.equals("no-cache")) noCache = true;
                    else if (directive.equals("max-age")) {
                        if (value == null) continue;
                        maxAge = parseLong(value, -1L);
                    }
                }
            }
        }
        return noCache || maxAge == 0L;
    }

    final boolean checkHeader(String name, boolean isOk) {
        String value = getHeaderField(name),
            cacheValue = cacheHeaderList.getHeaderValue(name);
        return isOk && (value != null) != (cacheValue != null) ||
            value != null && !value.equals(cacheValue);
    }

    boolean checkVary()
        throws IOException {
        List<String> varyFields = headerFields.get((Object)"vary");
        if (varyFields.isEmpty()) return true;
        Iterator<String> varyFieldIter = varyFields.iterator();
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException ex) {
            throw new IOException(Support.stackTrace(ex));
        }
        while (varyFieldIter.hasNext()) {
            String vary = varyFieldIter.next().trim();
            if (vary.equals("*")) {
                cacheFile = null;
                return false;
            }
            StringTokenizer fieldNames = new StringTokenizer(vary, ",");
            while (fieldNames.hasMoreElements()) {
                List<String> values = requestHeaderList.get((Object)fieldNames.nextToken().trim());
                Iterator<String> valueIter = values.iterator();
                StringBuffer value = new StringBuffer();
                while (valueIter.hasNext())
                    value.append(valueIter.next()).append("\r\n");
                value.append('\u0000');
                messageDigest.update(value.toString().getBytes("UTF-8"));
            }
        }
        byte digest[] = messageDigest.digest();
        varyId = new BigInteger(digest).toString(Character.MAX_RADIX);
        return true;
    }

    boolean checkOffline()
        throws IOException {
        if (isPartialRequest) {
            cacheFile = null;
            return false;
        }
        if (!(this instanceof W3HttpURLConnection) &&
            !(this instanceof W3FtpURLConnection) && !usingProxy()) return false;
        if (method != null && method.equals("HEAD")) return false;
        rangeStart = cacheFile.length();
        if (rangeStart > 0L) setRequestProperty("Range", "bytes=" + rangeStart + "-");
        unsetRequestProperty("if-modified-since");
        unsetRequestProperty("if-none-match");
        return false;
    }

    /** Checks if file can be found in protocol specific cache.
        Content is considered to be dynamic, if it is fetched with POST-method,
        URL has query parameters or path starts with string /cgi-bin.
        In this case cache is filled only if flag forceCaches is set,
        and cache is read only if flag checkCaches is set. */
    final boolean checkCacheFile(String cachePath, int defaultPort)
        throws IOException {
        return checkCacheFile(cachePath, defaultPort, null);
    }

    boolean checkCacheFile(String cachePath, int defaultPort, URL url)
        throws IOException {
        this.cachePath = cachePath;
        this.defaultPort = defaultPort;
        String path, query;
        boolean isPurge;
        if (url != null) {
            path = url.getPath();
            query = url.getQuery();
            path = Support.getParameters(path, params = new HashMap<String, String>());
            if (path.equals("")) path = "/";
            isPurge = true;
        } else {
            path = this.path;
            query = this.query;
            setIfModifiedSince(getRequestPropertyDate("if-modified-since", 0L));
            isPurge = false;
        }
        boolean isDynamic = false;
        if (!isPurge && (!useCaches && !autoResume || (isDynamic = query != null || path.startsWith("/cgi-bin/")) && !checkCaches && !forceCaches)) return cached = false;
        if (url == null) url = this.url;
        cachePath = (cachePath + (url.getPort() != -1 && url.getPort() != defaultPort ? url.getHost() + "#" + url.getPort() : url.getHost()).toLowerCase() + Support.decodePath(path)).replace('/', File.separatorChar);
        if (File.separatorChar == '\\') {
            int colon = -1, nColons = 0;
            for (;;) {
                colon = cachePath.indexOf(':', colon + 1);
                if (colon == -1) break;
                if (++nColons <= 1) continue;
                cachePath = cachePath.substring(0, colon) + "#" + cachePath.substring(colon + 1);
            }
            cachePath = cachePath.replace('*', '#');
        }
        boolean isIndexFile = false;
        if (query == null) {
            cacheDir = new W3File(cachePath);
            if (!path.endsWith("/")) {
                // If end user is requesting index file without trailing slash from cache,
                // request must be redirected appending slash to URL. Otherwise links in that page can be broken.
                W3File indexCacheFile;
                cacheFile = new W3File(cachePath);
                if (cacheFile.exists() && !cacheFile.isFile() &&
                    (indexCacheFile = new W3File(cachePath + File.separator + "index.html")).exists()) {
                    cacheFile = indexCacheFile;
                    isIndexFile = true;
                } else {
                    int slash = path.lastIndexOf('/');
                    if (path.indexOf('.', slash + 1) == -1)
                        cacheFile = new W3File(cachePath + File.separator + "index.htm");
                }
            } else cacheFile = new W3File(cachePath + "index.html");
        } else {
            // When dynamic content is saved to cache and url length is longer than
            // underlying file system supports, URL string is truncated.
            String queryPart = "#" + Support.encodePath(query);
            int maxFilenameLength = W3File.getMaxFilenameLength() / 2,
                cacheNameLength = new W3File(cachePath).getName().length();
            for (int i = 0; cacheNameLength + queryPart.length() > maxFilenameLength;)
                if ((i = query.indexOf('&', i)) == -1) {
                    queryPart = "";
                    break;
                } else queryPart = "#" + Support.encodePath("?" + query.substring(++i));
            cacheDir = new W3File(cachePath + queryPart);
            // Some abnormal URLs contain other URLs in query part. This check ensures that directories are made correctly for such URLs.
            int i = query.lastIndexOf('/');
            if (i != -1 && query.indexOf('.', i + 1) == -1 ||
                query.indexOf('&') == -1 && query.indexOf('=') == -1 && query.indexOf('.') == -1) {
                query = query.replace('/', File.separatorChar);
                cacheDir = new W3File(cachePath + queryPart);
                query += query.endsWith(File.separator) ? "index.html" : File.separator + "index.html";
            }
            cacheFile = new W3File(cachePath + queryPart);
            if (!path.endsWith("/")) {
                W3File cacheFile1;
                if (!cacheFile.exists() && (cacheFile1 = new W3File(cachePath + File.separator + queryPart)).exists()) cacheFile = cacheFile1;
            }
        }
        cacheHeadersFile = new W3File(cacheFile.getPath() + "#");
        File varyCacheFiles[] = null;
        boolean checkVary = false;
        if (!isPurge) {
            if (!getCacheFile(cacheHeaders) || !checkVary()) return false;
            if (varyId != null) {
                if (cacheDir.exists()) {
                    varyCacheFiles = cacheDir.listFiles(this);
                    if (varyCacheFiles.length > 0) checkVary = true;
                }
                cacheFile = new W3File(cacheDir.getPath() + File.separator + varyId);
            }
        }
        boolean isUsable = cacheFile.exists() && !cacheFile.isWriteable(),
            isOffline = isUsable && cacheFile.isOffline();
        if (isUsable) cacheFileLastModified = cacheFile.lastModified();
        isPartialRequest = requestHeaderFields.hasHeader("range");
        requestHeaderFields.removeAll("range");
        eTagMatches = false;
        String ifNoneMatch = null, ifRange = getRequestProperty("if-range");
        long ctm = System.currentTimeMillis(), expirationTime0 = -1L;
        boolean isWeak = false;
        if (checkCaches) {
            if (!isUsable || isOffline) throw new IOException((cacheFile.exists() ? "Cache copy is not usable" : "Cache copy not found") + " " + cacheFile.getCanonicalPath());
        } else if (doOutput || method != null && method.equals("DELETE")) {
            closeStreams();
            cacheFile.delete();
            cacheFile = null;
            return false;
        } else if (isOffline) {
            return checkOffline();
        } else {
            if (!isUsable && !checkVary) return false;
            boolean noCache = false, noStore = false;
            long maxAge = -1L, minFresh = -1L, maxStale = -1L;
            List<String> pragmaList = requestHeaderFields.get((Object)"pragma"),
                cacheControlList = requestHeaderFields.get((Object)"cache-control");
            if (!pragmaList.isEmpty() || !cacheControlList.isEmpty()) {
                Iterator valueIter = new IteratorSequence(new Iterator[] {pragmaList.iterator(), cacheControlList.iterator()}, false);
                while (valueIter.hasNext()) {
                    StringTokenizer st = new StringTokenizer((String)valueIter.next(), ",=", true);
                    while (st.hasMoreTokens()) {
                        String directive = st.nextToken().trim().toLowerCase();
                        if (directive.equals(",")) continue;
                        boolean quoted = false;
                        StringBuffer buffer = new StringBuffer();
                        while (st.hasMoreTokens()) {
                            String token = st.nextToken(), trimmedToken = token.trim();
                            if (quoted) {
                                if (trimmedToken.endsWith("\"")) quoted = false;
                            } else if (trimmedToken.startsWith("\"") && !trimmedToken.endsWith("\"")) quoted = true;
                            if (!quoted)
                                if (trimmedToken.equals(",")) break;
                                else if (buffer.length() == 0 && !directive.equals("=") && trimmedToken.equals("=")) continue;
                            buffer.append(token);
                        }
                        String value = buffer.length() > 0 ? buffer.toString() : null;
                        if (directive.equals("no-cache")) noCache = true;
                        else if (directive.equals("no-store")) noStore = true;
                        else if (directive.equals("max-age")) {
                            if (value == null) continue;
                            maxAge = parseLong(value, -1L);
                        } else if (directive.equals("max-stale")) {
                            maxStale = value != null ? parseLong(value, -1L) : Long.MIN_VALUE;
                        } else if (directive.equals("min-fresh")) {
                            if (value == null) continue;
                            minFresh = parseLong(value, -1L);
                        }
                    }
                }
            }
            if (noCache) {
                cacheFile = null;
                return false;
            }
            if (noStore) {
                closeStreams();
                cacheFile.delete();
                cacheFile = null;
                return false;
            }
            if (!isUsable && checkVary) {
                ifNoneMatch = getRequestProperty("if-none-match");
                Set<String> eTagSet = new HashSet<String>();
                StringBuffer buffer = new StringBuffer();
                if (ifNoneMatch != null) {
                    StringTokenizer st = new StringTokenizer(ifNoneMatch, ",");
                    while (st.hasMoreTokens())
                        eTagSet.add(st.nextToken().trim());
                    buffer.append(ifNoneMatch);
                }
                HeaderList varyHeaderList = null;
                for (int fileIndex = 0; fileIndex < varyCacheFiles.length; fileIndex++) {
                    InputStream in0 = null;
                    try {
                        in0 = new BufferedInputStream(new FileInputStream(varyCacheFiles[fileIndex]));
                        varyHeaderList = new HeaderList(in0);
                    } catch (IOException ex) {
                        continue;
                    } catch (ParseException ex) {
                        continue;
                    } finally {
                        try {
                            if (in0 != null) in0.close();
                        } catch (IOException ex1) {}
                    }
                    String eTag1 = varyHeaderList.getHeaderValue("etag");
                    if (eTag1 != null) {
                        eTag1 = eTag1.trim();
                        if (!eTagSet.contains(eTag1)) {
                            if (buffer.length() > 0) buffer.append(',');
                            buffer.append(eTag1);
                            eTagSet.add(eTag1);
                            cacheSetIfNoneMatch = true;
                        }
                    }
                }
                if (cacheSetIfNoneMatch) {
                    ifNoneMatch = buffer.toString();
                    setRequestProperty("If-None-Match", ifNoneMatch);
                }
                return false;
            }
            long sMaxAge = -1L;
            boolean isPublic = false;
            pragmaList = headerFields.get((Object)"pragma");
            cacheControlList = headerFields.get((Object)"cache-control");
            if (!pragmaList.isEmpty() || !cacheControlList.isEmpty()) {
                Iterator valueIter = new IteratorSequence(new Iterator[] {pragmaList.iterator(), cacheControlList.iterator()}, false);
                while (valueIter.hasNext()) {
                    StringTokenizer st = new StringTokenizer((String)valueIter.next(), ",=", true);
                    while (st.hasMoreTokens()) {
                        String directive = st.nextToken().trim().toLowerCase();
                        if (directive.equals(",")) continue;
                        boolean quoted = false;
                        StringBuffer buffer = new StringBuffer();
                        while (st.hasMoreTokens()) {
                            String token = st.nextToken(), trimmedToken = token.trim();
                            if (quoted) {
                                if (trimmedToken.endsWith("\"")) quoted = false;
                            } else if (trimmedToken.startsWith("\"") && !trimmedToken.endsWith("\"")) quoted = true;
                            if (!quoted)
                                if (trimmedToken.equals(",")) break;
                                else if (buffer.length() == 0 && !directive.equals("=") && trimmedToken.equals("=")) continue;
                            buffer.append(token);
                        }
                        String value = buffer.length() > 0 ? buffer.toString() : null;
                        if (directive.equals("no-cache")) noCache = true;
                        else if (directive.equals("max-age")) {
                            if (value == null) continue;
                            long maxAge1 = parseLong(value, -1L);
                            if (maxAge1 != -1L && (maxAge == -1L || maxAge1 < maxAge))
                                maxAge = maxAge1;
                        } else if (directive.equals("s-maxage")) {
                            if (value == null) continue;
                            sMaxAge = parseLong(value, -1L);
                            mustRevalidate = true;
                        } else if (directive.equals("must-revalidate") || directive.equals("proxy-revalidate")) mustRevalidate = true;
                        else if (directive.equals("public")) isPublic = true;
                    }
                }
            }
            if (maxAge == 0L || sMaxAge == 0L) noCache = true;
            long cacheFileTime = parseLong(headerFields.getHeaderValue("age"), 0L),
                dateValue = parseDate(headerFields.getHeaderValue("date"), -1L);
            if (dateValue == -1L) dateValue = ctm;
            cacheFileTime = dateValue - cacheFileTime;
            String s = headerFields.getHeaderValue("expires");
            Date d = s != null && !s.equals("0") ? parse(s) : null;
            long expires = s != null ? d != null ? d.getTime() : 0L : -1L,
                refreshingInterval = getCacheRefreshingInterval(),
                expirationTime = expires;
            if (sMaxAge != -1L) expirationTime = cacheFileTime + sMaxAge * 1000L;
            else if (maxAge != -1L) expirationTime = cacheFileTime + maxAge * 1000L;
            if (expirationTime == -1L && refreshingInterval != -1L && !isDynamic) expirationTime = cacheFileTime + refreshingInterval;
            expirationTime0 = expirationTime;
            if (expirationTime != -1L) {
                if (minFresh != -1L) expirationTime -= minFresh * 1000L;
                if (maxStale == Long.MIN_VALUE && !mustRevalidate) expirationTime = -1L;
                else if (maxStale != -1L) expirationTime += maxStale * 1000L;
            }
            if (expirationTime != -1L) {
                if (ctm > expirationTime) noCache = true;
            } else if (isDynamic) noCache = true;
            if (requestHeaderFields.containsKey("authorization") && sMaxAge == -1 && !mustRevalidate && !isPublic) {
                closeStreams();
                cacheFile.delete();
                cacheFile = null;
                return false;
            }
            String eTag = headerFields.getHeaderValue("etag"),
                ifMatch = getRequestProperty("if-match");
            if (ifMatch != null) {
                StringTokenizer st = new StringTokenizer(ifMatch, ",");
                while (st.hasMoreTokens()) {
                    String eTag1 = st.nextToken().trim();
                    if (!eTag1.startsWith("W/") && eTag1.equals(eTag) || eTag1.equals("*")) {
                        eTagMatches = true;
                        break;
                    }
                }
                if (!eTagMatches && !noCache) {
                    setPreconFailed(false);
                    return connected = inputDone = true;
                }
            }
            long ifUnmodifiedSince = getRequestPropertyDate("if-unmodified-since", -1L);
            if (ifUnmodifiedSince != -1L) {
                if ((cacheFileLastModified > ifUnmodifiedSince || isPartialRequest) && !noCache) {
                    setPreconFailed(false);
                    return connected = inputDone = true;
                }
            } else if (!retried) {
                if (eTag != null) {
                    boolean canBeWeak = method != null && method.equals("GET") && !isPartialRequest;
                    ifNoneMatch = getRequestProperty("if-none-match");
                    if (ifNoneMatch != null) {
                        boolean wasWeak = false;
                        if (eTag != null && eTag.startsWith("W/"))
                            if (canBeWeak) {
                                eTag = eTag.substring(2);
                                isWeak = true;
                            } else wasWeak = true;
                        StringTokenizer st = new StringTokenizer(ifNoneMatch, ",");
                        while (st.hasMoreTokens()) {
                            String eTag1 = st.nextToken().trim();
                            if (canBeWeak && eTag1.startsWith("W/")) {
                                eTag1 = eTag1.substring(2);
                                isWeak = true;
                            }
                            if (eTag1.equals(eTag) || eTag1.equals("*")) {
                                eTagMatches = true;
                                break;
                            }
                        }
                        if (!eTagMatches) {
                            ifNoneMatch += "," + eTag;
                            setRequestProperty("If-None-Match", ifNoneMatch);
                            cacheSetIfNoneMatch = true;
                        } else if (!noCache && ifRange == null && (!isPartialRequest || !wasWeak) && (ifModifiedSince <= 0L || ifModifiedSince >= cacheFileLastModified)) {
                            if (method != null && (method.equals("GET") || method.equals("HEAD"))) setNotModified(isWeak);
                            else setPreconFailed(isWeak);
                            return connected = inputDone = true;
                        }
                    }
                }
                if (cacheFileLastModified > ifModifiedSince) {
                    // Cache file may be returned
                    setRequestProperty("If-Modified-Since", Support.format(new Date(cacheFileLastModified)));
                    cacheSetIfModifiedSince = true;
                }
            }
            if (noCache) return false;
        }
        if (isIndexFile) {
            String location = url.toString() + "/";
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            String title = HTTP_MOVED_PERM + " " + Support.htmlString(messages.getString("movedPermanently"));
            Support.writeBytes(bout, "<html><head><title>" + title + "</title></head><body>\n", charsetName);
            Support.writeBytes(bout, "<h1>" + title + "</h1>\n", charsetName);
            Support.writeBytes(bout, location + "\n", charsetName);
            Support.writeBytes(bout, "</body></html>", charsetName);
            byte b[] = bout.toByteArray();
            headerFields = new HeaderList();
            setResponse(HTTP_MOVED_PERM, "Moved Permanently", false, false);
            headerFields.append(new Header("Location", location));
            headerFields.append(new Header("Content-Type", Support.htmlType));
            headerFields.append(new Header("Content-Length", String.valueOf(b.length)));
            in = new ByteArrayInputStream(b);
            return connected = inputDone = true;
        }
        if (!isPartialRequest && !cacheSetIfModifiedSince && !cacheSetIfNoneMatch && ifRange == null && (eTagMatches ? ifModifiedSince <= 0L || ifModifiedSince >= cacheFileLastModified : ifNoneMatch == null && ifModifiedSince > 0L && ifModifiedSince >= cacheFileLastModified)) setNotModified(isWeak);
        else if (ifNoneMatch != null && ifModifiedSince > 0L && isPartialRequest) return false;
        else if (!setCacheFile(cacheHeaders)) return false;
        if (expirationTime0 != -1L && ctm > expirationTime0)
            headerFields.append(new Header("Warning", "110 Keppi \"Response is stale\""));
        return connected = inputDone = true;
    }

    /** Checks if file can be found in circulation cache. */
    boolean checkCacheFile(String filePath, String entryName)
        throws IOException {
        setIfModifiedSince(getRequestPropertyDate("if-modified-since", 0L));
        if (doOutput) return false;
        cacheFile = new W3File(filePath + "_" + entryName);
        boolean isUsable = cacheFile.exists() && !cacheFile.isOffline() && !cacheFile.isWriteable();
        if (!isUsable)
            if (!checkCaches || forceCaches || entryName.equals("")) return false;
            else throw new IOException(cacheFile.exists() ? "Cache copy is not usable" : "Cache copy not found");
        cacheFileLastModified = cacheFile.lastModified();
        cacheHeadersFile = new W3File(cacheFile.getPath() + "#");
        if (ifModifiedSince > 0L && ifModifiedSince >= cacheFileLastModified) setNotModified(false);
        else if (!getCacheFile(true) || !setCacheFile(true)) return false;
        return connected = inputDone = true;
    }

    /** Checks if cache can be used after entity headers are read. */
    boolean checkCacheUse(boolean cacheHeaders, boolean isCheck)
        throws IOException {
        if (!useCaches && !autoResume || checkCaches) {
            cacheFile = null;
            return false;
        }
        // When end user issued partial request, control should never come here,
        // only when this class itself explicitly issued partial request.
        boolean isOk = getResponseCode() == HTTP_OK;
        if (!isOk || method != null && method.equals("HEAD")) isCheck = true;
        if (useCaches) {
            if (!checkVary()) return false;
            if (varyId != null) cacheFile = new W3File(cacheDir.getPath() + File.separator + varyId);
            if (forceCaches) {
                final String contentDisposition = getHeaderField("content-disposition");
                if (contentDisposition != null) {
                    final Map<String, String> contentDispositionParams
                        = new HashMap<String, String>();
                    Support.getParameters(contentDisposition, contentDispositionParams);
                    final String filename = contentDispositionParams.get("filename");
                    if (filename != null) {
                        int lastSlash = cachePath.lastIndexOf('/');
                        if (lastSlash != -1) {
                            cacheFile = new W3File(cacheDir.getParent(), filename);
                        }
                    }
                }
            }
            if (cacheFile == null) return false;
            if (isPartialRequest || getHeaderField("set-cookie") != null) return false;
            boolean isOffline = false;
            if (cacheFile.exists()) {
                // When cache file exists, it can be obsolete or offline (only partially read)
                isOffline = cacheFile.isOffline();
                if (isOffline) {
                    // Remote server maybe does not support partial retrieves and response is whole entity
                    if (isOk) isOffline = false;
                    else if (getResponseCode() == HTTP_PARTIAL)
                        if (rangeStartReceived <= rangeStart &&
                            (forceResume || cacheFileLastModified == getLastModified()) &&
                            (maxCacheEntryLength < 0 || rangeLength < maxCacheEntryLength)) {
                            // In case Content-Range header is not present, assumes that partial response corresponds to request
                            if (verbose) log("Proceeding with partial get");
                            setResponse(HTTP_OK, "OK", false, false);
                            headerFields.removeAll("content-range");
                            headerFields.replace(new Header("Content-Length", rangeLength));
                            status = -1;
                            // Partial response is incorrect, original entity has been changed or entity size exceeds limit
                        } else isOffline = false;
                    // Response is out of context (no complete or partial response)
                    else return false;
                } else if (cacheHeaderList != null) {
                    long dateValue = getHeaderFieldDate("date", -1L);
                    if (dateValue != -1L) {
                        String cacheDate = cacheHeaderList.getHeaderValue("date");
                        if (cacheDate != null) {
                            long cacheDateValue = parseDate(cacheDate, -1L);
                            if (cacheDateValue > dateValue && getCacheFile(cacheHeaders)) {
                                mayKeepAlive = false;
                                return true;
                            }
                        }
                    }
                }
                // If cache file can't be deleted, it is possibly in use by other thread
                if (getResponseCode() == HTTP_CREATED) {
                    closeStreams();
                    cacheFile.delete();
                    return false;
                }
                if (cacheHeaderList != null && (checkHeader("content-md5", isOk) || checkHeader("content-length", isOk) || checkHeader("etag", isOk) || checkHeader("last-modified", isOk) || checkHeader("content-location", isOk) || checkHeader("expires", isOk) || checkHeader("vary", isOk) || checkHeader("cache-control", isOk))) {
                    closeStreams();
                    cacheFile.delete();
                    return false;
                }
            }
            if (!isOffline)
                if (getResponseCode() == HTTP_PARTIAL && !retried && getRequestProperty("range") != null) {
                    // Partial response received, but there is no appropriate cache file.
                    // Entity is requested complete.
                    if (verbose) log("Partial get canceled, reissuing get");
                    if (!mayKeepAlive) close();
                    clear();
                    closeStreams();
                    cacheFile.delete();
                    unsetRequestProperty("Range");
                    retried = true;
                    this.in = getInput(false);
                    return false;
                }
            if (isOk) {
                boolean noStore = false;
                List<String> pragmaList = headerFields.get((Object)"pragma"),
                    cacheControlList = headerFields.get((Object)"cache-control");
                if (!pragmaList.isEmpty() || !cacheControlList.isEmpty()) {
                    Iterator valueIter = new IteratorSequence(new Iterator[] {pragmaList.iterator(), cacheControlList.iterator()}, false);
                    while (valueIter.hasNext()) {
                        StringTokenizer st = new StringTokenizer((String)valueIter.next(), ",=", true);
                        while (st.hasMoreTokens()) {
                            String directive = st.nextToken().trim().toLowerCase();
                            if (directive.equals(",")) continue;
                            boolean quoted = false;
                            StringBuffer buffer = new StringBuffer();
                            while (st.hasMoreTokens()) {
                                String token = st.nextToken(), trimmedToken = token.trim();
                                if (quoted) {
                                    if (trimmedToken.endsWith("\"")) quoted = false;
                                } else if (trimmedToken.startsWith("\"") && !trimmedToken.endsWith("\"")) quoted = true;
                                if (!quoted)
                                    if (trimmedToken.equals(",")) break;
                                    else if (buffer.length() == 0 && !directive.equals("=") && trimmedToken.equals("=")) continue;
                                buffer.append(token);
                            }
                            String value = buffer.length() > 0 ? buffer.toString() : null;
                            if (directive.equals("private")) {
                                if (value != null) {
                                    StringTokenizer tokens = new StringTokenizer(Support.unquoteSimpleString(value), ",");
                                    while (tokens.hasMoreTokens())
                                        headerList.removeAll(tokens.nextToken().trim());
                                } else noStore = true;
                            } else if (directive.equals("no-store")) noStore = true;
                        }
                    }
                }
                if (noStore || maxCacheEntryLength >= 0 && getContentLength() > maxCacheEntryLength) {
                    // No store or entity size exceeds limit, cache is not used
                    closeStreams();
                    cacheFile.delete();
                    cacheFile = null;
                    return false;
                }
            }
            // If cache file can't be created, it is possibly created by other thread
            try {
                if (!isOffline) {
                    closeStreams();
                    for (W3File parentFile = cacheFile;;) {
                        String parent = parentFile.getParent();
                        if (parent == null) break;
                        parentFile = new W3File(parent);
                        if (parentFile == null || !parentFile.exists()) break;
                        if (!parentFile.isDirectory()) {
                            parentFile.delete();
                            break;
                        }
                    }
                    if (!cacheFile.exists()) new File(cacheFile.getParent()).mkdirs();
                    if (cacheHeaders) {
                        // writing headers received from remote server to the header file
                        cacheHeadersFile.delete();
                        if (!cacheHeadersFile.createNewFile()) return false;
                        BufferedOutputStream fout0 = new BufferedOutputStream(new FileOutputStream(cacheHeadersFile));
                        Support.sendHeaderList(fout0, headerList);
                        fout0.close();
                        cacheHeadersFile.setLastTimes(System.currentTimeMillis(), lastModified);
                        cacheHeadersFile.setOnline();
                        cacheHeadersFile.setReadOnly();
                    }
                    if (isCheck) return true;
                    cacheFile.delete();
                    if (!cacheFile.createNewFile()) return false;
                } else if (isCheck) return true;
            } catch (IOException ex) {
                if (ex.getMessage() != null && ex.getMessage().equals("File name too long")) {
                    cacheFile = null;
                    return false;
                }
                throw ex;
            }
            checkCacheCleaning();
            // Only auto resume were required, but response is not complete.
            // If response is partial, client itself is maybe handling resuming.
        } else if (cacheFile == null || !isOk) return false;
        if (isCheck) return true;
        if (this.in == null) return false;
        cacheFile.setReadAndWrite();
        // Updating cache or doing auto resume
        if (fillCaches)
            if (W3URLConnection.cachePool != null)
                synchronized (W3URLConnection.cachePool) {
                    filling = (CacheFillingThread)W3URLConnection.cachePool.poolThread;
                    filling.setup(this);
                    in = origin = fillCaches ? null : new BufferedInputStream(filling.in = new PipedInputStream(filling.out = new PipedOutputStream()));
                    W3URLConnection.cachePool.poolThread = W3URLConnection.cachePool.threadPool.reserve(filling);
                }
            else {
                filling = new CacheFillingThread(this);
                in = origin = fillCaches ? null : new BufferedInputStream(filling.in = new PipedInputStream(filling.out = new PipedOutputStream()));
                filling.start();
            }
        else in = origin = new BufferedInputStream(new CacheFillingInputStream(this));
        return true;
    }

    /** Sets time value to cookie named by class name. */
    public void setTime() {
        Cookie cookie = new Cookie(getClass().getName(), Long.toString(System.currentTimeMillis(), Character.MAX_RADIX));
        cookie.setMaxAge(1000 * 24 * 60 * 60);
        headerFields.append(new Header("Set-Cookie", cookieSetHeader(cookie)));
    }

    /** Gets time value in cookie named by class name. */
    public long getTime(String date, Cookie cookies[])
        throws IOException {
        if (date != null && !(date = date.trim()).equals("")) {
            Date dt = parse(date, userDateFormats);
            if (dt == null) throw new IOException("Invalid date (format must be: " + userDateFormats[0].toPattern() + ")");
            return dt.getTime();
        }
        Cookie cookie = getCookie(getClass().getName(), cookies);
        return cookie != null ? Long.parseLong(cookie.getValue(), Character.MAX_RADIX) : 0L;
    }

    /** Gets protocol version of response. */
    public final String getProtocol()
        throws IOException {
        getResponseCode();
        return protocol;
    }

    /** Returns available bytes in input stream. */
    public final int available()
        throws IOException {
        return requester.input.available();
    }

    /** Returns true if returning cached copy of content. */
    public final boolean cached() {
        return cached;
    }

    /** Returns true if filling cache with new or updated content. */
    public final boolean fillingCaches() {
        return filling != null && filling.isAlive();
    }

    /** This method waits until cache is filled. */
    public final void waitForFillingCaches()
        throws InterruptedException {
        if (fillingCaches())
            if (fillCaches) filling.join();
            else mayKeepAlive = false;
    }

    public void setAutoCaches(boolean autoCaches) {
        this.autoCaches = autoCaches;
        setUseCaches(autoCaches);
    }

    public void setAutoResume(boolean autoResume) {
        this.autoResume = autoResume;
    }

    /** If this is set all content including dynamic must come always from cache. */
    public void setCheckCaches(boolean checkCaches) {
        this.checkCaches = checkCaches;
        setUseCaches(checkCaches);
    }

    /** If this is set cache is filled but content is not returned to caller directly.
        Method waitForFillingCaches can be used for waiting while cache is filled. */
    public void setFillCaches(boolean fillCaches) {
        this.fillCaches = fillCaches;
        setUseCaches(fillCaches);
    }

    /** If this is set cache is filled also with dynamic content. */
    public void setForceCaches(boolean forceCaches) {
        this.forceCaches = forceCaches;
        setUseCaches(forceCaches);
    }

    /** If this is set cache filling is forced to resume.
        This is usable only in exceptional cases when downloading individual
        files with ClientRobot was not interrupted under control.
        This flag avoids checking if partial cache file is valid. */
    public void setForceResume(boolean forceResume) {
        this.forceResume = forceResume;
        setUseCaches(forceResume);
    }

    /** Sets string which is prepended to links. */
    public void setRequestRoot(String requestRoot) {
        this.requestRoot = Support.canonizePath(requestRoot);
    }

    public void setServletPathTable(Map<String, String> servletPathTable) {
        this.servletPathTable = servletPathTable;
    }

    void setRequestPropertiesFromQuery()
        throws IOException {
        if (query == null) return;
        Map<String, Object> queryPars = new HashMap<String, Object>();
        Vector<String> keys = new Vector<String>();
        parseQuery(query, queryPars, keys);
        Enumeration keyItems = keys.elements();
        while (keyItems.hasMoreElements()) {
            String key = (String)keyItems.nextElement();
            Object value = queryPars.get(key);
            addRequestProperty(key, Support.encodeWords(value instanceof Vector ? (String)((Vector)value).firstElement() : (String)value));
        }
    }

    /** Resets variables associated with response.
        @param url URL */
    public void reset(URL url) {
        this.url = url;
        clear();
        cacheSetIfModifiedSince = false;
        cacheSetIfNoneMatch = false;
        cached = false;
        cacheFile = null;
        cacheFileLastModified = 0L;
        eTagMatches = false;
        filling = null;
        isPartialRequest = false;
        varyId = null;
        requestHeaderFields = new HeaderList();
        requestHeaderList = null;
        path = url.getPath();
        query = url.getQuery();
        path = Support.getParameters(path, params = new HashMap<String, String>());
        if (path.equals("")) path = "/";
        method = null;
        continueHeaderFieldsList = null;
        mustRevalidate = false;
        retried = false;
    }

    /** Checks URL if connection can be reused.
        @param url URL where scheme, host and port must be equal to last used URL.
        @return true if URL can be set, false if connection can't be reused. */
    public final boolean check(URL url) {
        return requester != null && (!this.url.getProtocol().equals(url.getProtocol()) || !usingProxy() && (!this.url.getHost().equals(url.getHost()) || this.url.getPort() != url.getPort())) ? false : true;
    }

    /** Sets URL to the specified and resets variables associated with request and response.
        @param url URL */
    public void set(URL url) {
        reset(url);
        autoCaches = false;
        autoResume = false;
        checkCaches = false;
        fillCaches = false;
        forceCaches = false;
        forceResume = false;
        hasContentEncoding = false;
        hasTransferEncoding = false;
        redirected = false;
        connecting = false;
        doingInput = false;
        setDoInput(false);
        setDoOutput(false);
        setIfModifiedSince(0L);
        setUseCaches(getDefaultUseCaches());
        requestRoot = null;
    }

    public final String getQuery() {
        return query;
    }

    public InputStream checkContentEncoding(InputStream in)
        throws IOException {
        if (method != null && method.equals("HEAD")) return this.in = in;
        String contentEncoding;
        if ((contentEncoding = getHeaderField("content-encoding")) != null) {
            String acceptEncoding = getRequestProperty("accept-encoding");
            if (acceptEncoding != null) {
                Vector<ListValue> acceptEncodings = getList(acceptEncoding);
                for (int comma, lastComma = contentEncoding.length(); lastComma != -1; lastComma = comma) {
                    comma = contentEncoding.lastIndexOf(',', lastComma);
                    String contentCoding = Support.getParameters(contentEncoding.substring(comma + 1, lastComma), null).trim().toLowerCase();
                    if (contentCoding.equals("gzip") || contentCoding.equals("x-gzip")) {
                        if (acceptEncodings.contains(gzip) || acceptEncodings.contains(xgzip)) continue;
                        in = new GZIPInputStream(in);
                    } else if (contentCoding.equals("deflate") || contentCoding.equals("x-deflate")) {
                        if (acceptEncodings.contains(deflate) || acceptEncodings.contains(xdeflate)) continue;
                        in = new InflaterFillInputStream(in, new Inflater(true));
                    } else if (!contentCoding.equals("identity") && !contentCoding.equals("")) {
                        ListValue encodingListValue = new ListValue(contentEncoding, null, 0.0f);
                        if (!acceptEncodings.contains(encodingListValue)) {
                            int dot = url.getPath().lastIndexOf('.');
                            if (dot == -1 || !url.getPath().substring(dot + 1).trim().equalsIgnoreCase(contentCoding.trim())) {
                                headerFields = new HeaderList();
                                setResponse(HTTP_NOT_ACCEPTABLE, "Not acceptable from content encodings " + acceptEncoding, false, false);
                                return this.in = null;
                            }
                        }
                    }
                    contentEncoding = comma != -1 ? contentEncoding.substring(0, comma).trim() : "";
                }
                if (!contentEncoding.equals("")) {
                    hasContentEncoding = true;
                    Header contentEncodingHeader = new Header("Content-Encoding", contentEncoding);
                    headerFields.replace(contentEncodingHeader);
                    headerList.replace(contentEncodingHeader);
                } else headerFields.removeAll("content-encoding");
                headerFields.removeAll("content-length");
            }
        }
        in = new MarkInputStream(in);
        String ct = getContentType(), ct1 = ct, ct0 = null;
        boolean isOctetStream = false;
        if (ct1 == null || (ct0 = Support.getParameters(ct1, null).toLowerCase()).equals(Support.unknownType) || (isOctetStream = ct0.equals(Support.octetStreamType)))
            ct1 = guessContentTypeFromName(url.getPath());
        if (!isOctetStream && !hasContentEncoding && !hasTransferEncoding) {
            if (ct1 == null)
                try {
                    ct1 = guessContentTypeFromStream(in);
                } catch (IOException ex) {}
            if (ct1 != null) {
                Map<String, String> params = new HashMap<String, String>();
                ct0 = Support.getParameters(ct1, params);
                if (ct0.equalsIgnoreCase(Support.htmlType)) {
                    String charset = params.get("charset");
                    if (charset == null) {
                        ct1 = guessContentTypeFromHtmlStream(in);
                        if (ct1 == null) ct1 = ct0 + "; charset=" + charsetName;
                    }
                }
            }
        }
        //if (ct1 == null && (url.getPath().endsWith("/") || url.getPath().equals(""))) ct1 = Support.htmlType;
        if (ct1 != null && ct1 != ct) headerFields.replace(new Header("Content-Type", contentType = ct1));
        return in;
    }

    public final void purge(String headerName)
        throws IOException {
        if (headerName != null) {
            Iterator<String> fields = headerFields.get((Object)headerName).iterator();
            while (fields.hasNext()) {
                URL locationUrl = new URL(url, fields.next());
                if (url.getHost().equals(locationUrl.getHost()) && url.getPort() == locationUrl.getPort()) checkCacheFile(cachePath, defaultPort, locationUrl);
            }
        } else checkCacheFile(cachePath, defaultPort, url);
    }

    public final boolean mayUseTrailer() {
        boolean useTrailer = false;
        int requestHeaderFieldsLength = requestHeaderFields.size();
        for (int index = 0; index < requestHeaderFieldsLength; index++) {
            Header header = requestHeaderFields.getHeader(index);
            if ("te".equalsIgnoreCase(header.getName())) {
                StringTokenizer tokens = new StringTokenizer(header.getValue());
                while (tokens.hasMoreElements())
                    if (tokens.nextToken().trim().equals("trailers")) {
                        useTrailer = true;
                        break;
                    }
            }
            if (useTrailer) break;
        }
        return useTrailer;
    }

    public InputStream checkMessage(InputStream in, boolean multiple, boolean useCacheHeaders)
        throws IOException, ParseException {
        if (verbose)
            headerFields.dump(logStream);
        status = -1;
        int responseCode = getResponseCode();
        String connection = getHeaderField("connection");
        if (!disablePersistentConnection)
            mayKeepAlive = ((mayPipeline = getProtocol().compareTo(protocol_1_1) >= 0) ?
                            connection == null || !connection.equalsIgnoreCase("close") :
                            (connection != null || (connection = getHeaderField("proxy-connection")) != null) &&
                            connection.equalsIgnoreCase("keep-alive")) &&
                (responseCode == HTTP_NO_CONTENT || responseCode == HTTP_NOT_MODIFIED || responseCode == HTTP_PRECON_FAILED || responseCode == 100 || responseCode == 101 || responseCode == 199 || getHeaderField("content-length") != null || getHeaderField("transfer-encoding") != null);
        String transferEncoding = getHeaderField("transfer-encoding");
        removeConnectionValues(headerFields);
        cacheHeaderList = headerList;
        Support.copyHeaderList(headerFields, headerList = new HeaderList());
        if ((responseCode == HTTP_OK || responseCode == HTTP_NOT_MODIFIED) && cacheHeaderList != null) {
            int headerListLength = cacheHeaderList.size();
            for (int index = 0; index < headerListLength; index++) {
                Header header = cacheHeaderList.getHeader(index);
                String headerKey = header.getName().toLowerCase();
                if ("warning".equals(headerKey) &&
                    !headerList.contains(header)) {
                    String value = header.getValue();
                    StringBuffer cacheValue = new StringBuffer();
                    boolean hasChanges = false;
                    for (int comma = 0, lastComma = -1, lastQuotes = -1, quotes = 0, quotesCount = 0; lastQuotes < value.length(); lastQuotes = quotes) {
                        quotes = value.indexOf('"', lastQuotes + 1);
                        if (quotes == -1) {
                            for (; comma != -1; lastComma = comma) {
                                comma = value.indexOf(',', lastComma + 1);
                                String value1 = comma != -1 ? value.substring(lastComma + 1, comma) : value.substring(lastComma + 1);
                                if (!value1.trim().startsWith("1")) {
                                    if (cacheValue.length() > 0) cacheValue.append(',');
                                    cacheValue.append(value1);
                                } else hasChanges = true;
                            }
                            break;
                        }
                        if (quotes == 0 || value.charAt(quotes - 1) != '\\') {
                            quotesCount++;
                            if (quotesCount == 2) {
                                comma = value.indexOf(',', quotes + 1);
                                if (comma == -1) comma = value.length();
                            } else if (quotesCount > 2) {
                                int length = 0;
                                if (quotes < comma) continue;
                                if (quotesCount > 3) {
                                    if (comma < quotes) {
                                        comma = value.indexOf(',', quotes + 1);
                                        if (comma == -1) comma = value.length();
                                    }
                                } else {
                                    String value1 = value.substring(lastComma + 1, comma);
                                    if (!value1.trim().startsWith("1")) {
                                        if (cacheValue.length() > 0) cacheValue.append(',');
                                        cacheValue.append(value1);
                                    } else hasChanges = true;
                                }
                                comma -= length;
                                lastComma = comma;
                                quotes = comma + 1;
                                quotesCount = 0;
                                continue;
                            }
                        }
                    }
                    if (hasChanges) {
                        value = cacheValue.toString();
                        if (!value.trim().equals("")) 
                            headerList.append(new Header(header.getName(), value));
                    } else headerList.append(header);
                }
            }
        }
        String s = getHeaderField("date");
        Date date = s != null ? parse(s) : null;
        List<Integer> warningIndexList = new ArrayList<Integer>(),
            cacheWarningIndexList = new ArrayList<Integer>();
        int headerFieldsLength = headerFields.size();
        for (int index = 0; index < headerFieldsLength; index++) {
            Header header = headerFields.getHeader(index);
            String headerKey = header.getName().toLowerCase();
            if ("warning".equals(headerKey)) {
                String value = header.getValue();
                StringBuffer cacheValue = new StringBuffer();
                boolean hasChanges = false;
                for (int comma = 0, lastComma = -1, lastQuotes = -1, quotes = 0, quotesCount = 0; lastQuotes < value.length(); lastQuotes = quotes) {
                    quotes = value.indexOf('"', lastQuotes + 1);
                    if (quotes == -1) break;
                    if (quotes == 0 || value.charAt(quotes - 1) != '\\') {
                        quotesCount++;
                        if (quotesCount == 2) {
                            comma = value.indexOf(',', quotes + 1);
                            if (comma == -1) comma = value.length();
                        } else if (quotesCount > 2) {
                            int length = 0;
                            if (quotes < comma) continue;
                            if (quotesCount > 3) {
                                if (comma < quotes) {
                                    comma = value.indexOf(',', quotes + 1);
                                    if (comma == -1) comma = value.length();
                                }
                                Date warningDate = parse(value.substring(lastQuotes + 1, quotes));
                                if (date == null || warningDate == null || !warningDate.equals(date)) {
                                    value = value.substring(0, lastComma + 1) + value.substring(comma);
                                    length = comma - lastComma;
                                    hasChanges = true;
                                }
                            } else {
                                String value1 = value.substring(lastComma + 1, comma);
                                if (cacheValue.length() > 0) cacheValue.append(',');
                                cacheValue.append(value1);
                            }
                            comma -= length;
                            lastComma = comma;
                            quotes = comma + 1;
                            quotesCount = 0;
                            continue;
                        }
                    }
                }
                if (value != header.getValue()) {
                    if (value.trim().equals("")) warningIndexList.add(new Integer(index));
                    else header.setValue(value);
                }
                if (hasChanges) {
                    value = cacheValue.toString();
                    if (value.trim().equals("")) cacheWarningIndexList.add(new Integer(index));
                    else headerList.setHeader(index, new Header(header.getName(), value));
                }
            }
        }
        for (int index = warningIndexList.size() - 1; index >= 0; index--)
            headerFields.remove(warningIndexList.get(index).intValue());
        for (int index = cacheWarningIndexList.size() - 1; index >= 0; index--)
            headerList.remove(cacheWarningIndexList.get(index).intValue());
        // Remove hop-by-hop headers and headers which can be changed after transformation of body part
        // from header list which will be stored in cache file
        headerList.removeAll("keep-alive");
        headerList.removeAll("proxy-authenticate");
        headerList.removeAll("proxy-authorization");
        headerList.removeAll("trailer");
        headerList.removeAll("transfer-encoding");
        headerList.removeAll("upgrade");
        if (!headerList.containsKey("date"))
            headerList.append(new Header("Date", Support.format(new Date())));
        long cacheFileAge = getHeaderFieldLong("age", 0L) * 1000L;
        if (requestTime != 0L) {
            long ctm = System.currentTimeMillis();
            cacheFileAge += ctm - requestTime;
            headerList.replace(new Header("Age", String.valueOf(cacheFileAge)));
        }
        String location = null;
        boolean redirect = false;
        if (responseCode == HTTP_INTERNAL_ERROR && !doOutput) {
            if (!retried) {
                clear();
                if (mayKeepAlive) {
                    byte b[] = new byte[Support.bufferLength];
                    while (in.read(b) > 0);
                } else close();
                List<Header> requestHeaderList = getRequestHeaderFields().getList();
                int length = requestHeaderList.size();
                for (int index = 0; index < length; index++) {
                    Header header = requestHeaderList.get(index);
                    String key = header.getName().toLowerCase();
                    if (key.equals("accept") || key.startsWith("accept-")) {
                        requestHeaderList.remove(index--);
                        length--;
                    }
                }
                retried = true;
                this.in = in = getInput(multiple);
                return in;
            }
        }
        if (!multiple)
            switch (responseCode) {
            case HTTP_MOVED_PERM:
            case HTTP_MOVED_TEMP:
                redirect = true;
                if (!redirected && getInstanceFollowRedirects()) location = getHeaderField("location");
                break;
            case HTTP_NO_CONTENT:
                return this.in = null;
            case HTTP_PRECON_FAILED:
                if (cacheSetIfNoneMatch && checkCacheUse(cacheHeaders, true) && setCacheFile(cacheHeaders)) return this.in;
                headerFields.removeAll("content-length");
                headerFields.removeAll("content-type");
                return this.in = null;
            case HTTP_NOT_MODIFIED:
                // Cache file can be used
                if (cacheSetIfModifiedSince && !eTagMatches || cacheSetIfNoneMatch) {
                    if ((retried || checkCacheUse(cacheHeaders, true)) && setCacheFile(cacheHeaders)) return this.in;
                    if (!retried) {
                        if (verbose) log("Conditional get failed, reissuing get");
                        if (!mayKeepAlive) close();
                        clear();
                        unsetRequestProperty("if-modified-since");
                        unsetRequestProperty("if-none-match");
                        retried = true;
                        this.in = in = getInput(multiple);
                        return in;
                    }
                } else if (!retried) checkCacheUse(cacheHeaders, true);
                headerFields.removeAll("content-length");
                headerFields.removeAll("content-type");
                this.in = in = null;
                break;
            case 100:
            case 101:
            case 199:
                return this.in = null;
            } else if (responseCode != HTTP_OK && responseCode != HTTP_PARTIAL) throw new ParseException("Invalid status " + status + " " + message, 0);
        if (responseCode == HTTP_CREATED || method != null && method.equals("DELETE")) {
            purge(null);
            purge("location");
            purge("content-location");
        }
        long lastModified = getLastModified();
        if (lastModified > 0L) headerFields.replace(new Header("Last-Modified", Support.format(new Date(lastModified))));
        if (in != null) {
            plainIn = in;
            if (transferEncoding != null) {
                headerFields.removeAll("transfer-encoding");
                for (int comma, lastComma = transferEncoding.length();;) {
                    comma = transferEncoding.lastIndexOf(',', lastComma);
                    String transferCoding = Support.getParameters(transferEncoding.substring(comma + 1, lastComma), null).trim().toLowerCase();
                    if (transferCoding.equals("chunked"))
                        in = chunkedIn = new ChunkedInputStream(in);
                    else if (!transferCoding.equals("")) {
                        transferEncoding = transferEncoding.substring(0, lastComma).trim();
                        if (!transferEncoding.equals("")) {
                            Header transferEncodingHeader = new Header("Transfer-Encoding", transferEncoding);
                            headerFields.replace(transferEncodingHeader);
                            headerList.replace(transferEncodingHeader);
                            hasTransferEncoding = true;
                            cacheFile = null;
                        }
                        break;
                    }
                    if (comma == -1) break;
                    lastComma = comma;
                }
                headerFields.removeAll("content-length");
            } else {
                if (getContentLength() != -1) in = limitIn = new LimitInputStream(in, getContentLength());
                else mayKeepAlive = false;
                if (getContentLength() == 0 &&
                    (responseCode == HTTP_NO_CONTENT || responseCode == HTTP_NOT_MODIFIED || responseCode == HTTP_PRECON_FAILED)) {
                    headerFields.removeAll("content-length");
                    headerFields.removeAll("content-type");
                    contentLength = -1;
                }
            }
            String contentType = getContentType();
            if (contentType != null) {
                Map<String, String> params = new HashMap<String, String>();
                if (Support.getParameters(contentType, params).equalsIgnoreCase("multipart/byteranges")) {
                    String boundary = params.get("boundary");
                    if (boundary != null) {
                        boundary = "\r\n--" + boundary + "--\r\n";
                        in = convertIn = new ConvertInputStream(in, boundary, boundary, null, -1, false, false, false, true);
                        headerFields.removeAll("content-length");
                        contentLength = -1;
                    }
                }
            }
            in = checkContentEncoding(in);
            if (in == null) return null;
        }
        if (!multiple && !redirect) {
            /*
              if (!cacheSetIfModifiedSince && getRequestProperty("if-range") == null && ifModifiedSince > 0L && lastModified > 0L && ifModifiedSince >= lastModified) {
              // Not modified response
              setNotModified(false);
              mayKeepAlive = false;
              return this.in;
              }
            */
            if (responseCode != HTTP_OK && responseCode != HTTP_PARTIAL && responseCode != HTTP_NOT_MODIFIED) cacheFile = null;
            else if (cacheSetIfModifiedSince && (checkCaches || lastModified > 0L) && cacheFileLastModified >= lastModified && checkCacheUse(cacheHeaders, true) && setCacheFile(cacheHeaders)) return this.in;
            // Returning cache file
        }
        if (rangeStart > 0L) {
            // If partial content was explicitly requested, content range header is parsed here
            String contentRange = getHeaderField("content-range");
            if (contentRange != null) {
                rangeStartReceived = rangeLength = 0L;
                StringTokenizer st = new StringTokenizer(contentRange), st1;
                if (st.hasMoreTokens() && st.nextToken().equalsIgnoreCase("bytes"))
                    if ((!st.hasMoreTokens() || !(st = new StringTokenizer(st.nextToken(), "/")).hasMoreTokens() ||
                         !(s = st.nextToken().trim()).equals("*") &&
                         (!(st1 = new StringTokenizer(s, "-")).hasMoreTokens() ||
                          (rangeStartReceived = Long.parseLong(st1.nextToken().trim())) < 0L ||
                          !st1.hasMoreTokens() || st1.nextToken().trim().equals("")) ||
                         !st.hasMoreTokens() || !(s = st.nextToken().trim()).equals("*") && (rangeLength = Long.parseLong(s)) < 0L))
                        throw new ParseException("Invalid Content-Range " + contentRange, 0);
            }
        }
        if (location != null) {
            byte b[] = new byte[Support.bufferLength];
            while (in.read(b) > 0);
            URL url1 = new URL(url, location);
            //if (!check(url1)) {
            disconnect();
            reset(url1);
            //}
            redirected = true;
            setRequestProperty("Referer", url.toString());
            if (checking != null) checking.redirecting(this);
            return getInputStream();
        }
        if (multiple) return in;
        this.in = in;
        checkCacheUse(cacheHeaders, false);
        return this.in;
    }

    /** Checks response from remote server. */
    public final InputStream checkMessage(boolean useCacheHeaders)
        throws IOException {
        try {
            return checkMessage(in, false, useCacheHeaders);
        } catch (ParseException ex) {
            throw new IOException(Support.stackTrace(ex));
        }
    }

    /** Checks response from remote server and returns normalized input stream or null if no content. */
    public InputStream checkResponse(InputStream in, boolean multiple)
        throws IOException, ParseException {
        int n;
        for (int countContinued = 0;; countContinued++) {
            headerFields = new HeaderList();
            if ((n = check(in, "HTTP")) == 0) {
                // MIME-response
                HeaderList.parseHeaders(in, headerFields, true);
                String s = getHeaderField("keep-alive");
                if (s != null) {
                    Map<String, String> table = new HashMap<String, String>();
                    Support.parse(s, table, ",", true);
                    s = table.get("timeout");
                    if (s != null) keepAliveTimeoutMillis = Long.parseLong(s.trim()) * 1000L;
                    s = table.get("max");
                    if (s != null) keepAliveCount = Long.parseLong(s.trim());
                }
                InputStream in1 = checkMessage(in, multiple, cacheHeaders);
                if (in1 == null) {
                    int responseCode = getResponseCode();
                    if ((responseCode == 100 || responseCode == 101 || responseCode == 199) && countContinued < 20) {
                        if (continueHeaderFieldsList == null)
                            continueHeaderFieldsList = new ArrayList<HeaderList>();
                        continueHeaderFieldsList.add(headerFields);
                        status = -1;
                        continue;
                    }
                }
                in = in1;
                inputDone = true;
                return in;
            }
            if (n > 0) {
                // response seems to be simple
                if (verbose) log("Received simple response");
                setResponse(HTTP_OK, "OK", false, false);
                String ct = guessContentType(url.getPath());
                headerFields.append(new Header("Content-Type", ct != null ? ct : Support.unknownType));
                Support.copyHeaderList(headerFields, headerList = new HeaderList());
                if (!multiple) this.in = in;
                inputDone = true;
                return in;
            }
            throw new EOFException("Empty response");
        }
    }

    /** Checks response from remote server. */
    public final InputStream checkInput()
        throws IOException {
        doingInput = true;
        closeStreams();
        try {
            return checkResponse(requester.input, false);
        } catch (ParseException ex) {
            throw new IOException(Support.stackTrace(ex));
        }
    }

    public final void check() {
        try {
            if (!connecting) {
                connecting = true;
                if (!connected) {
                    connect();
                    if (getDoOutput())
                        getOutputStream();
                }
            }
            if (connected && !doingInput && !inputDone)
                getInputStream();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        if (!connected) throw new RuntimeException("Not connected");
    }

    public void connect()
        throws IOException {
        doConnect();
        connected = true;
    }

    /** Sets socket connect timeout. */
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    /** Sets socket read timeout. */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setRequestTime(int requestTime) {
        this.requestTime = requestTime;
    }

    public void setClientTime(long clientTime) {
        this.clientTime = clientTime;
    }

    public long getClientTime() {
        return clientTime;
    }

    public long getKeepAliveTimeoutMillis() {
        return keepAliveTimeoutMillis;
    }

    public long getKeepAliveCount() {
        return keepAliveCount;
    }

    public void doConnect()
        throws IOException {
        doConnect(false);
    }

    public void doConnect(boolean multiple)
        throws IOException {
        if (connected) return;
        if (verbose)
            requestHeaderFields.dump(logStream);
        for (int tries = 1; tries <= numberOfTries; tries++) {
            try {
                if (requester == null) open();
                if (requestTime == 0L)
                    requestTime = System.currentTimeMillis();
                Support.sendMessage(requester.output, requestHeaderFields);
                connected = true;
                return;
            } catch (IOException ex) {
                if (verbose) log(ex.toString(), ex);
                close();
                if (tries == numberOfTries) throw ex;
            } catch (ParseException ex) {
                if (verbose) log(ex.toString(), ex);
                close();
                throw new IOException(Support.stackTrace(ex));
            }
            if (verbose) log("Retrying to make connection");
            clear();
        }
        throw new IOException("Peer closed");
    }

    /** Sends request to HTTP server and gets response input. */
    public InputStream getInput(boolean multiple)
        throws IOException {
        doingInput = true;
        if (!connected) connect();
        if (inputDone) return in;
        for (boolean retried = tryOnce;; retried = true)
            try {
                doConnect(multiple);
                InputStream in = checkResponse(requester.input, multiple);
                if (getResponseCode() == HTTP_CLIENT_TIMEOUT && !retried) {
                    close();
                    continue;
                }
                return in;
            } catch (IOException ex) {
                if (!retried) {
                    close();
                    continue;
                }
                if (cacheFile != null && !multiple && !mustRevalidate && getCacheFile(cacheHeaders) && setCacheFile(cacheHeaders)) {
                    headerFields.append(new Header("Warning", "111 Keppi \"Revalidation failed\""));
                    inputDone = true;
                    return this.in;
                }
                throw ex;
            } catch (ParseException ex) {
                throw new IOException(Support.stackTrace(ex));
            }
    }

    public OutputStream getOutput()
        throws IOException {
        boolean useChunked;
        if (getRequestProperty("content-length") == null &&
            getRequestProperty("transfer-encoding") == null) {
            setRequestProperty("Transfer-Encoding", "chunked");
            useChunked = true;
        } else useChunked = false;
        if (!connected) connect();
        if (inputDone) return null;
        return useChunked ? (sink = new ChunkedOutputStream(requester.output)) : requester.output;
    }

    /** Returns true if mobile server is used in connection. */
    public boolean getUseMobile() {
        return useMobile;
    }

    public void complete()
        throws IOException {
    }

    /** Closes streams specific to one request. */
    public void closeStreams() {
        if (convertIn != null) {
            if (convertIn.isEnded())
                try {
                    byte b[] = new byte[Support.bufferLength];
                    if (chunkedIn != null)
                        for (int n; (n = chunkedIn.read(b)) > 0;);
                    else if (limitIn != null)
                        for (int n; (n = limitIn.read(b)) > 0;);
                    else for (int n; (n = plainIn.read(b)) > 0;);
                } catch (IOException ex) {
                    if (verbose) log(ex.toString(), ex);
                }
            else mayKeepAlive = false;
        } else if (chunkedIn != null && chunkedIn.hasLeft() || limitIn != null && limitIn.getRemaining() > 0) mayKeepAlive = false;
        try {
            if (origin != null) {
                origin.close();
                origin = null;
            }
        } catch (IOException ex) {}
        try {
            if (sink != null) {
                sink.close();
                sink = null;
            }
        } catch (IOException ex) {}
        /*
          if (filling != null) {
          if (filling.out != null)
          // Notifies output side of pipe that stream will be closed
          try {
          synchronized (filling.out) {
          filling.out.close();
          filling.out.notifyAll();
          }
          } catch (IOException ex) {}
          filling.interrupt();
          try {
          filling.join();
          } catch (InterruptedException ex) {
          Thread.currentThread().interrupt();
          return;
          }
          }
        */
    }

    public void setCacheCleaningInterval(long cacheCleaningInterval) {
    }

    public void setCacheRefreshingInterval(long cacheRefreshingInterval) {
    }

    public long getCacheRefreshingInterval() {
        return 0L;
    }

    public void setMayKeepAlive(boolean mayKeepAlive) {
        this.mayKeepAlive = mayKeepAlive;
    }

    public void setDisablePersistentConnection(boolean disablePersistentConnection) {
        this.disablePersistentConnection = disablePersistentConnection;
    }

    public String getCharsetName() {
        return charsetName;
    }

    public boolean isConnected() {
        return connected;
    }

    protected void finalize() {
        disconnect();
    }

}

