
package FI.realitymodeler.server;

import FI.realitymodeler.*;
import FI.realitymodeler.common.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;
import java.util.zip.*;
import javax.servlet.*;
import javax.servlet.http.*;

class ByteRange {
    public int firstBytePos;
    public int lastBytePos;

    public String toString() {
        return "{ByteRange: firstBytePos=" + firstBytePos + ",lastBytePos=" + lastBytePos + "}";
    }

}

class ByteRangeComparator implements Comparator<ByteRange> {

    public int compare(ByteRange o1, ByteRange o2) {
        return o1.firstBytePos - o2.lastBytePos;
    }

}

/** HTTP 1.1-request handler which is backwards compatible with older versions.

    Defines an object to provide client request information to a servlet. The servlet
    container creates a ServletRequest object and passes it as an argument to the
    servlet's service method.
    A ServletRequest object provides data including parameter name and values,
    attributes, and an input stream. Interfaces that extend ServletRequest can provide
    additional protocol specific data (for example, HTTP data is provided by
    javax.servlet.http.HttpServletRequest.

    Extends the javax.servlet.ServletRequest interface to provide request information
    for HTTP servlets.
    The servlet container creates an HttpServletRequest object and passes it as an
    argument to the servlet's service methods (doGet, doPost, etc).

    Defines an object to assist a servlet in sending a response to the client. The servlet
    container creates a ServletResponse object and passes it as an argument to the
    servlet's service method.
    To send binary data in a MIME body response, use the ServletOutputStream
    returned by getOutputStream(). To send character data, use the PrintWriter
    object returned by getWriter(). To mix binary and text data, for example, to
    create a multipart response, use a ServletOutputStream and manage the character
    sections manually.
    The charset for the MIME body response can be specified with
    setContentType(String) . For example, "text/html; charset=Shift_JIS". The
    charset can alternately be set using setLocale(Locale). If no charset is specified,
    ISO-8859-1 will be used. The setContentType or setLocale method must
    be called before getWriter for the charset to affect the construction of the writer.
    See the Internet RFCs such as RFC 2045 (http://info.internet.isi.edu/in-notes/rfc/
    files/rfc2045.txt) for more information on MIME. Protocols such as SMTP and
    HTTP define profiles of MIME, and those standards are still evolving.

    Extends the javax.servlet.ServletResponse interface to provide HTTP-specific
    functionality in sending a response. For example, it has methods to access
    HTTP headers and cookies.
    The servlet container creates an HttpServletRequest object and passes it as an
    argument to the servlet's service methods (doGet, doPost, etc).

*/
public class W3Request extends Request implements HttpServletRequest {
    static ByteRangeComparator byteRangeComparator = new ByteRangeComparator();
    static ByteRange maxByteRange = new ByteRange();
    static {
        maxByteRange.firstBytePos = 0;
        maxByteRange.lastBytePos = Integer.MAX_VALUE;
    }

    public W3RequestFacade w3requestFacade = null;
    public W3ResponseFacade w3responseFacade = null;
    public BufferedReader reader = null;
    public Date date = null;
    public Locale locale = null;
    /** Working buffer. */
    public byte buffer[] = new byte[Support.bufferLength];
    public ChannelSocket channelSocket = null;
    /** SSL-object when secure mode is used. */
    public SecureSocketsLayer ssl = null;
    /** Message header store for request received. */
    public Store store = null;
    public Store logStore = null;
    /** Socket input stream. */
    public InputStream in = null;
    public InputStream rawIn = null;
    public InputStream plainIn = null;
    /** Socket output stream. */
    public OutputStream out = null;
    public PrintWriter firstWriter = null;
    public PrintWriter lastWriter = null;
    public Principal userPrincipal = null;
    /** Domain of request. */
    public Domain domain = null;
    public ServletInputStream inputStream = null;
    public String characterEncoding = null;
    public String contentType = null;
    public String contextPath = "";
    /** URL scheme. */
    public String scheme = null;
    /** Request path. */
    public String requestPath = null;
    public String servletPath = null;
    public String pathInfo = null;
    public String normPath = null;
    /** Host specified in URI. */
    public String host = null;
    /** Request url's query part. */
    public String queryString = null;
    /** Remote user name. */
    public String remoteUser;
    public String requestedSessionId = null;
    /** Authentication type. */
    public String authType;
    /** Authorization field. */
    public String authorization;
    /** Account name. */
    public String accountName;
    /** Challenge header. */
    public String challenge = null;
    /** Response protocol. */
    public String protocol = null;
    /** Request URL. */
    public String requestURL = null;
    /** Session set cookie header value */
    public String sessionSetCookie = null;
    /** Attributes of request. */
    public Map<String, Object> attributes = new HashMap<String, Object>();
    /** Query parameters in string arrays. */
    public Map<String, String[]> queryParameters = null;
    /** Parsed query parameters. */
    public Map<String, Object> queryValues = null;
    /** Preserves order of query parameters. */
    public Vector<String> queryKeys = null;
    /** Accept types and parameters. */
    public Vector acceptTypes = null;
    public String accept = null;
    /** Accept character sets. */
    public Vector acceptCharsets = null;
    public String acceptCharset = null;
    /** Message header to be used in logic bag evaluation and to be sent to requester. */
    public HeaderList responseHeaderList;
    /** URL where request were directed. */
    public URL url;
    /** URL which was originally in request. */
    public URL context;
    public Client client = null;
    public Cookie cookies[] = null;
    public Map<String, W3URLConnection> connectionPool = new HashMap<String, W3URLConnection>();
    public W3URLConnection uc = null;
    public W3HttpURLConnection tunnelUc = null;
    public W3Requester requester = null;
    public W3Lock filterLock = new W3Lock();
    public W3Lock receiveLock = new W3Lock();
    public W3Lock respondLock = new W3Lock();
    public W3Lock responderLock = new W3Lock();
    public W3Lock senderLock = new W3Lock();
    public PipedOutputStream filterOut = null;
    public String remoteAddr;
    public List<Store> requestHeaders = new ArrayList<Store>();
    public Vector chain = null;
    public Vector<Locale> locales = null;
    public Vector<String> parameterKeys = null;
    public Responder responder = null;
    public Response newResponse = null;
    public Response oldResponse = null;
    public Sender sender = null;
    public W3Server w3server = null;
    public W3Servlet w3servlet = null;
    public W3Session session = null;
    public W3Context w3context = null;
    public W3OutputStream firstOut = null;
    public W3OutputStream lastOut = null;
    public W3OutputStream w3out = null;
    public boolean committed = false;
    public boolean completed = false;
    public boolean filter = false;
    public boolean finished = false;
    public boolean jsp = false;
    public boolean local = false;
    public boolean mobile = false;
    public boolean mustClose = false;
    public boolean mustStop = false;
    public boolean named = false;
    public boolean unauthorized = false;
    public int byteCountReceived;
    public int byteCountSent;
    public int completionBufferSize;
    public int filterNumber;
    public int firstFilterNumber;
    public int lastFilterNumber;
    public int invokeNumber;
    public int port;
    public int requestNumber;
    public int state;
    public int status;
    public long responseLastModified;
    public long endTime;
    public long startTime;

    public class W3Exception extends IOException {
        static final long serialVersionUID = 0L;
        int status;

        public W3Exception(int status, String message) {
            super(status + " " + message);
            this.status = status;
        }

        public W3Exception(int status) {
            this(status, w3context.getComment(status));
        }

        public int getStatus() {
            return status;
        }

    }

    public class W3InputStream extends ServletInputStream {
        InputStream in;

        public W3InputStream(InputStream in)
            throws IOException {
            this.in = in;
        }

        public final int read()
            throws IOException {
            return in.read();
        }

        public final int read(byte b[])
            throws IOException {
            return in.read(b);
        }

        public final int read(byte b[], int off, int len)
            throws IOException {
            return in.read(b, off, len);
        }

        public final long skip(long n)
            throws IOException {
            return in.skip(n);
        }

        public final int available()
            throws IOException {
            return in.available();
        }

        public synchronized void mark(int readlimit) {
            in.mark(readlimit);
        }

        public synchronized void reset()
            throws IOException {
            in.reset();
        }

        public final boolean markSupported() {
            return in.markSupported();
        }

    }

    public class CompletionOutputStream extends FilterOutputStream {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        OutputStream out0;
        HeaderList responseHeaderList;
        Store store;
        boolean exceed = false;

        public CompletionOutputStream(OutputStream out) {
            super(out);
            out0 = out;
            this.out = bout;
            this.responseHeaderList = (HeaderList)W3Request.this.responseHeaderList.clone();
            this.store = W3Request.this.store;
        }

        public final void check()
            throws IOException {
            if (exceed || bout.size() < completionBufferSize) return;
            W3Request.this.responseHeaderList = this.responseHeaderList;
            sendResponseHeaderList(out0, store);
            bout.writeTo(this.out = out0);
            bout.reset();
            exceed = true;
            store.keep = false;
        }

        public final void write(int b)
            throws IOException {
            this.out.write(b);
            check();
        }

        public final void write(byte b[], int off, int len)
            throws IOException {
            this.out.write(b, off, len);
            check();
        }

        public void flush()
            throws IOException {
            if (exceed) this.out.flush();
        }

        public void close()
            throws IOException {
            this.out.flush();
            if (exceed) return;
            W3Request.this.responseHeaderList = this.responseHeaderList;
            if (status != 100 &&
                status != 101 &&
                status != 199 &&
                status != HttpServletResponse.SC_NO_CONTENT &&
                status != HttpServletResponse.SC_NOT_MODIFIED &&
                status != HttpServletResponse.SC_PRECONDITION_FAILED || bout.size() > 0)
                sender.setContentLength(bout.size());
            sendResponseHeaderList(out0, store);
            bout.writeTo(out0);
            bout.reset();
            out0.flush();
            exceed = true;
        }

    }

    public class W3OutputStream extends ServletOutputStream implements Runnable {
        ByteRange byteRange = null;
        InputStream in;
        Iterator<ByteRange> byteRangesIter = null;
        OutputStream out;
        W3Lock filterLock = null;
        PipedOutputStream filterOut = null;
        PrintWriter writer = null;
        Store store = null;
        Servlet servlet = null;
        String boundary = null, contentType = null;
        boolean committed = false, isMultiPart = false, mustClose = false, noMoreByteRanges = false, partStarted = false, useByteRanges = false;
        int bytePos = 0, byteCount = 0, contentLength = -1;

        public W3OutputStream(OutputStream out) {
            this.out = out;
            committed = W3Request.this.committed;
            store = W3Request.this.store;
        }

        public void run() {
            try {
                servlet.service(W3Request.this, sender);
            } catch (Exception ex) {
                try {
                    in.close();
                } catch (IOException ex1) {} finally {
                    w3server.handle(ex);
                }
            } catch (Error er) {
                w3server.handle(er);
                throw er;
            } finally {
                if (writer != null) writer.flush();
                try {
                    out.flush();
                } catch (IOException ex) {}
                if (filterLock != null) filterLock.release();
                else if (filterOut != null)
                    try {
                        filterOut.close();
                    } catch (IOException ex) {}
            }
        }

        /** Sends response to client taking care of servlet filters. */
        void check()
            throws IOException {
            if (committed) return;
            if (W3Request.this.committed) throw new IOException("missed");
            String range = getHeader("range"), ifRange = getHeader("if-range");
            if (status == HttpServletResponse.SC_OK) {
                // checking if request is conditional
                if (ifRange == null && !filter) {
                    Date d;
                    String s;
                    boolean modified;
                    HashMap<String,String> params = null;
                    long modifiedSince = ((modified = (s = getHeader("if-modified-since")) != null) || (s = getHeader("if-unmodified-since")) != null) && (d = Support.parse(Support.getParameters(s, params = new HashMap<String,String>()))) != null ? d.getTime() : 0L;
                    boolean result = false;
                    String unless = getHeader("unless");
                    if (unless != null && (result = evaluate(unless, responseHeaderList)) && modifiedSince == 0L && range == null)
                        throw new W3Exception(HttpServletResponse.SC_PRECONDITION_FAILED);
                    else if (!result) {
                        String eTag = getProperty("etag");
                        boolean eTagMatches = false;
                        List<String> ifMatchList = store.headerList.get((Object)"if-match");
                        if (!ifMatchList.isEmpty()) {
                            Iterator<String> ifMatchIter = ifMatchList.iterator();
                            while (ifMatchIter.hasNext()) {
                                StringTokenizer st = new StringTokenizer(ifMatchIter.next(), ",");
                                while (st.hasMoreTokens()) {
                                    String eTag1 = st.nextToken().trim();
                                    if (!eTag1.startsWith("W/") && eTag != null && eTag1.equals(eTag) || eTag1.equals("*")) {
                                        eTagMatches = true;
                                        break;
                                    }
                                }
                            }
                            if (!eTagMatches) throw new W3Exception(HttpServletResponse.SC_PRECONDITION_FAILED);
                            eTagMatches = false;
                        }
                        List<String> ifNoneMatchList = store.headerList.get((Object)"if-none-match");
                        if (!ifNoneMatchList.isEmpty()) {
                            boolean canBeWeak = range == null && store.method != null && store.method.equals("GET");
                            if (eTag != null && eTag.startsWith("W/"))
                                if (canBeWeak) eTag = eTag.substring(2);
                                else ifNoneMatchList = null;
                            if (ifNoneMatchList != null) {
                                Iterator<String> ifNoneMatchIter = ifNoneMatchList.iterator();
                                while (ifNoneMatchIter.hasNext()) {
                                    StringTokenizer st = new StringTokenizer(ifNoneMatchIter.next(), ",");
                                    while (st.hasMoreTokens()) {
                                        String eTag1 = st.nextToken().trim();
                                        if (eTag1.startsWith("W/"))
                                            if (canBeWeak) eTag1 = eTag1.substring(2);
                                            else continue;
                                        if (eTag != null && eTag1.equals(eTag) || eTag1.equals("*")) {
                                            eTagMatches = true;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        if (modifiedSince != 0L && (eTag == null || eTagMatches)) {
                            int length = -1;
                            if ((s = params.get("length")) != null)
                                try {
                                    length = Integer.parseInt(s);
                                } catch (NumberFormatException ex) {}
                            long lastModified = getResponseLastModified();
                            if (lastModified != -1L)
                                if (modified) {
                                    if (range == null && modifiedSince >= lastModified &&
                                        ((contentLength = getResponseContentLength()) == -1 || length == -1 || contentLength == length))
                                        throw new W3Exception(HttpServletResponse.SC_NOT_MODIFIED);
                                } else {
                                    if (modifiedSince < lastModified ||
                                        (contentLength = getResponseContentLength()) != -1 && length != -1 && contentLength != length)
                                        if (store.method != null && (store.method.equals("GET") || store.method.equals("HEAD"))) {
                                            if (range == null)
                                                throw new W3Exception(HttpServletResponse.SC_NOT_MODIFIED);
                                        } else throw new W3Exception(HttpServletResponse.SC_PRECONDITION_FAILED);
                                }
                        } else if (eTagMatches) throw new W3Exception(HttpServletResponse.SC_NOT_MODIFIED);
                    }
                }
            } else if (status == HttpServletResponse.SC_UNAUTHORIZED || status == HttpServletResponse.SC_PROXY_AUTHENTICATION_REQUIRED) {
                // In case unauthorized status was received locally or remotely,
                // generic authentication form is responded if authentication scheme is supported.
                // This form is used if client itself has no support for these authentication schemes,
                // and authentication procedure is performed by the server with credentials supplied by the client.
                if (!unauthorized &&
                    ((challenge = getProperty("www-authenticate")) != null ||
                     (challenge = getProperty("proxy-authenticate")) != null)) {
                    StringTokenizer st = new StringTokenizer(challenge);
                    if (st.hasMoreTokens() && w3server.authSchemes.containsKey(st.nextToken().trim().toLowerCase())) {
                        unauthorized = true;
                        throw new W3Exception(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                }
            } else if (mobile && (status == HttpServletResponse.SC_MOVED_PERMANENTLY || status == HttpServletResponse.SC_MOVED_TEMPORARILY)) throw new W3Exception(status);
            if (acceptTypes != null && chain == null && (contentType = getResponseContentType()) != null)
                try {
                    // checking what content type client accepts
                    HashMap<String,String> params = new HashMap<String,String>();
                    String ct = Support.getParameters(contentType, params),
                        characterEncoding = params.get("charset");
                    /*
                      if (characterEncoding == null && ct.startsWith("text/"))
                      sender.setContentType(contentType + "; charset=ISO-8859-1");
                    */
                    chain = (Vector)w3server.filters.get(ct);
                    Enumeration acceptTypesEnum = null;
                    ListValue acceptListValue = null, acceptedListValue = null;
                    acceptTypesEnum = acceptTypes.elements();
                    acceptListValue = (ListValue)acceptTypesEnum.nextElement();
                    for (;;) {
                        RegexpPool pool = new RegexpPool();
                        pool.add("*/*".equals(acceptListValue.name) ? "*" : acceptListValue.name, Boolean.TRUE);
                        if (pool.match(ct) != null) acceptedListValue = acceptListValue;
                        if (chain != null) {
                            filterNumber = firstFilterNumber = lastFilterNumber =
                                ((Step)chain.firstElement()).mimeName != null ? 0 : 1;
                            int chainSize = chain.size();
                            while (lastFilterNumber < chainSize) {
                                Step step = (Step)chain.elementAt(lastFilterNumber);
                                if (step.start) {
                                    if (acceptListValue == null || acceptedListValue != null) {
                                        chain = null;
                                        break;
                                    }
                                    filterNumber = firstFilterNumber = lastFilterNumber;
                                }
                                if (pool.match(step.mimeName) != null) {
                                    acceptedListValue = acceptListValue;
                                    break;
                                }
                                lastFilterNumber++;
                            }
                            if (lastFilterNumber < chainSize) break;
                        }
                        if (acceptTypesEnum != null && acceptTypesEnum.hasMoreElements()) {
                            acceptListValue = (ListValue)acceptTypesEnum.nextElement();
                            continue;
                        } else chain = null;
                        break;
                    }
                    if (acceptListValue != null && acceptedListValue == null)
                        if (!local && !mobile || status == HttpServletResponse.SC_NOT_ACCEPTABLE) chain = null;
                        else throw new W3Exception(HttpServletResponse.SC_NOT_ACCEPTABLE, ct + " not acceptable from " + accept);
                    else if (chain != null && ((Step)chain.firstElement()).mimeName == null &&
                             acceptedListValue.qualityValue < 1.0) filterNumber = 0;
                    // first element in filter chain is quality filter servlet
                    // client may have asked lower quality factor than normal
                } catch (RegexException ex) {
                    throw new IOException(ex);
                }
            Step step;
            if (chain != null && filterNumber <= lastFilterNumber &&
                (!(step = (Step)chain.elementAt(filterNumber)).start ||
                 filterNumber == firstFilterNumber)) {
                // calling servlet filter
                if (step.mimeName == null) filterNumber = firstFilterNumber;
                else filterNumber++;
                PipedOutputStream pout = new PipedOutputStream();
                W3Request.this.in = in = new BufferedInputStream(new PipedInputStream(pout));
                out = pout;
                if (w3out == null) {
                    W3Request.this.filterOut = pout;
                    try {
                        W3Request.this.filterLock.lock();
                    } catch (InterruptedException ex) {
                        store.keep = false;
                        mustStop = true;
                    }
                } else {
                    w3out.filterOut = pout;
                    w3out.filterLock = null;
                }
                w3out = this;
                filterLock = W3Request.this.filterLock;
                if (step.w3servlet.servlet == null)
                    try {
                        step.w3servlet.loadServlet();
                    } catch (ServletException ex) {
                        throw new IOException(ex);
                    }
                servlet = step.w3servlet.servlet;
                committed = true;
                if (w3server.verbose)
                    responseHeaderList.dump(System.out);
                store.method = "FILTER";
                store.headerList = responseHeaderList;
                responseHeaderList = new HeaderList();
                lastOut = null;
                lastWriter = null;
                if (w3server.logLevel > 1) w3server.log(requestNumber + ": Calling filter servlet " + servlet.getClass().getName());
                new Thread(w3server.filterGroup, this).start();
                return;
            }
            if (status == HttpServletResponse.SC_OK) {
                // checking if request is partial
                if (range != null && (contentLength = getResponseContentLength()) != -1) {
                    if (ifRange != null) {
                        if (ifRange.startsWith("W/")) range = null;
                        else if (ifRange.startsWith("\"")) {
                            String eTag = getProperty("etag");
                            if (eTag == null || eTag.startsWith("W/") || !eTag.equals(ifRange)) range = null;
                        } else if (store.headerList.containsKey("if-match") || store.headerList.containsKey("if-unmodified-since")) {
                            long lastModified = getResponseLastModified();
                            Date date = Support.parse(ifRange.trim());
                            if (date == null || date.getTime() != lastModified) range = null;
                        } else range = null;
                    }
                    if (range != null) {
                        TreeSet<ByteRange> byteRanges = new TreeSet<ByteRange>(byteRangeComparator);
                        boolean unsatisfiable = false;
                        StringTokenizer st = new StringTokenizer(range, "=");
                        String rangeType;
                        if (!(rangeType = st.nextToken().trim()).equalsIgnoreCase("bytes")) throw new W3Exception(HttpServletResponse.SC_BAD_REQUEST, "Bad range type " + rangeType);
                        st = new StringTokenizer(st.nextToken(), ",");
                        while (st.hasMoreTokens()) {
                            useByteRanges = false;
                            StringTokenizer st1 = new StringTokenizer(st.nextToken(), "-", true);
                            if (!st1.hasMoreTokens()) break;
                            String s = st1.nextToken().trim();
                            byteRange = new ByteRange();
                            try {
                                if (s.equals("-")) {
                                    if (!st1.hasMoreTokens()) break;
                                    // Suffix byte range specifier
                                    byteRange.firstBytePos = Math.max(contentLength - Integer.parseInt(st1.nextToken().trim()), 0);
                                    byteRange.lastBytePos = contentLength - 1;
                                    if (byteRange.firstBytePos == byteRange.lastBytePos) unsatisfiable = true;
                                } else {
                                    // Byte range specifier
                                    byteRange.firstBytePos = Integer.parseInt(s.trim());
                                    if (byteRange.firstBytePos > contentLength) unsatisfiable = true;
                                    byteRange.firstBytePos = Math.min(byteRange.firstBytePos, contentLength - 1);
                                    if (!st1.hasMoreTokens()) break;
                                    st1.nextToken();
                                    if (!st1.hasMoreTokens()) break;
                                    byteRange.lastBytePos = st1.hasMoreTokens() ? Math.min(Integer.parseInt(st1.nextToken().trim()), contentLength - 1) : contentLength - 1;
                                }
                            } catch (NumberFormatException ex) {
                                break;
                            }
                            if (byteRange.firstBytePos < 0 || byteRange.lastBytePos < 0 || byteRange.firstBytePos > byteRange.lastBytePos) break;
                            useByteRanges = true;
                            // Ignore redundant byte ranges
                            if (!byteRanges.isEmpty()) {
                                SortedSet<ByteRange> subSet = byteRanges.subSet(byteRange, maxByteRange);
                                try {
                                    ByteRange byteRange0 = subSet.first();
                                    if (byteRange0.lastBytePos >= byteRange.lastBytePos) continue;
                                    if ((byteRange.firstBytePos = Math.max(byteRange.firstBytePos, byteRange0.lastBytePos + 1)) >= contentLength) continue;
                                } catch (NoSuchElementException ex) {}
                            }
                            byteRanges.add(byteRange);
                        }
                        if (byteRanges.isEmpty() && unsatisfiable) throw new W3Exception(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                        if (useByteRanges) {
                            byteRangesIter = byteRanges.iterator();
                            byteRange = byteRangesIter.next();
                            sender.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                            if (byteRanges.size() == 1) {
                                sender.setHeader("Content-Range", "bytes " + byteRange.firstBytePos + "-" + byteRange.lastBytePos + "/" + contentLength);
                                sender.setContentLength(byteRange.lastBytePos - byteRange.firstBytePos + 1);
                            } else {
                                isMultiPart = true;
                                Random random = new Random(System.currentTimeMillis());
                                boundary = "multipart_byteranges_" + Long.toString(Math.abs(random.nextLong()), Character.MAX_RADIX);
                                sender.setContentType("multipart/byteranges; boundary=" + boundary);
                                unsetContentLength();
                            }
                        }
                    }
                }
            }
            committed = true;
            w3out = this;
            boolean isHead = store.method != null && store.method.equals("HEAD");
            if (store.method == null || !store.method.equals("CONNECT"))
                if (getProtocol().compareTo(W3URLConnection.protocol_1_1) >= 0) {
                    if (status != 100 &&
                        status != 101 &&
                        status != 199 &&
                        status != HttpServletResponse.SC_NO_CONTENT &&
                        status != HttpServletResponse.SC_NOT_MODIFIED &&
                        status != HttpServletResponse.SC_PRECONDITION_FAILED) {
                        if (getProperty("transfer-encoding") != null) throw new IOException("Transfer-Encoding not allowed");
                        if (w3server.contentEncoding != null &&
                            getProperty("content-encoding") == null)
                            if (w3server.contentEncoding.equals("gzip")) {
                                out = new GZIPOutputStream(out);
                                sender.setHeader("Content-Encoding", w3server.contentEncoding);
                                unsetContentLength();
                            } else if (w3server.contentEncoding.equals("deflate")) {
                                out = new DeflaterOutputStream(out, new Deflater(Deflater.DEFLATED));
                                sender.setHeader("Content-Encoding", w3server.contentEncoding);
                                unsetContentLength();
                            }
                        if (getResponseContentLength() == -1) {
                            String contentType = getResponseContentType();
                            if (contentType == null || !Support.getParameters(contentType, null).equalsIgnoreCase("multipart/byteranges")) {
                                if (w3server.logLevel > 1) w3server.log(requestNumber + ": Making chunked output stream");
                                out = new ChunkedOutputStream(out);
                                mustClose = true;
                                sender.setHeader("Transfer-Encoding", "chunked");
                            }
                        }
                    }
                } else if (!isHead && getResponseContentLength() == -1 && getProperty("transfer-encoding") == null) {
                    if (w3server.logLevel > 1) w3server.log(requestNumber + ": Making completion output stream");
                    out = new CompletionOutputStream(out);
                    mustClose = true;
                    return;
                }
            sendResponseHeaderList(W3Request.this.out, store);
            if (isHead) throw new W3Exception(HttpServletResponse.SC_OK);
        }

        boolean checkByteRanges()
            throws IOException {
            if (noMoreByteRanges) return true;
            if (bytePos > byteRange.lastBytePos) {
                if (!byteRangesIter.hasNext()) return noMoreByteRanges = true;
                byteRange = byteRangesIter.next();
                partStarted = false;
            }
            if (isMultiPart && !partStarted) {
                Support.writeBytes(out, "\r\n--" + boundary + "\r\n", null);
                if (contentType != null) Support.writeBytes(out, "Content-Type: " + contentType + "\r\n", null);
                Support.writeBytes(out, "Content-Range: bytes " + byteRange.firstBytePos + "-" + byteRange.lastBytePos + "/" + contentLength + "\r\n\r\n", null);
                partStarted = true;
            }
            return false;
        }

        public final void write(int b)
            throws IOException {
            check();
            if (useByteRanges && (checkByteRanges() || bytePos++ < byteRange.firstBytePos)) return;
            out.write(b);
            byteCount++;
        }

        public final void write(byte b[], int off, int len)
            throws IOException {
            check();
            if (useByteRanges) {
                if (checkByteRanges()) return;
                int n = byteRange.firstBytePos - bytePos;
                bytePos += len;
                if (n >= 0) {
                    off += n;
                    len -= n;
                }
                n = bytePos - byteRange.lastBytePos - 1;
                if (n > 0) len -= n;
                if (len <= 0) return;
            }
            out.write(b, off, len);
            byteCount += len;
        }

        public final void flush()
            throws IOException {
            check();
            out.flush();
        }

        public final void close()
            throws IOException {
            if (w3server.verbose) w3server.log("Closing output stream");
            flush();
            if (isMultiPart) {
                Support.writeBytes(out, "\r\n--" + boundary + "--", null);
                out.flush();
            }
            if (out instanceof DeflaterOutputStream) {
                ((DeflaterOutputStream)out).finish();
                ((DeflaterOutputStream)out).flush();
            }
            if (mustClose) out.close();
            W3Request.this.byteCountSent = byteCount;
        }

    }

    public class HeaderNames implements Enumeration {
        Iterator headerItems;
        HashSet<String> nameSet = new HashSet<String>();
        String name = null;

        HeaderNames(HeaderList header) {
            headerItems = header.iterator();
            if (headerItems.hasNext()) headerItems.next();
        }

        public boolean hasMoreElements() {
            if (name != null) return true;
            while (headerItems.hasNext()) {
                Header header = (Header)headerItems.next();
                String nameKey = header.getName().toLowerCase();
                if (nameSet.contains(nameKey)) continue;
                nameSet.add(nameKey);
                name = header.getName();
                return true;
            }
            return false;
        }

        public Object nextElement()
            throws NoSuchElementException {
            if (!hasMoreElements()) throw new NoSuchElementException();
            Object name1 = name;
            name = null;
            return name1;
        }

    }

    public class Headers implements Enumeration {
        Iterator headerItems;
        String name;
        StringTokenizer values = null;

        Headers(HeaderList header, String name) {
            headerItems = header.iterator();
            if (headerItems.hasNext()) headerItems.next();
            this.name = name;
        }

        Headers(HeaderList header) {
            this(header, null);
        }

        public boolean hasMoreElements() {
            if (values != null) return true;
            while (headerItems.hasNext()) {
                Header header = (Header)headerItems.next();
                if (header.getName().equalsIgnoreCase(name)) {
                    values = new StringTokenizer(header.getValue(), ",");
                    if (values.hasMoreTokens()) return true;
                }
            }
            return false;
        }

        public Object nextElement()
            throws NoSuchElementException {
            if (!hasMoreElements()) throw new NoSuchElementException();
            String value = values.nextToken().trim();
            if (!values.hasMoreTokens()) values = null;
            return value;
        }

    }

    public class Responder extends Thread {
        byte buffer[] = new byte[Support.bufferLength];

        public Responder() {
            super(W3Request.this.w3server.responderGroup, W3Request.this.getName());
        }

        public void run() {
            if (w3server.logLevel > 1) w3server.log("Responder " + getName() + " starting");
            boolean responseCleared = false;
            while (!isInterrupted())
                try {
                    for (;;) {
                        responseCleared = false;
                        Response response = null;
                        synchronized (this) {
                            if (newResponse == null) wait();
                            if (mustClose || mustStop || newResponse == null) break;
                            response = newResponse;
                        }
                        if (w3server.logLevel > 3) w3server.log("Responder " + getName() + " resuming");
                        boolean mustCloseUc = false;
                        W3URLConnection uc = response.uc;
                        try {
                            InputStream in = uc.getInputStream();
                            int responseCode = uc.getResponseCode();
                            HttpServletResponse res = response.res;
                            res.setStatus(responseCode);
                            Header viaHeaders[] = uc.getHeaderFieldList().getHeaders("via");
                            if (viaHeaders != null) viaHeaders[viaHeaders.length - 1].setValue(viaHeaders[viaHeaders.length - 1].getValue() + ", " + response.viaValue);
                            String key;
                            for (int i = 1; (key = uc.getHeaderFieldKey(i)) != null; i++)
                                if (key.length() > 0) res.addHeader(key, uc.getHeaderField(i));
                            if (viaHeaders == null) res.addHeader("Via", response.viaValue);
                            if (uc.cached()) res.addHeader("X-Cache", response.cache);
                            List<HeaderList> continueHeadersList = uc.getContinueHeaderFieldsList();
                            if (continueHeadersList != null)
                                setAttribute(W3Request.class.getName() + "/continueHeadersList", continueHeadersList);
                            OutputStream out = res.getOutputStream();
                            res.flushBuffer();
                            synchronized (this) {
                                newResponse = null;
                            }
                            responseCleared = true;
                            if (in != null && !response.method.equals("HEAD") &&
                                responseCode != 100 &&
                                responseCode != 101 &&
                                responseCode != 199 &&
                                responseCode != HttpServletResponse.SC_NO_CONTENT &&
                                responseCode != HttpServletResponse.SC_NOT_MODIFIED &&
                                responseCode != HttpServletResponse.SC_PRECONDITION_FAILED) {
                                // receiving data from remote server
                                for (int n; (n = in.read(buffer)) > 0;)
                                    out.write(buffer, 0, n);
                            }
                            out.close();
                        } catch (Exception ex) {
                            mustCloseUc = true;
                            if (ex instanceof InterruptedException) mustStop = true;
                            else handle(ex);
                            finish();
                        } finally {
                            synchronized (sender) {
                                requestHeaders.clear();
                                mustClose = true;
                                sender.notifyAll();
                            }
                            if (!responseCleared) {
                                synchronized (this) {
                                    newResponse = null;
                                }
                                responseCleared = true;
                            }
                            uc.closeStreams();
                            respondLock.release();
                            synchronized (uc.connectionPool) {
                                uc.uc.numberInUse--;
                                if (!uc.mayKeepAlive() || mustCloseUc) {
                                    uc.connectionPool.remove(uc.connectionName);
                                    uc.disconnect();
                                }
                            }
                            end(response.store);
                        }
                    }
                } catch (Exception ex) {
                    if (ex instanceof InterruptedException) mustStop = true;
                    else w3server.handle(ex);
                } catch (Error er) {
                    w3server.handle(er);
                    throw er;
                } finally {
                    responderLock.release();
                    if (!responseCleared)
                        synchronized (this) {
                            newResponse = null;
                        }
                    if (mustStop) break;
                }
            if (w3server.logLevel > 1) w3server.log("Responder " + getName() + " stopping");
        }

    }

    public class Sender extends Thread implements HttpServletResponse {
        OutputStream out = null;
        String characterEncoding = null;

        public Sender() {
            super(W3Request.this.w3server.senderGroup, W3Request.this.getName());
        }

        public void run() {
            if (w3server.logLevel > 1) w3server.log("Sender " + getName() + " starting");
            boolean hasNew = false;
            int timeout = 0;
            while (!isInterrupted())
                try {
                    boolean pipelined = false;
                    do {
                        for (;;) {
                            hasNew = false;
                            synchronized (this) {
                                if (requestHeaders.isEmpty()) wait(timeout);
                                if (requestHeaders.isEmpty()) {
                                    if (mustClose) break;
                                    continue;
                                }
                                timeout = w3server.keepAliveTimeout;
                                if (mustStop) {
                                    mustClose = true;
                                    store.keep = false;
                                    break;
                                }
                                store = requestHeaders.remove(0);
                                hasNew = true;
                            }
                            if (w3server.logLevel > 3) w3server.log("Sender " + getName() + " resuming");
                            if (store.response != null) {
                                store.response = null;
                                respondLock.lock();
                                continue;
                            }
                            break;
                        }
                        if (!hasNew) break;
                        tunnelUc = null;
                        requester = null;
                        pipelined = false;
                        ChunkedInputStream chunkedIn = null;
                        ConvertInputStream convertIn = null;
                        LimitInputStream limitIn = null;
                        try {
                            begin();
                            setAttribute(W3Request.class.getName() + "/uc", store.uc);
                            if (store.method == null) {
                                sendError(HttpServletResponse.SC_BAD_REQUEST, "No parameter " + store.request);
                                break;
                            }
                            in = plainIn;
                            String s;
                            if (store.receiving) {
                                if ((s = getHeader("transfer-encoding")) != null)
                                    if ((s = Support.getParameters(s, null)).equals("chunked")) {
                                        in = chunkedIn = new ChunkedInputStream(in);
                                        store.headerList.removeAll("transfer-encoding");
                                        store.headerList.removeAll("content-length");
                                    } else throw new W3Exception(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "Unknown transfer coding " + s);
                                int l = -1;
                                if (chunkedIn == null) {
                                    l = getContentLength();
                                    if (l != -1) in = limitIn = new LimitInputStream(in, l);
                                    else mustClose = true;
                                }
                                String contentType = W3Request.this.getContentType();
                                if (contentType != null) {
                                    Map<String,String> params = new HashMap<String,String>();
                                    if (Support.getParameters(contentType, params).equalsIgnoreCase("multipart/byteranges")) {
                                        String boundary = params.get("boundary");
                                        if (boundary != null) {
                                            boundary = "\r\n--" + boundary + "--\r\n";
                                            in = convertIn = new ConvertInputStream(in, boundary, boundary, null, -1, false, false, false, true);
                                            store.headerList.removeAll("content-length");
                                        }
                                    }
                                }
                                if (chunkedIn == null && convertIn == null && l == -1) {
                                    store.headerList.append(new Header("Content-Length", "0"));
                                    in = limitIn = new LimitInputStream(in, 0);
                                }
                            } else if ((s = getHeader("Expect")) != null && !s.equals("100-continue"))
                                throw new W3Exception(HttpServletResponse.SC_EXPECTATION_FAILED, "Expectation failed");
                            if (w3server.debug) w3server.currentRequests.add(W3Request.this.getName() + "-" + store.method + " " + store.uri);
                            if (store.method.equals("CONNECT")) {
                                // Server is acting here as a SSL-tunneling proxy
                                store.requestURI = store.uri;
                                url = new URL("https://" + store.uri);
                                if (!store.keep) checkLimits();
                                if (w3server.sslProxyDisabled) {
                                    sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "SSL Proxy not available");
                                    break;
                                }
                                if (checkDomain(url.toString())) break;
                                store.keep = false;
                                if (w3server.useSslProxy) {
                                    tunnelUc = new W3HttpURLConnection(new URL("http", w3server.sslProxyHost, w3server.sslProxyPort, "/"));
                                    tunnelUc.setRequestProperty("", getHeader(""));
                                    Iterator headerItems = store.headerList.iterator();
                                    while (headerItems.hasNext()) {
                                        Header header = (Header)headerItems.next();
                                        tunnelUc.addRequestProperty(header.getName(), header.getValue());
                                    }
                                    tunnelUc.setUseCaches(false);
                                    tunnelUc.getInputStream();
                                    responseHeaderList = (HeaderList)tunnelUc.getHeaderFields();
                                    complete();
                                    if (tunnelUc.getResponseCode() != 200) break;
                                    requester = tunnelUc.getRequester();
                                } else {
                                    StringTokenizer st = new StringTokenizer(store.uri, ":");
                                    requester = new W3Requester(st.nextToken(), Integer.parseInt(st.nextToken()));
                                    complete();
                                }
                                InputStream requesterIn = requester.socket.getInputStream();
                                receiveLock.release();
                                for (int n; requester.isOpen() && (n = requesterIn.read(buffer)) > 0;) {
                                    out.write(buffer, 0, n);
                                    out.flush();
                                    byteCountReceived += n;
                                }
                            } else pipelined = handle(store.uri);
                        } catch (Exception ex) {
                            store.keep = false;
                            mustClose = true;
                            if (ex instanceof InterruptedException) mustStop = true;
                            else if (store != null) handle(ex);
                        } finally {
                            try {
                                if (store != null) {
                                    if (store.receiving) {
                                        if (convertIn != null) {
                                            if (convertIn.isEnded()) {
                                                if (chunkedIn != null)
                                                    for (int n; (n = chunkedIn.read(buffer)) > 0;);
                                                else if (limitIn != null)
                                                    for (int n; (n = limitIn.read(buffer)) > 0;);
                                                else for (int n; (n = plainIn.read(buffer)) > 0;);
                                            } else mustClose = true;
                                        } else if (chunkedIn != null && chunkedIn.hasLeft() || limitIn != null && limitIn.getRemaining() > 0) mustClose = true;
                                        receiveLock.release();
                                    }
                                    if (!pipelined) end(store);
                                }
                            } finally {
                                if (tunnelUc != null) tunnelUc.disconnect();
                                else if (requester != null) requester.close();
                            }
                        }
                    } while (!mustClose && store.keep && (w3server.keepAliveCount == -1 || w3server.keepAliveCount > requestNumber));
                } catch (Exception ex) {
                    if (ex instanceof InterruptedException) mustStop = true;
                    else w3server.handle(ex);
                } catch (Error er) {
                    w3server.handle(er);
                    throw er;
                } finally {
                    mustClose = true;
                    if (!ProxyServlet.disableProxyPipelining) {
                        synchronized (responder) {
                            mustClose = true;
                            responder.notifyAll();
                        }
                        try {
                            responderLock.lock();
                        } catch (InterruptedException ex) {
                            mustStop = true;
                        }
                    }
                    finish();
                    receiveLock.release();
                    senderLock.release();
                    if (mustStop) break;
                    timeout = 0;
                }
            if (w3server.logLevel > 1) w3server.log("Sender " + getName() + " stopping");
        }

        /** Returns the name of the character encoding (MIME charset) used for
            the body sent in this response. The character encoding may have been
            specified explicitly using the setCharacterEncoding(java.lang.String) or
            setContentType(java.lang.String) methods, or implicitly using the
            setLocale(java.util.Locale) method. Explicit specifications take
            precedence over implicit specifications. Calls made to these methods
            after getWriter has been called or after the response has been committed
            have no effect on the character encoding. If no character encoding has
            been specified, ISO-8859-1 is returned.  See RFC 2047
            (http://www.ietf.org/rfc/rfc2047.txt) for more information about
            character encoding and MIME. 

            @return a String specifying the name of the character encoding, for
            example, UTF-8

        */
        public String getCharacterEncoding() {
            String contentType = getResponseContentType(),
                defaultCharacterEncoding = "ISO-8859-1";
            if (contentType == null) return defaultCharacterEncoding;
            HashMap<String,String> params = new HashMap<String,String>();
            Support.getParameters(contentType, params);
            String characterEncoding = params.get("charset");
            return characterEncoding != null ? characterEncoding : defaultCharacterEncoding;
        }

        /** Returns the content type used for the MIME body sent in this
            response.  The content type proper must have been specified using
            setContentType(java.lang.String) before the response is committed. If no
            content type has been specified, this method returns null. If a content
            type has been specified and a character encoding has been explicitly or
            implicitly specified as described in getCharacterEncoding(), the charset
            parameter is included in the string returned. If no character encoding
            has been specified, the charset parameter is omitted. 

            @return a String specifying the content type, for example, text/html;
            charset=UTF-8, or null

        */
        public String getContentType() {
            return getProperty("content-type");
        }

        /** Forces any content in the buffer to be written to the client. A call to this
            method automatically commits the response, meaning the status code and
            headers will be written.

            @throws IOException

            @see setBufferSize(int), getBufferSize(), isCommitted(), reset()

        */
        public void flushBuffer()
            throws IOException {
            if (lastOut == null && lastWriter == null) getOutputStream();
            if (lastWriter != null) lastWriter.flush();
            else lastOut.flush();
        }

        /** Returns the actual buffer size used for the response. If no buffering is used,
            this method returns 0.

            @return the actual buffer size used

            @see setBufferSize(int), flushBuffer(), isCommitted(), reset()

        */
        public int getBufferSize() {
            return completionBufferSize;
        }

        /** Returns a ServletOutputStream suitable for writing binary data in the
            response. The servlet container does not encode the binary data.
            Calling flush() on the ServletOutputStream commits the response. Either this
            method or getWriter() may be called to write the body, not both.

            @return a ServletOutputStream for writing binary data

            @throws IllegalStateException if the getWriter method has been called on this
            response

            @throws IOException if an input or output exception occurred

            @see getWriter()

        */
        public ServletOutputStream getOutputStream()
            throws IOException {
            if (lastOut != null) return lastOut;
            String s;
            if (getProtocol().compareTo(W3URLConnection.protocol_1_1) >= 0
                && (s = getHeader("Expect")) != null && s.equals("100-continue")) {
                List<HeaderList> continueHeadersList = (List<HeaderList>)getAttribute(W3Request.class.getName() + "/continueHeadersList");
                if (continueHeadersList != null) {
                    Iterator<HeaderList> continueHeadersItems = continueHeadersList.iterator();
                    while (continueHeadersItems.hasNext())
                        Support.sendMessage(out, continueHeadersItems.next());
                }
            }
            W3OutputStream w3out = new W3OutputStream(w3server.type ? new CloneOutputStream(new OutputStream[] {out, System.out}) : out);
            if (firstOut == null) firstOut = w3out;
            lastOut = w3out;
            return lastOut;
        }

        /** Returns a PrintWriter object that can send character text to the client. The
            character encoding used is the one specified in the charset-property of the
            setContentType(String) method, which must be called before calling this
            method for the charset to take effect.
            If necessary, the MIME type of the response is modified to reflect the character
            encoding used.
            Calling flush() on the PrintWriter commits the response.
            Either this method or getOutputStream() may be called to write the body,
            not both.

            @return a PrintWriter object that can return character data to the client

            @throws UnsupportedEncodingException if the charset specified in
            setContentType cannot be used

            @throws IllegalStateException if the getOutputStream method has already been
            called for this response object

            @throws IOException if an input or output exception occurred

            @see getOutputStream(), setContentType(String)

        */
        public PrintWriter getWriter()
            throws IOException, UnsupportedEncodingException {
            W3OutputStream w3out = null;
            if (lastWriter != null) return lastWriter;
            else if (lastOut != null) w3out = lastOut;
            Writer writer = null;
            if (w3out == null) w3out = new W3OutputStream(w3server.type ? new CloneOutputStream(new OutputStream[] {out, System.out}) : out);
            String contentType = getResponseContentType();
            if (contentType != null) {
                HashMap<String,String> params = new HashMap<String,String>();
                String ct = Support.getParameters(contentType, params), charset = params.get("charset");
                if (acceptCharsets != null) {
                    boolean hasDefaultCharset = charset == null || charset.equalsIgnoreCase("ISO-8859-1"),
                        foundEncoding = false;
                    for (Iterator iter = acceptCharsets.iterator(); iter.hasNext(); foundEncoding = false) {
                        foundEncoding = true;
                        String name = ((ListValue)iter.next()).name;
                        if (name.equals("*") || charset != null && charset.equalsIgnoreCase(name)) break;
                        String characterEncoding = Support.getCharacterEncoding(name);
                        if (hasDefaultCharset && characterEncoding.equals("8859_1")) break;
                    }
                    if (!foundEncoding) {
                        for (Iterator iter = acceptCharsets.iterator(); iter.hasNext();) {
                            String name = ((ListValue)iter.next()).name,
                                characterEncoding = Support.getCharacterEncoding(name);
                            try {
                                writer = new OutputStreamWriter(w3out, characterEncoding);
                            } catch (UnsupportedEncodingException ex) {
                                continue;
                            }
                            setContentType(ct + "; charset=" + name);
                            if (!hasDefaultCharset) {
                                writer = new OutputStreamWriter(new EncodingConversionOutputStream(writer, Support.getCharacterEncoding(charset)), "8859_1");
                                if (w3server.verbose) w3server.log("Made stream writer converting from encoding " + charset);
                            } else if (w3server.verbose) w3server.log("Made stream writer with encoding " + name);
                            break;
                        }
                    }
                }
                if (charset != null && writer == null) {
                    String characterEncoding = Support.getCharacterEncoding(charset);
                    try {
                        writer = new OutputStreamWriter(w3out, characterEncoding);
                    } catch (UnsupportedEncodingException ex) {}
                }
            }
            if (writer == null) writer = new OutputStreamWriter(w3out, "8859_1");
            w3out.writer = new PrintWriter(new BufferedWriter(writer));
            if (W3Request.this.w3out != null) W3Request.this.w3out.writer = w3out.writer;
            else firstWriter = w3out.writer;
            lastOut = w3out;
            lastWriter = w3out.writer;
            return lastWriter;
        }

        /** Returns a boolean indicating if the response has been committed. A commited
            response has already had its status code and headers written.

            @return a boolean indicating if the response has been committed

            @see setBufferSize(int), getBufferSize(), flushBuffer(), reset()

        */
        public boolean isCommitted() {
            return committed;
        }

        /** Clears any data that exists in the buffer as well as the status code and headers.
            If the response has been committed, this method throws an IllegalStateException.

            @throws IllegalStateException if the response has already been committed

            @see setBufferSize(int), getBufferSize(), flushBuffer(), isCommitted()

        */
        public void reset() {
            resetBuffer();
            responseHeaderList = new HeaderList();
            responseLastModified = -1L;
            characterEncoding = null;
            locale = null;
            status = -1;
        }

        /** Clears the content of the underlying buffer in the response without clearing
            headers or status code. If the response has been committed, this method
            throws an IllegalStateException.
            Since: 2.3

            @see setBufferSize(int), getBufferSize(), isCommitted(), reset()

        */
        public void resetBuffer() {
            if (isCommitted()) throw new IllegalStateException();
            if (lastOut != null && lastOut.out instanceof CompletionOutputStream)
                ((CompletionOutputStream)lastOut.out).bout.reset();
        }

        /** Sets the preferred buffer size for the body of the response. The servlet container
            will use a buffer at least as large as the size requested. The actual buffer
            size used can be found using getBufferSize.
            A larger buffer allows more content to be written before anything is actually
            sent, thus providing the servlet with more time to set appropriate status codes
            and headers. A smaller buffer decreases server memory load and allows the
            client to start receiving data more quickly.
            This method must be called before any response body content is written; if
            content has been written, this method throws an IllegalStateException.

            @param size the preferred buffer size
            @throws IllegalStateException if this method is called after content has been
            written
            @see getBufferSize(), flushBuffer(), isCommitted(), reset()

        */
        public void setBufferSize(int size) {
            if (isCommitted() || lastOut != null && lastOut.out instanceof CompletionOutputStream && ((CompletionOutputStream)lastOut.out).bout.size() > 0) throw new IllegalStateException();
            completionBufferSize = size;
        }

        /** Sets the character encoding (MIME charset) of the response being
            sent to the client, for example, to UTF-8. If the character encoding has
            already been set by setContentType(java.lang.String) or
            setLocale(java.util.Locale), this method overrides it. Calling
            setContentType(java.lang.String) with the String of text/html and
            calling this method with the String of UTF-8 is equivalent with calling
            setContentType with the String of text/html; charset=UTF-8.  This method
            has no effect if it is called after getWriter has been called or after
            the response has been committed. 

            @param characterEncoding - a String specifying only the character set
            defined by IANA Character Sets
            (http://www.iana.org/assignments/character-sets)

            @see setLocale

        */
        public void setCharacterEncoding(String characterEncoding) {
            this.characterEncoding = characterEncoding;
            String contentType = getResponseContentType();
            if (contentType == null) return;
            String ct = Support.getParameters(contentType);
            setContentType(ct + "; charset=" + characterEncoding);
        }

        /** Sets the length of the content body in the response In HTTP servlets, this
            method sets the HTTP Content-Length header.

            @param len an integer specifying the length of the content being returned to the
            client; sets the Content-Length header

        */
        public void setContentLength(int len) {
            setIntHeader("Content-Length", len);
        }

        /** Sets the content type of the response being sent to the client. The content type
            may include the type of character encoding used, for example,
            text/html; charset=ISO-8859-4.
            If obtaining a PrintWriter, this method should be called first.

            @param type a String specifying the MIME type of the content
            @see getOutputStream(), getWriter()

        */
        public void setContentType(String ct) {
            setHeader("Content-Type", ct);
        }

        /** Sets the locale of the response, setting the headers (including the
            Content-Type's charset) as appropriate. This method should be called
            before a call to getWriter(). By default, the response locale is the
            default locale for the server. 

            @param loc the locale of the response
            @see getLocale()

        */
        public void setLocale(Locale locale) {
            W3Request.this.locale = locale;
            setHeader("Content-Language", locale.getLanguage() + "-" + locale.getCountry());
        }

        /** Returns the locale specified for this response using the
            setLocale(java.util.Locale) method. Calls made to setLocale after the
            response is committed have no effect. If no locale has been specified,
            the container's default locale is returned. 

            @return the locale assigned to the response.

            @see setLocale(java.util.Locale)

        */
        public Locale getLocale() {
            return locale;
        }

        /** Adds the specified cookie to the response. This method can be called multiple
            times to set more than one cookie.

            @param cookie the Cookie to return to the client

        */
        public void addCookie(Cookie cookie) {
            responseHeaderList.append(new Header("Set-Cookie", W3URLConnection.cookieSetHeader(cookie)));
        }

        /** Adds a response header with the given name and date value. The date is specified
            in terms of milliseconds since the epoch. This method allows response
            headers to have multiple values.

            @param name the name of the header to set
            @param value the additional date value
            @see setDateHeader(String, long)

        */
        public void addDateHeader(String name, long date) {
            addHeader(name, Support.format(new Date(date)));
        }

        /** Adds a response header with the given name and value. This method allows
            response headers to have multiple values.

            @param name the name of the header
            @param value the additional header value
            @see setHeader(String, String)

        */
        public void addHeader(String name, String value) {
            responseHeaderList.append(new Header(name, value));
        }

        /** Adds a response header with the given name and integer value. This method
            allows response headers to have multiple values.

            @param name the name of the header
            @value the assigned integer value
            @see: setIntHeader(String, int)

        */
        public void addIntHeader(String name, int value) {
            addHeader(name, String.valueOf(value));
        }

        /** Returns a boolean indicating whether the named response header has already
            been set.

            @param name the header name
            @return true if the named response header has already been set; false
            otherwise

        */
        public boolean containsHeader(String name) {
            return getProperty(name) != null;
        }

        /** @deprecated Deprecated as of version 2.1, use encodeRedirectURL(String url) instead
            @param url the url to be encoded.
            @return the encoded URL if encoding is needed; the unchanged URL
            otherwise.

        */
        @Deprecated
        public String encodeRedirectUrl(String url) {
            return encodeRedirectURL(url);
        }

        /** Encodes the specified URL for use in the sendRedirect method or, if encoding
            is not needed, returns the URL unchanged. The implementation of this
            method includes the logic to determine whether the session ID needs to be
            encoded in the URL. Because the rules for making this determination can differ
            from those used to decide whether to encode a normal link, this method is
            seperate from the encodeURL method.
            All URLs sent to the HttpServletResponse.sendRedirect method should
            be run through this method. Otherwise, URL rewriting cannot be used with
            browsers which do not support cookies.

            @param url the url to be encoded.
            @return the encoded URL if encoding is needed; the unchanged URL
            otherwise.
            @see sendRedirect(String), encodeUrl(String)

        */
        public String encodeRedirectURL(String url) {
            return url;
        }

        /** @deprecated Deprecated as of version 2.1, use encodeURL(String url) instead
            @param url - the url to be encoded.
            @return the encoded URL if encoding is needed; the unchanged URL
            otherwise.

        */
        @Deprecated
        public String encodeUrl(String url) {
            return encodeURL(url);
        }

        /** Encodes the specified URL by including the session ID in it, or, if encoding is
            not needed, returns the URL unchanged. The implementation of this method
            includes the logic to determine whether the session ID needs to be encoded in
            the URL. For example, if the browser supports cookies, or session tracking is
            turned off, URL encoding is unnecessary.
            For robust session tracking, all URLs emitted by a servlet should be run
            through this method. Otherwise, URL rewriting cannot be used with brows-ers
            which do not support cookies.

            @param url the url to be encoded.
            @return the encoded URL if encoding is needed; the unchanged URL
            otherwise.

        */
        public String encodeURL(String url) {
            return url;
        }

        /** Sends an error response to the client using the specified status code and clearing
            the buffer.
            If the response has already been committed, this method throws an IllegalStateException.
            After using this method, the response should be considered
            to be committed and should not be written to.

            @param sc the error status code
            @throws IOException If an input or output exception occurs
            @throws IllegalStateException If the response was committed before this method
            call

        */
        public void sendError(int sc)
            throws IOException {
            sendError(sc, null);
        }

        /** Sends an error response to the client using the specified status clearing the
            buffer. The server defaults to creating the response to look like an HTML-formatted
            server error page containing the specified message, setting the content
            type to "text/html", leaving cookies and other headers unmodified. If an
            error-page declaration has been made for the web application corresponding
            to the status code passed in, it will be served back in preference to the suggested
            msg parameter.
            If the response has already been committed, this method throws an IllegalStateException.
            After using this method, the response should be considered
            to be committed and should not be written to.

            @param sc the error status code
            @param msg the descriptive message
            @throws IOException If an input or output exception occurs
            @throws IllegalStateException If the response was committed

        */
        public void sendError(int sc, String msg)
            throws IOException {
            w3context.sendError(w3servlet, W3Request.this, this, sc, msg);
        }

        /** Sends a temporary redirect response to the client using the specified redirect
            location URL. This method can accept relative URLs; the servlet container
            must convert the relative URL to an absolute URL before sending the
            response to the client. If the location is relative without a leading "/" the container
            interprets it as relative to the current request URI. If the location is relative
            with a leading "/" the container interprets it as relative to the servlet
            container root.
            If the response has already been committed, this method throws an IllegalStateException.
            After using this method, the response should be considered
            to be committed and should not be written to.

            @param location the redirect location URL
            @throws IOException If an input or output exception occurs
            @throws IllegalStateException If the response was committed

        */
        public void sendRedirect(String location)
            throws IOException {
            w3context.sendRedirect(W3Request.this, this, location);
        }

        /** Sets a response header with the given name and date-value. The date is specified
            in terms of milliseconds since the epoch. If the header had already been
            set, the new value overwrites the previous one. The containsHeader method
            can be used to test for the presence of a header before setting its value.

            @param name the name of the header to set
            @param value the assigned date value
            @see containsHeader(String), addDateHeader(String, long)

        */
        public void setDateHeader(String name, long date) {
            setHeader(name, Support.format(new Date(date)));
        }

        /** Sets a response header with the given name and value. If the header had
            already been set, the new value overwrites the previous one. The containsHeader
            method can be used to test for the presence of a header before setting its value.

            @param name the name of the header
            @param value the header value
            @see containsHeader(String), addHeader(String, String)

        */
        public void setHeader(String name, String value) {
            responseHeaderList.replace(new Header(name, value));
        }

        /** Sets a response header with the given name and integer value. If the header
            had already been set, the new value overwrites the previous one. The
            containsHeader method can be used to test for the presence of a header
            before setting its value.

            @param name the name of the header
            @param value the assigned integer value
            @see containsHeader(String), addIntHeader(String, int)

        */
        public void setIntHeader(String name, int value) {
            setHeader(name, String.valueOf(value));
        }

        /** Sets the status code for this response. This method is used to set the return
            status code when there is no error (for example, for the status codes SC_OK
            or SC_MOVED_TEMPORARILY). If there is an error, and the caller wishes
            to invoke an defined in the web applicaion, the sendError method should be
            used instead.
            The container clears the buffer and sets the Location header, preserving cook-ies
            and other headers.

            @param sc the status code
            @see sendError(int, String)

        */
        public void setStatus(int sc) {
            setStatus(sc, null);
        }

        /** @deprecated Deprecated as of version 2.1, due to ambiguous meaning of the message
            parameter. To set a status code use setStatus(int), to send an error with a
            description use sendError(int, String). Sets the status code and message
            for this response.

            @param sc the status code
            @param sm the status message

        */
        @Deprecated
        public void setStatus(int sc, String sm) {
            if (sm == null) {
                sm = w3context.getComment(sc);
            }
            status = sc;
            Header header = responseHeaderList.getFirst();
            if (header != null && header.getName().equals("")) responseHeaderList.remove(0);
            responseHeaderList.prepend(new Header("", protocol + " " + status + (sm != null ? " " + sm : "")));
        }

        /**
         * Gets the current status code of this response.
         * @return the current status code of this response
         * @since Servlet 3.0
         */
        public int getStatus() {
            return status;
        }

        /**
         * Gets the value of the response header with the given name.
         *
         * If a response header with the given name exists and contains multiple values, the value that was added first will be returned.
         *
         * This method considers only response headers set or added via {@link #setHeader(java.lang.String, java.lang.String)}, {@link #addHeader(java.lang.String, java.lang.String)}, {@link #setDateHeader(java.lang.String, long)}, {@link #addDateHeader(java.lang.String, long)}, {@link #setIntHeader(java.lang.String, int)}, or {@link #addIntHeader(java.lang.String, int)}, respectively. 
         * @param name the name of the response header whose value to return
         * @return the value of the response header with the given name, or null if no header with the given name has been set on this response
         * @since Servlet 3.0
         */
        public String getHeader(String name) {
            return responseHeaderList.getHeaderValue(name);
        }

        /**
         * Gets the values of the response header with the given name.
         *
         * This method considers only response headers set or added via {@link #setHeader(java.lang.String, java.lang.String)}, {@link #addHeader(java.lang.String, java.lang.String)}, {@link #setDateHeader(java.lang.String, long)}, {@link #addDateHeader(java.lang.String, long)}, {@link #setIntHeader(java.lang.String, int)}, or {@link #addIntHeader(java.lang.String, int)}, respectively.
         * 
         * Any changes to the returned Collection must not affect this HttpServletResponse.
         * @param name the name of the response header whose values to return
         * @return a (possibly empty) Collection of the values of the response header with the given name
         * @since Servlet 3.0
         */
        public Collection<String> getHeaders(String name) {
            Header headers[] = responseHeaderList.getHeaders(name);
            List<String> headerList = new ArrayList<String>();
            for (Header header : headers)
                headerList.add(header.getValue());
            return headerList;
        }

        /**
         * Gets the names of the headers of this response.
         *
         * This method considers only response headers set or added via {@link #setHeader(java.lang.String, java.lang.String)}, {@link #addHeader(java.lang.String, java.lang.String)}, {@link #setDateHeader(java.lang.String, long)}, {@link #addDateHeader(java.lang.String, long)}, {@link #setIntHeader(java.lang.String, int)}, or {@link #addIntHeader(java.lang.String, int)}, respectively.
         *
         * Any changes to the returned Collection must not affect this HttpServletResponse. 
         * @return a (possibly empty) Collection of the names of the headers of this response
         * @since Servlet 3.0
         */
        public Collection<String> getHeaderNames() {
            List<Header> headerList = responseHeaderList.getList();
            List<String> headerNames = new ArrayList<String>();
            for (Header header : headerList)
                headerNames.add(header.getName());
            return headerNames;
        }

    }

    /** Evaluates logic bag of unless-header with given message header. */
    public static boolean evaluate(String logicBag, HeaderList headerList) {
        int i = 0;
        while (i < logicBag.length() && Support.whites.indexOf(logicBag.charAt(i)) != -1) i++;
        if (logicBag.charAt(i++) != '{') return false;
        while (i < logicBag.length() && Support.whites.indexOf(logicBag.charAt(i)) != -1) i++;
        int j = i;
        while (i < logicBag.length() && Support.whites.indexOf(logicBag.charAt(i)) == -1) i++;
        String op = logicBag.substring(j, i).toLowerCase();
        Vector<Object> v = new Vector<Object>();
        if (op.equals("def")) {
            for (;;) {
                while (i < logicBag.length() && Support.whites.indexOf(logicBag.charAt(i)) != -1 &&
                       logicBag.charAt(i) != '}') i++;
                if (i == logicBag.length() || logicBag.charAt(i) == '}') break;
                j = i;
                while (i < logicBag.length() && Support.whites.indexOf(logicBag.charAt(i)) == -1 &&
                       logicBag.charAt(i) != '}') i++;
                if (i > j) v.addElement(logicBag.substring(j, i));
                if (i == logicBag.length() || logicBag.charAt(i) == '}') break;
            } while (i < logicBag.length() && Support.whites.indexOf(logicBag.charAt(i)) != -1) i++;
            if (i < logicBag.length() && logicBag.charAt(i) == ',' && evaluate(logicBag.substring(i + 1), headerList)) return true;
            for (i = 0; i < v.size(); i++)
                if (!headerList.hasHeader((String)v.elementAt(i))) return false;
            return true;
        }
        boolean quoted = false;
        int h = 0, k = 0;
        for (;;) {
            while (i < logicBag.length() && (("{}\"".indexOf(logicBag.charAt(i)) == -1 ||
                                              logicBag.charAt(i) != '"' && quoted || i > 0 && logicBag.charAt(i - 1) == '\\'))) i++;
            if (i == logicBag.length()) break;
            switch (logicBag.charAt(i)) {
            case '"':
                quoted = !quoted;
                break;
            case '{':
                if (++k == 1) h = i;
                break;
            case '}':
                if (--k == 0) v.addElement(logicBag.substring(h, i + 1));
                break;
            }
            i++;
            if (k == -1) break;
        } while (i < logicBag.length() && Support.whites.indexOf(logicBag.charAt(i)) != -1) i++;
        if (i < logicBag.length() && logicBag.charAt(i) == ',' && evaluate(logicBag.substring(i + 1), headerList)) return true;
        if (op.equals("and")) {
            for (i = 0; i < v.size(); i++)
                if (!evaluate((String)v.elementAt(i), headerList)) return false;
            return true;
        }
        if (op.equals("or")) {
            for (i = 0; i < v.size(); i++)
                if (evaluate((String)v.elementAt(i), headerList)) return true;
            return false;
        }
        if (op.equals("xor")) {
            boolean g = false;
            for (i = 0; i < v.size(); i++)
                if (g && (g = evaluate((String)v.elementAt(i), headerList))) return false;
            return true;
        }
        if (op.equals("not")) {
            for (i = 0; i < v.size(); i++)
                if (evaluate((String)v.elementAt(i), headerList)) return false;
            return true;
        }
        Object a[];
        String s, t, u;
        Vector<Object> r = new Vector<Object>();
        for (i = 0; i < v.size(); i++) {
            s = (String)v.elementAt(i);
            j = 1;
            while (j < s.length() && Support.whites.indexOf(s.charAt(j)) != -1) j++;
            h = j;
            while (j < s.length() && Support.whites.indexOf(s.charAt(j)) == -1) j++;
            if ((u = headerList.getHeaderValue(t = s.substring(h, j).toLowerCase())) != null) {
                while (j < s.length() && Support.whites.indexOf(s.charAt(j)) != -1) j++;
                if (s.charAt(j) == '"') {
                    h = ++j;
                    while (j < s.length() && (s.charAt(j) != '"' || s.charAt(j - 1) == '\\')) j++;
                } else {
                    h = j;
                    while (j < s.length() && s.charAt(j) != '}' &&
                           Support.whites.indexOf(s.charAt(j)) == -1) j++;
                }
                s = s.substring(h, j);
                if (t.equals("date") || t.equals("expires") ||
                    t.equals("if-modified-since") || t.equals("last-modified")) {
                    Date dv = Support.parse(Support.getParameters(u, null)),
                        dr = Support.parse(Support.getParameters(s, null));
                    if (dv != null && dr != null) {
                        v.setElementAt(new Long(dv.getTime()), i);
                        r.addElement(new Long(dr.getTime()));
                    } else {
                        v.setElementAt(null, i);
                        r.addElement(null);
                    }
                } else {
                    v.setElementAt(u, i);
                    r.addElement(s);
                }
            } else {
                v.setElementAt(null, i);
                r.addElement(null);
            }
        }
        if (op.equals("eq")) {
            for (i = 0; i < v.size(); i++)
                if (v.elementAt(i) == null || !v.elementAt(i).equals(r.elementAt(i)))
                    return false;
            return true;
        }
        if (op.equals("ne")) {
            for (i = 0; i < v.size(); i++)
                if (v.elementAt(i) != null && v.elementAt(i).equals(r.elementAt(i)))
                    return false;
            return true;
        }
        if (op.equals("lt")) {
            for (i = 0; i < v.size(); i++)
                if (v.elementAt(i) == null || v.elementAt(i) instanceof String &&
                    ((String)v.elementAt(i)).compareTo((String)r.elementAt(i)) >= 0 ||
                    ((Long)v.elementAt(i)).longValue() >= ((Long)r.elementAt(i)).longValue())
                    return false;
            return true;
        }
        if (op.equals("le")) {
            for (i = 0; i < v.size(); i++)
                if (v.elementAt(i) == null || v.elementAt(i) instanceof String &&
                    ((String)v.elementAt(i)).compareTo((String)r.elementAt(i)) > 0 ||
                    ((Long)v.elementAt(i)).longValue() > ((Long)r.elementAt(i)).longValue())
                    return false;
            return true;
        }
        if (op.equals("ge")) {
            for (i = 0; i < v.size(); i++)
                if (v.elementAt(i) == null || v.elementAt(i) instanceof String &&
                    ((String)v.elementAt(i)).compareTo((String)r.elementAt(i)) < 0 ||
                    ((Long)v.elementAt(i)).longValue() < ((Long)r.elementAt(i)).longValue())
                    return false;
            return true;
        }
        if (op.equals("gt")) {
            for (i = 0; i < v.size(); i++)
                if (v.elementAt(i) == null || v.elementAt(i) instanceof String &&
                    ((String)v.elementAt(i)).compareTo((String)r.elementAt(i)) <= 0 ||
                    ((Long)v.elementAt(i)).longValue() <= ((Long)r.elementAt(i)).longValue())
                    return false;
            return true;
        }
        if (op.equals("in")) {
            for (i = 0; i < v.size(); i++)
                if (v.elementAt(i) == null || !(v.elementAt(i) instanceof String) ||
                    ((String)v.elementAt(i)).indexOf((String)r.elementAt(i)) == -1) return false;
            return true;
        }
        return false;
    }

    public static void setParameters(Map<String, String[]> parameters, Map<String, Object> parameterValues) {
        Iterator<Map.Entry<String,Object>> parameterValueIter = parameterValues.entrySet().iterator();
        while (parameterValueIter.hasNext()) {
            Map.Entry<String,Object> entry = parameterValueIter.next();
            if (entry.getValue() instanceof Vector) {
                String values[] = new String[((Vector)entry.getValue()).size()];
                for (int i = 0; i < values.length; i++) values[i] = (String)((Vector)entry.getValue()).elementAt(i);
                parameters.put(entry.getKey(), values);
            } else parameters.put(entry.getKey(), new String[] {(String)entry.getValue()});
        }
    }

    public static String getParameterValue(String values[]) {
        return values != null && values.length > 0 ? values[0] : null;
    }

    public W3Request(W3Server w3server) {
        super(w3server.group);
        this.w3server = w3server;
        w3context = W3Context.defaultW3context;
        completionBufferSize = w3server.completionBufferSize;
        if (w3server.secure) ssl = new SecureSocketsLayer();
        sender = new Sender();
        if (!ProxyServlet.disableProxyPipelining)
            responder = new Responder();
    }

    public Object clone() {
        return new W3Request(w3server);
    }

    public URL getContext() {
        return context;
    }

    /** Returns the value of the named attribute as an Object, or null if no attribute
        of the given name exists.
        Attributes can be set two ways. The servlet container may set attributes to
        make available custom information about a request. For example, for requests
        made using HTTPS, the attribute
        javax.servlet.request.X509Certificate can be used to retrieve information
        on the certificate of the client. Attributes can also be set programatically
        using setAttribute(String, Object). This allows information to be
        embedded into a request before a RequestDispatcher call.
        Attribute names should follow the same conventions as package names. This
        specification reserves names matching java.*, javax.*, and sun.*.

        @param name a String specifying the name of the attribute
        @return an Object containing the value of the attribute, or null if the
        attribute does not exist

    */
    public Object getAttribute(String name) {
        if (name != null && name.equals("")) return this;
        return attributes.get(name);
    }

    /** Returns an Enumeration containing the names of the attributes available to
        this request. This method returns an empty Enumeration if the request has no
        attributes available to it.

        @return an Enumeration of strings containing the names of the request's
        attributes

    */
    public Enumeration getAttributeNames() {
        return new IteratorEnumeration<String>(attributes.keySet().iterator());
    }

    /** Returns the name of the character encoding used in the body of this request.
        This method returns <code>null</code> if the request does not specify a character encoding.

        @return a <code>String</code> containing the name of the chararacter encoding, or <code>null</code>
        if the request does not specify a character encoding.

    */
    public String getCharacterEncoding() {
        if (characterEncoding != null) return characterEncoding;
        getContentType();
        return characterEncoding;
    }

    /** Returns the length, in bytes, of the request body and made available by the
        input stream, or -1 if the length is not known. For HTTP servlets, same as the
        value of the CGI variable CONTENT_LENGTH.

        @return an integer containing the length of the request body or -1 if the
        length is not known

    */
    public int getContentLength() {
        return getIntHeader("content-length");
    }

    /** Returns the MIME type of the body of the request, or null if the type is not
        known. For HTTP servlets, same as the value of the CGI variable
        CONTENT_TYPE.

        @return a String containing the name of the MIME type of the request, or
        null if the type is not known

    */
    public String getContentType() {
        if (contentType != null) return contentType;
        contentType = getHeader("content-type");
        if (contentType == null) return null;
        HashMap<String,String> params = new HashMap<String,String>();
        Support.getParameters(contentType, params);
        characterEncoding = params.get("charset");
        return contentType;
    }

    /** Retrieves the body of the request as binary data using a
        ServletInputStream. Either this method or getReader() may be called to
        read the body, not both.

        @return a ServletInputStream object containing the body of the request

        @throws IllegalStateException if the getReader() method has already been
        called for this request

        @throws IOException if an input or output exception occurred

    */
    public ServletInputStream getInputStream()
        throws IOException {
        if (reader != null) throw new IllegalStateException();
        if (inputStream != null) return inputStream;
        String s;
        if (getProtocol().compareTo(W3URLConnection.protocol_1_1) >= 0 && status == -1
            && (s = getHeader("Expect")) != null && s.equals("100-continue")) {
            clear();
            sender.setStatus(100);
            sender.flushBuffer();
            committed = completed = false;
            sender.reset();
            clear();
        }
        store.parsePostData = false;
        return inputStream = new W3InputStream(w3server.type ? new OutputInputStream(in, System.out) : in);
    }

    /** Returns the host name of the Internet Protocol (IP) interface on
        which the request was received.

        @return a <code>String</code> containing the host name of the IP on
        which the request was received. 

    */
    public String getLocalName() {
        return w3server.localAddress.getHostName();
    }

    /** Returns the Internet Protocol (IP) address of the interface on which
        the request was received.

        @return a <code>String</code> containing the IP address on which the
        request was received.

    */       
    public String getLocalAddr() {
        return w3server.localAddress.getHostAddress();
    }

    /** Returns the Internet Protocol (IP) port number of the interface on
        which the request was received.

        @return an integer specifying the port number

    */
    public int getLocalPort() {
        return w3server.serverSocket.getLocalPort();
    }

    /**
     * Gets the servlet context to which this ServletRequest was last dispatched.
     * @return the servlet context to which this ServletRequest was last dispatched
     * @since Servlet 3.0
     */
    public ServletContext getServletContext() {
        return w3context;
    }

    /**
     * Puts this request into asynchronous mode, and initializes its AsyncContext with the original (unwrapped) ServletRequest and ServletResponse objects.
     *
     * Calling this method will cause committal of the associated response to be delayed until {@link AsyncContext#complete()} is called on the returned AsyncContext, or the asynchronous operation has timed out.
     *
     * Calling {@link AsyncContext#hasOriginalRequestAndResponse()} on the returned AsyncContext will return true. Any filters invoked in the outbound direction after this request was put into asynchronous mode may use this as an indication that any request and/or response wrappers that they added during their inbound invocation need not stay around for the duration of the asynchronous operation, and therefore any of their associated resources may be released.
     *
     * This method clears the list of AsyncListener instances (if any) that were registered with the AsyncContext returned by the previous call to one of the startAsync methods, after calling each AsyncListener at its onStartAsync method.
     *
     * Subsequent invocations of this method, or its overloaded variant, will return the same AsyncContext instance, reinitialized as appropriate. 
     * @return the (re)initialized AsyncContext
     * @throws java.lang.IllegalStateException if this request is within the scope of a filter or servlet that does not support asynchronous operations (that is, isAsyncSupported() returns false), or if this method is called again without any asynchronous dispatch (resulting from one of the {@link AsyncContext#dispatch()} methods), is called outside the scope of any such dispatch, or is called again within the scope of the same dispatch, or if the response has already been closed
     * @since Servlet 3.0
     */
    public AsyncContext startAsync() {
        return null;
    }

    /**
     * Puts this request into asynchronous mode, and initializes its AsyncContext with the given request and response objects.
     *
     * The ServletRequest and ServletResponse arguments must be the same instances, or instances of ServletRequestWrapper and ServletResponseWrapper that wrap them, that were passed to the service method of the Servlet or the doFilter method of the Filter, respectively, in whose scope this method is being called.
     *
     * Calling this method will cause committal of the associated response to be delayed until {@link AsyncContext#complete()} is called on the returned AsyncContext, or the asynchronous operation has timed out.
     *
     * Calling {@link AsyncContext#hasOriginalRequestAndResponse()} on the returned AsyncContext will return false, unless the passed in ServletRequest and ServletResponse arguments are the original ones or do not carry any application-provided wrappers. Any filters invoked in the outbound direction after this request was put into asynchronous mode may use this as an indication that some of the request and/or response wrappers that they added during their inbound invocation may need to stay in place for the duration of the asynchronous operation, and their associated resources may not be released. A ServletRequestWrapper applied during the inbound invocation of a filter may be released by the outbound invocation of the filter only if the given servletRequest, which is used to initialize the AsyncContext and will be returned by a call to {@link AsyncContext#getRequest()}, does not contain said ServletRequestWrapper. The same holds true for ServletResponseWrapper instances.
     *
     * This method clears the list of AsyncListener instances (if any) that were registered with the AsyncContext returned by the previous call to one of the startAsync methods, after calling each AsyncListener at its onStartAsync method.
     *
     * Subsequent invocations of this method, or its zero-argument variant, will return the same AsyncContext instance, reinitialized as appropriate. If a call to this method is followed by a call to its zero-argument variant, the specified (and possibly wrapped) request and response objects will remain locked in on the returned AsyncContext. 
     * @param servletRequest the ServletRequest used to initialize the AsyncContext
     * @param servletResponse the ServletResponse used to initialize the AsyncContext
     * @return the (re)initialized AsyncContext 
     * @throws java.lang.IllegalStateException
     * @since Servlet 3.0
     */
    public AsyncContext startAsync(ServletRequest servletRequest,
                                   ServletResponse servletResponse) {
        return null;
    }

    /**
     * Checks if this request has been put into asynchronous mode.
     *
     * A ServletRequest is put into asynchronous mode by calling startAsync() or startAsync(ServletRequest,ServletResponse) on it.
     *
     * This method returns false if this request was put into asynchronous mode, but has since been dispatched using one of the {@link #AsyncContext#dispatch()} methods or released from asynchronous mode via a call to {@link AsyncContext#complete}.
     * @return true if this request has been put into asynchronous mode, false otherwise
     * @since Servlet 3.0
     */
    public boolean isAsyncStarted() {
        return false;
    }

    /**
     * Checks if this request supports asynchronous operation.
     *
     * Asynchronous operation is disabled for this request if this request is within the scope of a filter or servlet that has not been annotated or flagged in the deployment descriptor as being able to support asynchronous handling. 
     * @return true if this request supports asynchronous operation, false otherwise
     * @since Servlet 3.0
     */
    public boolean isAsyncSupported() {
        return false;
    }

    /**
     * Gets the AsyncContext that was created or reinitialized by the most recent invocation of startAsync() or startAsync(ServletRequest,ServletResponse) on this request.
     * @return the AsyncContext that was created or reinitialized by the most recent invocation of startAsync() or startAsync(ServletRequest,ServletResponse) on this request
     * @throws java.lang.IllegalStateException if this request has not been put into asynchronous mode, i.e., if neither startAsync() nor startAsync(ServletRequest,ServletResponse) has been called
     * @since Servlet 3.0
     */
    public AsyncContext getAsyncContext() {
        return null;
    }

    /**
     * Gets the dispatcher type of this request.
     *
     * The dispatcher type of a request is used by the container to select the filters that need to be applied to the request: Only filters with matching dispatcher type and url patterns will be applied.
     *
     * Allowing a filter that has been configured for multiple dispatcher types to query a request for its dispatcher type allows the filter to process the request differently depending on its dispatcher type.
     *
     * The initial dispatcher type of a request is defined as DispatcherType.REQUEST. The dispatcher type of a request dispatched via {@link RequestDispatcher#forward(ServletRequest, ServletResponse)} or {@link RequestDispatcher#include(ServletRequest, ServletResponse)} is given as DispatcherType.FORWARD or DispatcherType.INCLUDE, respectively, while the dispatcher type of an asynchronous request dispatched via one of the {@link AsyncContext#dispatch} methods is given as DispatcherType.ASYNC. Finally, the dispatcher type of a request dispatched to an error page by the container's error handling mechanism is given as DispatcherType.ERROR. 
     * @return the dispatcher type of this request
     * @since Servlet 3.0
     */
    public DispatcherType getDispatcherType() {
        return DispatcherType.REQUEST;
    }

    /** Returns the preferred Locale that the client will accept content in, based on
        the Accept-Language header. If the client request doesn't provide an Accept-Language
        header, this method returns the default locale for the server.

        @return the preferred Locale for the client

        @see setLocale(Locale)
    */
    public Locale getLocale() {
        return (Locale)getLocales().nextElement();
    }

    /** Returns an Enumeration of Locale objects indicating, in decreasing order
        starting with the preferred locale, the locales that are acceptable to the client
        based on the Accept-Language header. If the client request doesn't provide an
        Accept-Language header, this method returns an Enumeration containing
        one Locale, the default locale for the server.

        @return an Enumeration of preferred Locale objects for the client

    */
    public Enumeration getLocales() {
        if (locales == null) {
            locales = W3URLConnection.getLocales(getHeader("accept-language"));
            if (locales == null || locales.isEmpty()) {
                locales = new Vector<Locale>();
                locales.addElement(Locale.getDefault());
            }
        }
        return locales.elements();
    }

    /** Returns the value of a request parameter as a String, or null if the parameter
        does not exist. Request parameters are extra information sent with the
        request. For HTTP servlets, parameters are contained in the query string or
        posted form data.
        You should only use this method when you are sure the parameter has only
        one value. If the parameter might have more than one value, use
        getParameterValues(String).
        If you use this method with a multivalued parameter, the value returned is
        equal to the first value in the array returned by getParameterValues.
        If the parameter data was sent in the request body, such as occurs with an
        HTTP POST request, then reading the body directly via getInputStream()
        or getReader() can interfere with the execution of this method.

        @param name a String specifying the name of the parameter
        @return a String representing the single value of the parameter
        @see getParameterValues(String)

    */
    public String getParameter(String name) {
        return getParameterValue(getParameterValues(name));
    }

    /** Returns a java.util.Map of the parameters of this request. Request parameters
        are extra information sent with the request. For HTTP servlets, parameters
        are contained in the query string or posted form data.

        @return an immutable java.util.Map containing parameter names as keys
        and parameter values as map values. The keys in the parameter map are of
        type String. The values in the parameter map are of type String array.

    */
    public Map<String,String[]> getParameterMap() {
        checkPostData();
        return queryParameters;
    }

    /** Returns an Enumeration of String objects containing the names of the
        parameters contained in this request. If the request has no parameters, the
        method returns an empty Enumeration.

        @return an Enumeration of String objects, each String containing the
        name of a request parameter; or an empty Enumeration if the request has no
        parameters

    */
    public Enumeration<String> getParameterNames() {
        checkPostData();
        return queryKeys.elements();
    }

    /** Returns an array of String objects containing all of the values the given
        request parameter has, or null if the parameter does not exist.
        If the parameter has a single value, the array has a length of 1.

        @param name a String containing the name of the parameter whose value is
        requested
        @return an array of String objects containing the parameter's values
        @see getParameter(String)

    */
    public String[] getParameterValues(String name) {
        checkPostData();
        return queryParameters.get(name);
    }

    /** Returns the name and version of the protocol the request uses in the form
        protocol/majorVersion.minorVersion, for example, HTTP/1.1. For HTTP
        servlets, the value returned is the same as the value of the CGI variable
        SERVER_PROTOCOL.

        @return a String containing the protocol name and version number

    */
    public String getProtocol() {
        return store.protocol;
    }

    /** Retrieves the body of the request as character data using a <code>BufferedReader</code>.
        The reader translates the character data according to the character encoding
        used on the body. Either this method or {@link #getInputStream()} may be called to
        read the body, not both.

        @return a <code>BufferedReader</code> containing the body of the request

        @throws UnsupportedEncodingException if the character set encoding used is not
        supported and the text cannot be decoded

        @throws IllegalStateException if getInputStream() method has been called on
        this request

        @throws IOException if an input or output exception occurred

        @see getInputStream()

    */
    public BufferedReader getReader()
        throws IOException {
        if (inputStream != null) throw new IllegalStateException();
        if (reader != null) return reader;
        String characterEncoding = getCharacterEncoding();
        if (characterEncoding != null) reader = new BufferedReader(new InputStreamReader(getInputStream(), Support.getCharacterEncoding(characterEncoding)));
        else reader = new BufferedReader(new InputStreamReader(getInputStream()));
        return reader;
    }

    /** @deprecated Deprecated as of Version 2.1 of the Java Servlet API, use
        ServletContext.getRealPath(String) instead.

    */
    @Deprecated
    public String getRealPath(String path) {
        return w3context.getRealPath(path);
    }

    /** Returns the Internet Protocol (IP) address of the client that sent the request.
        For HTTP servlets, same as the value of the CGI variable REMOTE_ADDR.

        @return a String containing the IP address of the client that sent the
        request

    */
    public String getRemoteAddr() {
        return socket.getInetAddress().getHostAddress();
    }

    /** Returns the fully qualified name of the client that sent the request. If the
        engine cannot or chooses not to resolve the hostname (to improve performance),
        this method returns the dotted-string form of the IP address. For
        HTTP servlets, same as the value of the CGI variable REMOTE_HOST.

        @return a String containing the fully qualified name of the client

    */
    public String getRemoteHost() {
        return socket.getInetAddress().getHostName();
    }

    /** Returns the Internet Protocol (IP) source port of the client or last
        proxy that sent the request. 

        @return an integer specifying the port number

    */
    public int getRemotePort() {
        return socket.getLocalPort();
    }

    /** Returns a RequestDispatcher object that acts as a wrapper for the resource
        located at the given path. A RequestDispatcher object can be used to forward
        a request to the resource or to include the resource in a response. The
        resource can be dynamic or static.
        The pathname specified may be relative, although it cannot extend outside the
        current servlet context. If the path begins with a "/" it is interpreted as relative
        to the current context root. This method returns null if the servlet container
        cannot return a RequestDispatcher. The difference between this method and
        ServletContext.getRequestDispatcher(String) is that this method can
        take a relative path.

        @param path a String specifying the pathname to the resource
        @return a RequestDispatcher object that acts as a wrapper for the
        resource at the specified path
        @see RequestDispatcher,ServletContext.getRequestDispatcher(String)

    */
    public RequestDispatcher getRequestDispatcher(String path) {
        return w3context.getRequestDispatcher(getRequestURI(), path);
    }

    /** Returns the name of the scheme used to make this request, for example, http,
        https,or ftp. Different schemes have different rules for constructing URLs,
        as noted in RFC 1738.

        @return a String containing the name of the scheme used to make this
        request

    */
    public String getScheme() {
        return local ? w3server.serverScheme : scheme;
    }

    /** Returns the host name of the server that received the request. For HTTP servlets,
        same as the value of the CGI variable SERVER_NAME.

        @return a String containing the name of the server to which the request
        was sent

    */
    public String getServerName() {
        return w3server.serverName;
    }

    /** Returns the port number on which this request was received. For HTTP servlets,
        same as the value of the CGI variable SERVER_PORT.

        @return an integer specifying the port number

    */
    public int getServerPort() {
        return local ? w3server.serverPort : url.getPort();
    }

    /** Returns a boolean indicating whether this request was made using a secure
        channel, such as HTTPS.

        @return a boolean indicating if the request was made using a secure
        channel

    */
    public boolean isSecure() {
        return w3server.secure;
    }

    /** Removes an attribute from this request. This method is not generally needed
        as attributes only persist as long as the request is being handled.
        Attribute names should follow the same conventions as package names.
        Names beginning with java.*, javax.*, and com.sun.*, are reserved for use
        by Sun Microsystems.

        @param name a String specifying the name of the attribute to remove

    */
    public void removeAttribute(String name) {
        Object value = attributes.remove(name);
        if (value == null) return;
        w3context.requestAttributeRemoved(this, name, value);
    }

    /** Stores an attribute in this request. Attributes are reset between requests. This
        method is most often used in conjunction with RequestDispatcher.
        Attribute names should follow the same conventions as package names.
        Names beginning with java.*, javax.*, and com.sun.*, are reserved for use
        by Sun Microsystems.
        If the value passed in is null, the effect is the same as calling
        removeAttribute(String).

        @param name a String specifying the name of the attribute
        @param o the Object to be stored

    */
    public void setAttribute(String name, Object object) {
        if (object == null) {
            removeAttribute(name);
            return;
        }
        Object oldValue = attributes.get(name);
        attributes.put(name, object);
        if (oldValue != null)
            w3context.requestAttributeReplaced(this, name, object);
        else w3context.requestAttributeAdded(this, name, object);
    }

    /** Overrides the name of the character encoding used in the body of this
        request. This method must be called prior to reading request parameters or
        reading input using getReader().

        @param a String containing the name of the chararacter encoding.

        @throws java.io.UnsupportedEncodingException - if this is not a valid
        encoding

    */
    public void setCharacterEncoding(String characterEncoding)
        throws UnsupportedEncodingException {
        new String(new byte[0], characterEncoding);
        W3Request.this.characterEncoding = characterEncoding;
    }

    /** Returns the name of the authentication scheme used to protect the servlet. All
        servlet containers support basic, form and client certificate authentication,
        and may additionally support digest authentication. If the servlet is not
        authenticated null is returned.
        Same as the value of the CGI variable AUTH_TYPE.

        @return one of the static members BASIC_AUTH, FORM_AUTH,
        CLIENT_CERT_AUTH, DIGEST_AUTH (suitable for == comparison)
        indicating the authentication scheme, or null if the request was not
        authenticated.

    */
    public String getAuthType() {
        return authType;
    }

    /** Returns the portion of the request URI that indicates the context of the
        request. The context path always comes first in a request URI. The path starts
        with "/" character but does not end with a "/" character. For servlets in the
        default (root) context, this method returns "". The container does not decode
        this string.

        @return a String specifying the portion of the request URI that indicates
        the context of the request

    */
    public String getContextPath() {
        return contextPath;
    }

    /** Returns an array containing all of the Cookie objects the client sent with this
        request. This method returns null if no cookies were sent.

        @return an array of all the Cookies included with this request, or null if
        the request has no cookies

    */
    public Cookie[] getCookies() {
        return cookies;
    }

    /** Returns the value of the specified request header as a long value that represents
        a Date object. Use this method with headers that contain dates, such as
        If-Modified-Since.
        The date is returned as the number of milliseconds since January 1, 1970 GMT.
        The header name is case insensitive.
        If the request did not have a header of the specified name, this method returns
        -1. If the header can't be converted to a date, the method throws an IllegalArgumentException.

        @param name a String specifying the name of the header
        @return a long value representing the date specified in the header
        expressed as the number of milliseconds since January 1, 1970 GMT, or -1 if
        the named header was not included with the reqest
        @throws IllegalArgumentException If the header value can't be converted to a date

    */
    public long getDateHeader(String name) {
        String value = getHeader(name);
        if (value == null) return -1L;
        Date date = Support.parse(Support.getParameters(value, null));
        if (date == null) throw new IllegalArgumentException();
        return date.getTime();
    }

    /** Returns the value of the specified request header as a String. If the request
        did not include a header of the specified name, this method returns null. The
        header name is case insensitive. You can use this method with any request
        header.

        @param name a String specifying the header name
        @return a String containing the value of the requested header, or null if
        the request does not have a header of that name

    */
    public String getHeader(String name) {
        return store.headerList.getHeaderValue(name);
    }

    /** Returns an enumeration of all the header names this request contains. If the
        request has no headers, this method returns an empty enumeration.
        Some servlet containers do not allow do not allow servlets to access headers
        using this method, in which case this method returns null

        @return an enumeration of all the header names sent with this request; if
        the request has no headers, an empty enumeration; if the servlet container
        does not allow servlets to use this method, null

    */
    public Enumeration getHeaderNames() {
        return new HeaderNames(store.headerList);
    }

    /** Returns all the values of the specified request header as an Enumeration of
        String objects.
        Some headers, such as Accept-Language can be sent by clients as several
        headers each with a different value rather than sending the header as a
        comma separated list.
        If the request did not include any headers of the specified name, this method
        returns an empty Enumeration. The header name is case insensitive. You can
        use this method with any request header.

        @param name a String specifying the header name
        @return an Enumeration containing the values of the requested header. If
        the request does not have any headers of that name return an empty
        enumeration. If the container does not allow access to header information,
        return null

    */
    public Enumeration getHeaders(String name) {
        return new Headers(store.headerList, name);
    }

    /** Returns the value of the specified request header as an int. If the request
        does not have a header of the specified name, this method returns -1. If the
        header cannot be converted to an integer, this method throws a Number-
        FormatException.
        The header name is case insensitive.

        @param name a String specifying the name of a request header
        @return an integer expressing the value of the request header or -1 if the
        request doesn't have a header of this name
        @throws NumberFormatException If the header value can't be converted to an int

    */
    public int getIntHeader(String name) {
        String value = getHeader(name);
        if (value == null) return -1;
        return Integer.parseInt(value);
    }

    /** Returns the name of the HTTP method with which this request was made, for
        example, GET, POST, or PUT. Same as the value of the CGI variable
        REQUEST_METHOD.

        @return a String specifying the name of the method with which this
        request was made

    */
    public String getMethod() {
        return store.method;
    }

    /** Returns any extra path information associated with the URL the client sent
        when it made this request. The extra path information follows the servlet path
        but precedes the query string. This method returns null if there was no extra
        path information.
        Same as the value of the CGI variable PATH_INFO.

        @return a String, decoded by the web container, specifying extra path
        information that comes after the servlet path but before the query string in the
        request URL; or null if the URL does not have any extra path information

    */
    public String getPathInfo() {
        return pathInfo;
    }

    /** Returns any extra path information after the servlet name but before the
        query string, and translates it to a real path. Same as the value of the CGI
        variable PATH_TRANSLATED.
        If the URL does not have any extra path information, this method returns
        null. The web container does not decode this string.

        @return a String specifying the real path, or null if the URL does not
        have any extra path information

    */
    public String getPathTranslated() {
        String path = getPathInfo();
        return path != null ? w3context.getRealPath(path) : null;
    }

    /** Returns the query string that is contained in the request URL after the path.
        This method returns null if the URL does not have a query string. Same as
        the value of the CGI variable QUERY_STRING.

        @return a String containing the query string or null if the URL contains
        no query string. The value is not decoded by the container.

    */
    public String getQueryString() {
        return queryString;
    }

    /** Returns the login of the user making this request, if the user has been authenticated,
        or null if the user has not been authenticated. Whether the user name
        is sent with each subsequent request depends on the browser and type of
        authentication. Same as the value of the CGI variable REMOTE_USER.

        @return a String specifying the login of the user making this request, or
        null</code if the user login is not known

    */
    public String getRemoteUser() {
        if (remoteUser == null) getUserPrincipal();
        return remoteUser;
    }

    /** Returns the session ID specified by the client. This may not be the same as
        the ID of the actual session in use. For example, if the request specified an old
        (expired) session ID and the server has started a new session, this method
        gets a new session with a new ID. If the request did not specify a session ID,
        this method returns null.

        @return a String specifying the session ID, or null if the request did not
        specify a session ID

        @see isRequestedSessionIdValid()

    */
    public String getRequestedSessionId() {
        return requestedSessionId;
    }

    /** Returns the part of this request's URL from the protocol name up to the query
        string in the first line of the HTTP request. The web container does not
        decode this String. For example:

        <table>
        <tr><th>First line of HTTP request</th><th>Returned Value</th></tr>
        <tr><td>POST /some/path.html HTTP/1.1</td><td>/some/path.html</td></tr>
        <tr><td>GET http://foo.bar/a.html HTTP/1.0</td><td>/a.html</td></tr>
        <tr><td>HEAD /xyz?a=b HTTP/1.1</td><td>/xyz</td></tr>
        </table>

        To reconstruct an URL with a scheme and host, use
        HttpUtils.getRequestURL(HttpServletRequest).

        @return a String containing the part of the URL from the protocol
        name up to the query string

        @see HttpUtils.getRequestURL(HttpServletRequest)

    */
    public String getRequestURI() {
        return store.requestURI;
    }

    /** Reconstructs the URL the client used to make the request. The returned URL
        contains a protocol, server name, port number, and server path, but it does not
        include query string parameters.
        Because this method returns a StringBuffer, not a string, you can modify the
        URL easily, for example, to append query parameters.
        This method is useful for creating redirect messages and for reporting errors.

        @return a StringBuffer object containing the reconstructed URL

    */
    public StringBuffer getRequestURL() {
        return new StringBuffer(getScheme() + "://" + getServerName() + ":" + getServerPort() + getRequestURI());
    }

    /** Returns the part of this request's URL that calls the servlet. This includes either
        the servlet name or a path to the servlet, but does not include any extra path
        information or a query string. Same as the value of the CGI variable
        SCRIPT_NAME.

        @return a String containing the name or path of the servlet being
        called, as specified in the request URL, decoded.

    */
    public String getServletPath() {
        return servletPath;
    }

    /** Returns the current session associated with this request, or if the request does
        not have a session, creates one.

        @return the HttpSession associated with this request

        @see getSession(boolean)

    */
    public HttpSession getSession() {
        return getSession(true);
    }

    /** Returns the current HttpSession associated with this request or, if if there is no
        current session and create is true, returns a new session.
        If create is false and the request has no valid HttpSession, this method
        returns null.
        To make sure the session is properly maintained, you must call this method
        before the response is committed. If the container is using cookies to maintain
        session integrity and is asked to create a new session when the response is
        committed, an IllegalStateException is thrown.
        Parameters:
        <code>true</code> - to create a new session for this request if necessary; false
        to return null if there is no current session

        @return the HttpSession associated with this request or null if create
        is false and the request has no valid session

        @see getSession()

    */
    public HttpSession getSession(boolean create) {
        if (!create && session != null) return session;
        session = w3context.getSession(this, create);
        return session;
    }

    /** Returns a java.security.Principal object containing the name of the current
        authenticated user. If the user has not been authenticated, the method returns
        null.

        @return a java.security.Principal containing the name of the user
        making this request; null if the user has not been authenticated

    */
    public Principal getUserPrincipal() {
        if (userPrincipal != null) return userPrincipal;
        userPrincipal = w3context.getUserPrincipal(this);
        if (userPrincipal != null) remoteUser = userPrincipal.getName();
        return userPrincipal;
    }

    /** Checks whether the requested session ID came in as a cookie.

        @return true if the session ID came in as a cookie; otherwise, false

        @see getSession(boolean)

    */
    public boolean isRequestedSessionIdFromCookie() {
        return requestedSessionId != null;
    }

    /** @deprecated Deprecated as of Version 2.1 of the Java Servlet API, use
        isRequestedSessionIdFromURL() instead.

    */
    @Deprecated
    public boolean isRequestedSessionIdFromUrl() {
        return isRequestedSessionIdFromURL();
    }

    /** Checks whether the requested session ID came in as part of the request URL.

        @return true if the session ID came in as part of a URL; otherwise,
        false

        @see getSession(boolean)

    */
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    /** Checks whether the requested session ID is still valid.

        @return true if this request has an id for a valid session in the current
        session context; false otherwise

        @see getRequestedSessionId(), getSession(boolean), HttpSessionContext

    */
    public boolean isRequestedSessionIdValid() {
        return session != null && !session.invalidated && !session.isNew;
    }

    /** Returns a boolean indicating whether the authenticated user is included in the
        specified logical "role". Roles and role membership can be defined using
        deployment descriptors. If the user has not been authenticated, the method
        returns false.

        @param role a String specifying the name of the role
        @return a boolean indicating whether the user making this request
        belongs to a given role; false if the user has not been authenticated

    */
    public boolean isUserInRole(String role) {
        return false;
    }

    /**
     * Use the container login mechanism configured for the ServletContext to authenticate the user making this request.
     *
     * This method may modify and commit the argument HttpServletResponse. 
     * @param response The HttpServletResponse associated with this HttpServletRequest
     * @return true when non-null values were or have been established as the values returned by {@link #getUserPrincipal()}, {@link #getRemoteUser()}, and {@link #getAuthType()}. Return false if authentication is incomplete and the underlying login mechanism has committed, in the response, the message (e.g., challenge) and HTTP status code to be returned to the user.
     * @throws IOException if an input or output error occurred while reading from this request or writing to the given response
     * @throws IllegalStateException if the login mechanism attempted to modify the response and it was already committed 
     * @throws ServletException if the authentication failed and the caller is responsible for handling the error (i.e., the underlying login mechanism did NOT establish the message and HTTP status code to be returned to the user)
     * @since Servlet 3.0
     */
    public boolean authenticate(HttpServletResponse response)
        throws IOException, ServletException {
        return false;
    }

    /**
     * Validate the provided username and password in the password validation realm used by the web container login mechanism configured for the ServletContext.
     *
     * This method returns without throwing a ServletException when the login mechanism configured for the ServletContext supports username password validation, and when, at the time of the call to login, the identity of the caller of the request had not been established (i.e, all of getUserPrincipal, getRemoteUser, and getAuthType return null), and when validation of the provided credentials is successful. Otherwise, this method throws a ServletException as described below.
     *
     * When this method returns without throwing an exception, it must have established non-null values as the values returned by getUserPrincipal, getRemoteUser, and getAuthType.
     * @param username The String value corresponding to the login identifier of the user.
     * @param password The password String corresponding to the identified user.
     * @throws ServletException if the configured login mechanism does not support username password authentication, or if a non-null caller identity had already been established (prior to the call to login), or if validation of the provided username and password fails.
     *             If any of {@link #getRemoteUser()},
     *             {@link #getUserPrincipal()} or {@link #getAuthType()} are
     *             non-null, if the configured authenticator does not support
     *             user name and password authentication or if the
     *             authentication fails
     * @since Servlet 3.0
     */
    public void login(String username, String password)
        throws ServletException {
    }

    /**
     * Establish null as the value returned when getUserPrincipal, getRemoteUser, and getAuthType is called on the request.
     * @throws ServletException
     *             If the logout fails
     * @since Servlet 3.0
     */
    public void logout()
        throws ServletException {
    }

    /**
     * Return a collection of all uploaded Parts.
     * Gets all the Part components of this request, provided that it is of type multipart/form-data.
     *
     * If this request is of type multipart/form-data, but does not contain any Part components, the returned Collection will be empty.
     *
     * Any changes to the returned Collection must not affect this HttpServletRequest. 
     * 
     * @return a (possibly empty) Collection of the Part components of this request
     * @throws IOException if an I/O error occurred during the retrieval of the Part components of this request
     * @throws IllegalStateException if the request body is larger than maxRequestSize, or any Part in the request is larger than maxFileSize
     * @throws ServletException if this request is not of type multipart/form-data
     * @since Servlet 3.0
     */
    public Collection<Part> getParts()
        throws IOException,
               IllegalStateException, ServletException {
        return null;
    }

    /**
     * Gets the named Part or null if the Part does not exist. Triggers upload
     * of all Parts.
     * 
     * @param name the name of the requested Part 
     * @return The Part with the given name, or null if this request is of type multipart/form-data, but does not contain the requested Part 
     * @throws IOException if an I/O error occurred during the retrieval of the requested Part
     * @throws IllegalStateException if the request body is larger than maxRequestSize, or any Part in the request is larger than maxFileSize
     * @throws ServletException if this request is not of type multipart/form-data
     * @since Servlet 3.0
     */
    public Part getPart(String name)
        throws IOException, IllegalStateException,
               ServletException {
        return null;
    }

    public Enumeration<Header> getHeaders() {
        Iterator<Header> headerItems = store.headerList.iterator();
        headerItems.next();
        return new IteratorEnumeration<Header>(headerItems);
    }

    public HeaderList getHeaderList() {
        return store.headerList;
    }

    public void clear() {
        locale = null;
        firstOut = lastOut = null;
        firstWriter = lastWriter = null;
        filterOut = null;
        filterLock.release();
        w3out = null;
    }

    public W3Server getEngine() {
        return null;
    }

    public String getProperty(String name) {
        return responseHeaderList.getHeaderValue(name);
    }

    public int getResponseContentLength() {
        String contentLength = getProperty("content-length");
        if (contentLength != null)
            try {
                return Integer.parseInt(contentLength);
            } catch (NumberFormatException ex) {}
        return -1;
    }

    public String getResponseContentType() {
        return getProperty("content-type");
    }

    public long getResponseLastModified() {
        if (responseLastModified != -1L) return responseLastModified;
        Date d;
        String s;
        if ((s = getProperty("last-modified")) != null &&
            (d = Support.parse(Support.getParameters(s, null))) != null &&
            (responseLastModified = d.getTime()) != 0L) return responseLastModified;
        return responseLastModified = 0L;
    }

    public void setNotModified()
        throws IOException {
        sender.reset();
        sender.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
    }

    public void unsetHeader(String name) {
        responseHeaderList.removeAll(name);
    }

    public void unsetContentLength() {
        unsetHeader("content-length");
    }

    public void setLastModified(long lastModified) {
        sender.setDateHeader("Last-Modified", lastModified);
    }

    public void sendUnauthorized()
        throws IOException {
        if (sender.isCommitted()) throw new IllegalStateException();
        clear();
        String userAgent = getHeader("user-agent");
        if (userAgent == null || userAgent.toLowerCase().startsWith("mozilla/1.")) return;
        sender.setContentType(Support.htmlType);
        unsetContentLength();
        PrintWriter writer = sender.getWriter();
        String title = HttpServletResponse.SC_UNAUTHORIZED + " " + w3server.messages.getString("unauthorized");
        writer.println("<html><head><title>" + title + "</title></head><body>");
        writer.println("<h1>" + title + "</h1>");
        writer.println("<h2>" + w3server.messages.getString("unauthorizedToGet") + " " + Support.htmlString(url.toExternalForm()) + "</h2>");
        writer.println("<form action=\"" + new URL(w3server.context, "/servlet/public/auth").toExternalForm() + "\" method=GET>");
        writer.println("<input name=location type=hidden value=\"" + URLEncoder.encode(store.requestURI, "8859_1") + "\">");
        writer.println("<input name=challenge type=hidden value=\"" + URLEncoder.encode(challenge, "8859_1") + "\">");
        writer.println("<table>");
        writer.println("<tr><th>" + w3server.messages.getString("username") + "</th><td><input name=username type=text size=55></td></tr>");
        writer.println("<tr><th>" + w3server.messages.getString("password") + "</th><td><input name=password type=password size=55></td></tr>");
        writer.println("</table>");
        writer.println("<input type=submit value=\"" + w3server.messages.getString("login") + "\">");
        writer.print("</form></body></html>");
        sender.flushBuffer();
    }

    public void sendResponseHeaderList(OutputStream out, Store store)
        throws IOException {
        if (getProtocol().compareTo(W3URLConnection.protocol_1_0) <= 0) {
            if (store.keep && (status == 100 ||
                               status == 101 ||
                               status == 199 ||
                               status == HttpServletResponse.SC_NO_CONTENT ||
                               status == HttpServletResponse.SC_NOT_MODIFIED ||
                               status == HttpServletResponse.SC_PRECONDITION_FAILED ||
                               getResponseContentLength() > 0)) {
                if (store.simple) {
                    sender.setHeader("Connection", "Keep-Alive");
                    unsetHeader("proxy-connection");
                } else {
                    sender.setHeader("Proxy-Connection", "Keep-Alive");
                    unsetHeader("connection");
                }
                if (requestNumber == 1) {
                    StringBuffer sb = new StringBuffer();
                    if (w3server.keepAlive != null) sb.append(w3server.keepAlive);
                    if (w3server.keepAliveTimeoutSecs != -1) {
                        if (sb.length() > 0) sb.append(", ");
                        sb.append("timeout=").append(String.valueOf(w3server.keepAliveTimeoutSecs));
                    }
                    if (w3server.keepAliveCount != -1) {
                        if (sb.length() > 0) sb.append(", ");
                        sb.append("max=").append(String.valueOf(w3server.keepAliveCount));
                    }
                    if (sb.length() > 0) sender.setHeader("Keep-Alive", sb.toString());
                    else unsetHeader("keep-alive");
                } else unsetHeader("keep-alive");
            } else {
                sender.setHeader("Connection", "close");
                store.keep = false;
            }
        } else if (store.keep ||
                   (status == 100 ||
                    status == 101 ||
                    status == 199) &&
                   getResponseContentLength() <= 0) unsetHeader("connection");
        else {
            sender.setHeader("Connection", "close");
            store.keep = false;
        }
        if (!store.keep) {
            unsetHeader("keep-alive");
            unsetHeader("proxy-connection");
        }
        //unsetHeader("proxy-authenticate");
        unsetHeader("trailer");
        unsetHeader("upgrade");
        if (!responseHeaderList.hasHeader("")) sender.setStatus(HttpServletResponse.SC_OK);
        int i = 1;
        if (!responseHeaderList.hasHeader("server")) responseHeaderList.insert(new Header("Server", w3server.serverInfo), i++);
        if (!responseHeaderList.hasHeader("date")) responseHeaderList.insert(new Header("Date", Support.format(new Date())), i++);
        if (!responseHeaderList.hasHeader("accept-ranges")) responseHeaderList.insert(new Header("Accept-Ranges", "bytes"), i);
        if (sessionSetCookie != null) sender.addHeader("Set-Cookie", sessionSetCookie);
        if (w3server.verbose) {
            System.out.println(store.request);
            responseHeaderList.dump(System.out);
        }
        committed = true;
        Support.sendMessage(out, responseHeaderList);
    }

    public boolean mayUseTrailer() {
        boolean useTrailer = false;
        Enumeration tes = getHeaders("te");
        while (tes.hasMoreElements())
            if (((String)tes.nextElement()).trim().equals("trailers")) {
                useTrailer = true;
                break;
            }
        return useTrailer;
    }

    public void checkPostData() {
        if (!store.parsePostData) return;
        store.parsePostData = false;
        try {
            W3URLConnection.parseQuery(getInputStream(), queryValues, queryKeys);
        } catch (IOException ex) {}
        setParameters(queryParameters, queryValues);
    }

    /** Waits for servlet filters to complete and then flushes output stream. */
    public void complete()
        throws IOException {
        if (completed) return;
        completed = true;
        if (w3server.verbose) w3server.log("Completing request " + getName() + "/" + requestNumber);
        if (firstWriter != null) firstWriter.flush();
        else if (firstOut != null) firstOut.flush();
        for (boolean tried = false;; tried = true) {
            if (filterOut != null) {
                filterOut.close();
                filterOut = null;
                try {
                    filterLock.lock();
                } catch (InterruptedException ex) {
                    store.keep = false;
                    mustStop = true;
                }
            }
            if (w3out != null) tried = true;
            sender.flushBuffer();
            if (tried) break;
        }
        if (firstWriter != null) firstWriter.close();
        else w3out.close();
    }

    /** Begins to handle request. */
    public void begin()
        throws IOException {
        startTime = System.currentTimeMillis();
        protocol = getProtocol().compareTo(W3URLConnection.protocol_1_1) >= 0 ? W3URLConnection.protocol_1_1 : getProtocol();
        byteCountReceived = byteCountSent = 0;
        committed = completed = unauthorized = false;
        chain = null;
        characterEncoding = null;
        contentType = null;
        store.clientMethod = store.method;
        store.clientHeaderList = store.headerList;
        inputStream = null;
        authType = null;
        locales = null;
        reader = null;
        session = null;
        sessionSetCookie = null;
        requestedSessionId = null;
        acceptTypes = (accept = getHeader("accept")) != null
            ? W3URLConnection.getList(accept, null, null) : null;
        acceptCharsets = (acceptCharset = getHeader("accept-charset")) != null
            ? W3URLConnection.getList(acceptCharset, "ISO-8859-1", null) : null;
        cookies = W3URLConnection.getCookies(store.headerList);
        if (cookies != null) {
            if (getHeader("authorization") == null) {
                Cookie cookie = W3URLConnection.getCookie(w3server.auth.getClass().getName(), cookies);
                if (cookie != null) store.headerList.append(new Header("Authorization", java.net.URLDecoder.decode(cookie.getValue(), "8859_1")));
            }
            Cookie sessionCookie = W3URLConnection.getCookie(w3server.sessionCookieName, cookies);
            if (sessionCookie != null)
                requestedSessionId = sessionCookie.getValue();
        }
        if ((authorization = getHeader("proxy-authorization")) != null ||
            (authorization = getHeader("authorization")) != null) {
            StringTokenizer st = new StringTokenizer(authorization);
            if (st.hasMoreTokens()) authType = st.nextToken();
        }
        attributes.clear();
        queryParameters = new HashMap<String,String[]>();
        queryValues = new HashMap<String,Object>();
        queryKeys = new Vector<String>();
        userPrincipal = null;
        invokeNumber = 0;
        sender.reset();
        clear();
    }

    /** Checks if total number of requests per server or per client exceeds the maximum. */
    public void checkLimits()
        throws IOException {
        // checks if total number of requests exceeds the limit
        if (w3server.maxRequests >= 0 && activeCount() - w3server.pool.poolCount() > w3server.maxRequests) {
            store.keep = false;
            throw new W3Exception(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Too many requests");
        }
        if (w3server.maxConnections != -1) {
            // checks if maximum number of connections per client exceeds the limit
            if ((client = w3server.clients.get(remoteAddr)) == null)
                w3server.clients.put(remoteAddr, client = new Client());
            if (++client.connections > w3server.maxConnections) {
                store.keep = false;
                throw new W3Exception(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Too many connections");
            }
        }
    }

    public boolean checkDomain(String matchPath)
        throws IOException {
        if (w3server.domains != null)
            domain = (Domain)w3server.domains.match(matchPath);
        else domain = null;
        boolean isForbiddenDomain = false;
        if (domain != null) {
            if (domain.name.equals("hidden")) isForbiddenDomain = true;
            else if (domain.name.equals("local")) {
                if (w3server.isLocalClient(socket.getInetAddress())) domain = null;
                else isForbiddenDomain = true;
            } else if (domain.name.equals("mobile"))
                if (!mobile) {
                    if (domain.secondName != null) {
                        Domain secondDomain = w3server.domainEntries.get(domain.secondName);
                        if (secondDomain != null) domain = secondDomain;
                    }
                } else domain = null;
        } else isForbiddenDomain = matchPath.startsWith("/hidden/");
        if (isForbiddenDomain) {
            sender.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden to access " + normPath);
            return true;
        }
        if (domain != null && !domain.name.equals("public"))
            try {
                if (mobile) w3server.mobile.servlet.service(this, sender);
                else domain.auth.servlet.service(this, sender);
            } catch (ServletException ex) {
                sender.setStatus(local ? HttpServletResponse.SC_UNAUTHORIZED : HttpServletResponse.SC_PROXY_AUTHENTICATION_REQUIRED);
                sender.setHeader(local ? "Www-Authenticate" : "Proxy-Authenticate", challenge);
                sender.flushBuffer();
                return true;
            }
        return false;
    }

    /** Handles HTTP-request.

        @param requestURI request parameter

    */
    public boolean handle(String uri)
        throws IOException, ServletException {
        store.requestURI = uri;
        String requestURI = uri;
        for (boolean tried = false;; tried = true) {
            url = new URL(w3server.defaultContext, requestURI);
            if (!tried) {
                context = url;
                requestURL = url.toString();
                store.headerList.removeAll("TE");
                store.headerList.removeAll("trailer");
                store.headerList.removeAll("upgrade");
            }
            scheme = url.getProtocol().toLowerCase();
            requestPath = url.getFile();
            host = url.getHost().trim().toLowerCase();
            if (host.startsWith("[")) host = host.substring(1);
            if (host.endsWith("]")) host = host.substring(0, host.length() - 1);
            host = host.trim();
            port = url.getPort();
            local = scheme.equals(w3server.defaultContext.getProtocol()) && w3server.isLocal(host) && (port == w3server.port || port == -1 && w3server.port == w3server.defaultPort);
            String s = getHeader("host");
            if (getProtocol().compareTo(W3URLConnection.protocol_1_1) >= 0 && s == null) throw new W3Exception(HttpServletResponse.SC_BAD_REQUEST, "Host-header missing");
            if (local && !tried && w3server.virtualHosts != null && s != null) {
                StringTokenizer st = new StringTokenizer(s, ":");
                W3Server virtualHost = w3server.virtualHosts.get(st.nextToken().trim().toLowerCase());
                if (virtualHost != null) w3context = virtualHost.w3context;
            }
            int questionMark = requestURI.indexOf('?');
            if (questionMark != -1) {
                queryString = requestURI.substring(questionMark + 1);
                requestURI = requestURI.substring(0, questionMark);
                if (!tried) store.requestURI = requestURI;
                W3URLConnection.parseQuery(queryString, queryValues, queryKeys);
                setParameters(queryParameters, queryValues);
            } else queryString = null;
            if (!store.keep && !mobile) checkLimits();
            int semicolon = requestPath.lastIndexOf(';');
            if (semicolon != -1 && requestPath.substring(semicolon + 1).startsWith("jsessionid="))
                requestPath = requestPath.substring(0, semicolon);
            String path;
            if ((path = normPath = W3URLConnection.normalize(requestPath)) == null)
                throw new W3Exception(HttpServletResponse.SC_BAD_REQUEST, "Invalid path " + requestPath);
            if (local) {
                store.requestURI = new URL(context, requestURI).getFile();
                requestURI = requestPath;
            }
            if (!tried) {
                removeAttribute(W3Request.class.getName() + "/virtualPath");
                // Checks if request is protected in domains
                String matchPath;
                if (local) {
                    if (File.separatorChar == '\\') path = path.toLowerCase();
                    matchPath = store.method.equals("PUT") ? ":" + path : path;
                } else {
                    matchPath = scheme + "://" + host + path.toLowerCase();
                    path = normPath = scheme + "://" + host + path;
                }
                if (checkDomain(matchPath)) break;
            }
            String actualPath = (String)w3server.virtualPaths.match(requestURI);
            if (!tried && actualPath != null) {
                setAttribute(W3Request.class.getName() + "/virtualPath", requestURI);
                requestPath = actualPath;
                // Replace [...] marks in path with query parameters
                HashMap<String,Integer> indexTable = new HashMap<String,Integer>();
                Iterator<String> keyIter = queryParameters.keySet().iterator();
                while (keyIter.hasNext()) indexTable.put(keyIter.next(), new Integer(0));
                StringBuffer sb = new StringBuffer();
                int i = 0, j, k = requestPath.indexOf('?');
                while ((j = requestPath.indexOf('[', i)) != -1) {
                    sb.append(requestPath.substring(i, j));
                    if ((i = requestPath.indexOf(']', ++j)) == -1) {
                        sender.sendError(HttpServletResponse.SC_NOT_FOUND, "Bad virtual path " + requestPath);
                        break;
                    }
                    String name = requestPath.substring(j, i), values[] = queryParameters.get(name);
                    if (values == null) values = queryParameters.get(name = "");
                    if (values != null) {
                        int index = indexTable.get(name).intValue();
                        if (index < values.length) {
                            s = values[index++];
                            if (j > k) sb.append(URLEncoder.encode(s, "8859_1"));
                            else sb.append(s);
                            indexTable.put(name, new Integer(index));
                        }
                    }
                    i++;
                }
                if (i > 0) requestPath = sb.toString() + requestPath.substring(i);
                sb.setLength(0);
                // Construct query string from remaining query parameters
                Iterator<String> iter = queryKeys.iterator();
                while (iter.hasNext()) {
                    String name = iter.next(), values[] = queryParameters.get(name);
                    int index = indexTable.get(name).intValue();
                    if (index < values.length) {
                        if (sb.length() > 0) sb.append('&');
                        if (!name.equals("")) sb.append(URLEncoder.encode(name, "8859_1")).append('=');
                        sb.append(URLEncoder.encode(values[index++], "8859_1"));
                        indexTable.put(name, new Integer(index));
                    }
                }
                if (sb.length() > 0) {
                    requestPath = requestPath.substring(0, k + 1) + sb.toString();
                }
                if (w3server.verbose) w3server.log("Constructed request path is " + requestPath);
                if (requestPath.startsWith("*")) {
                    requestPath = requestPath.substring(1);
                    if (!mobile) {
                        // Redirection is required
                        sender.sendRedirect(requestPath);
                        break;
                    }
                }
                if (!local || requestPath.indexOf(":/") != -1) {
                    // New request path
                    requestURI = requestPath;
                    continue;
                }
            }
            if (local) {
                boolean check = false;
                String cacheControl = getHeader("cache-control");
                if (cacheControl != null) {
                    if (Support.listContains(cacheControl, "max-age=0"))
                        check = true;
                }
                w3context = (W3Context)w3context.getContext(requestPath, check);
                if (w3context != W3Context.defaultW3context) {
                    int i = requestPath.indexOf('/', 1);
                    requestPath = i != -1 ? requestPath.substring(i) : "";
                }
                W3RequestDispatcher w3requestDispatcher = (W3RequestDispatcher)w3context.getRequestDispatcher(requestPath);
                if (w3requestDispatcher != null) {
                    w3servlet = w3requestDispatcher.w3servlet;
                    servletPath = w3requestDispatcher.servletPath;
                    pathInfo = w3requestDispatcher.pathInfo;
                    contextPath = w3requestDispatcher.contextPath;
                    if (!w3requestDispatcher.w3context.checkAuth(w3requestDispatcher, this, sender)) {
                        try {
                            w3context.requestInitialized(this);
                            w3requestDispatcher.invokeServlet(this, sender, DispatcherType.REQUEST);
                        } finally {
                            w3context.requestDestroyed(this);
                        }
                    }
                } else sender.sendError(HttpServletResponse.SC_NOT_FOUND, "Cannot find " + store.requestURI);
            } else {
                w3servlet = w3server.proxy;
                servletPath = "";
                pathInfo = null;
                setAttribute(ProxyServlet.class.getName() + "/connectionPool", connectionPool);
                w3servlet.servlet.service(this, sender);
                W3URLConnection uc = null;
                Response response = (Response)getAttribute(ProxyServlet.class.getName() + "/response");
                if (response != null) {
                    synchronized (responder) {
                        if (newResponse == null) {
                            newResponse = response;
                            responder.notifyAll();
                            store.response = response;
                        }
                    }
                    if (store.response == null) uc = response.uc;
                    else if (oldResponse != null) {
                        uc = oldResponse.uc;
                        oldResponse = response;
                    } else {
                        oldResponse = response;
                        return true;
                    }
                }
                if (uc != null) {
                    store.uc = uc;
                    synchronized (sender) {
                        requestHeaders.add(store);
                        sender.notifyAll();
                    }
                    return true;
                }
            }
            break;
        }
        complete();
        return false;
    }

    /** Ends request handling. */
    public void end(Store store)
        throws ServletException, IOException {
        if (w3server.debug) w3server.currentRequests.remove(W3Request.this.getName() + "-" + store.method + " " + store.uri);
        store.method = store.clientMethod;
        store.headerList = store.clientHeaderList;
        this.logStore = store;
        try {
            if (w3server.account != null)
                w3server.account.servlet.service(this, sender);
        } finally {
            endTime = System.currentTimeMillis();
            date = new Date(endTime);
            if (session != null) {
                session.lastAccessedTime = endTime;
                session.nBindings--;
            }
            if (w3server.logLevel > 2) w3server.log(this);
            requestNumber++;
        }
    }

    /** Initializes request to initial state. */
    public synchronized void initialize()
        throws SocketException {
        client = null;
        finished = false;
        mustClose = false;
        mustStop = false;
        protocol = W3URLConnection.protocol_1_0;
        remoteUser = null;
        remoteAddr = getRemoteAddr();
        requestNumber = 1;
        requestHeaders.clear();
        oldResponse = null;
        newResponse = null;
        filterLock.release();
        receiveLock.release();
        respondLock.release();
        responderLock.release();
        senderLock.release();
    }

    /** Handles exceptions occuring in request. */
    public void handle(Exception exception) {
        try {
            if (out !=  null && !sender.isCommitted()) {
                String msg = exception.toString();
                if (mobile) {
                    int i = msg.indexOf('\n');
                    if (i != -1) msg = msg.substring(0, i);
                }
                String location = w3context.getErrorPageByExceptionType(exception);
                if (location != null) {
                    setAttribute("javax.servlet.error.status_code", new Integer(HttpServletResponse.SC_INTERNAL_SERVER_ERROR));
                    setAttribute("javax.servlet.error.message", exception.getMessage());
                    setAttribute("javax.servlet.error.exception_type", exception.getClass());
                    setAttribute("javax.servlet.error.exception", exception);
                    setAttribute("javax.servlet.error.request_uri", store.requestURI);
                    setAttribute("javax.servlet.error.servlet_name", w3servlet.getServletName());
                    W3RequestDispatcher requestDispatcher = (W3RequestDispatcher)w3context.getRequestDispatcher(location);
                    try {
                        requestDispatcher.forward(this.w3requestFacade, this.w3responseFacade, DispatcherType.ERROR);
                        exception = null;
                    } catch (Exception ex) {
                        w3server.handle(ex);
                        if (exception != null)
                            w3server.handle(exception);
                    }
                }
                if (exception != null)
                    if (exception instanceof W3Exception) {
                        W3Exception w3ex = (W3Exception)exception;
                        if (w3ex.getStatus() == HttpServletResponse.SC_UNAUTHORIZED) sendUnauthorized();
                        else if (w3ex.getStatus() == HttpServletResponse.SC_NOT_MODIFIED) setNotModified();
                        else if (w3ex.getStatus() != HttpServletResponse.SC_OK) sender.sendError(w3ex.getStatus(), w3ex.getMessage());
                    } else if (exception instanceof LoginException) {
                        if (!committed) {
                            sender.reset();
                            sender.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            sender.setHeader("Www-Authenticate", challenge = "Basic domain=\"" + ((LoginException)exception).getDomain() + "\"");
                            sendUnauthorized();
                        }
                    } else if (exception instanceof FileNotFoundException) sender.sendError(HttpServletResponse.SC_NOT_FOUND, "Not Found " + msg);
                    else if (exception instanceof EOFException || exception instanceof InterruptedIOException) sender.sendError(HttpServletResponse.SC_GATEWAY_TIMEOUT, "Gateway timeout " + msg);
                    else if (exception instanceof UnavailableException || exception instanceof NoRouteToHostException) sender.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Service unavailable " + msg);
                    else {
                        if (!mobile) msg = Support.stackTrace(exception);
                        sender.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg);
                    }
                complete();
            } else store.keep = false;
        } catch (IOException ex) {
            store.keep = false;
        } finally {
            if (exception != null && !(exception instanceof W3Exception)) w3server.handle(exception);
        }
    }

    /** Finishes request. */
    public synchronized void finish() {
        if (finished) return;
        finished = true;
        try {
            if (socket != null) socket.close();
        } catch (IOException ex) {
            w3server.handle(ex);
        } finally {
            Iterator<W3URLConnection> connectionValues = connectionPool.values().iterator();
            while (connectionValues.hasNext()) {
                W3URLConnection uc = connectionValues.next();
                uc.uc.disconnect();
            }
            connectionPool.clear();
            try {
                if (ssl != null) ssl.close();
            } catch (IOException ex) {
                w3server.handle(ex);
            } finally {
                try {
                    if (filterOut != null) filterOut.close();
                } catch (IOException ex) {
                    w3server.handle(ex);
                } finally {
                    if (w3server.maxConnections != -1 && --client.connections == 0) w3server.clients.remove(remoteAddr);
                }
            }
        }
    }

    /** Handles coming HTTP-requests. */
    public void run() {
        try {
            if (w3server.logLevel > 1) w3server.log("Request " + getName() + " starting");
            sender.start();
            if (!ProxyServlet.disableProxyPipelining)
                responder.start();
            while (!isInterrupted())
                try {
                    if (w3server.socketChannels) {
                        channelSocket = (ChannelSocket)socket;
                        in = channelSocket.getInputStream();
                        out = channelSocket.getOutputStream();
                        socket = channelSocket.getSocket();
                    } else if (w3server.secure) {
                        // request is supposed to be secure
                        ssl.setSocket(socket);
                        ssl.accept();
                        out = ssl.getOutputStream();
                        in = ssl.getInputStream();
                    } else {
                        out = socket.getOutputStream();
                        in = socket.getInputStream();
                    }
                    initialize();
                    rawIn = in;
                    if (w3server.mobileServer) {
                        // request is supposed to be compressed
                        out = new DeflaterFlushOutputStream(out, new Deflater(Deflater.BEST_SPEED, true));
                        in = new InflaterFillInputStream(in, new Inflater(true));
                    } else out = new BufferedOutputStream(out);
                    plainIn = in = new BufferedInputStream(in);
                    sender.out = out;
                    receiveLock.lock();
                    respondLock.lock();
                    responderLock.lock();
                    senderLock.lock();
                    boolean connecting = false, receiving = false, keep = false, started = false;
                    do {
                        if (receiving) receiveLock.lock();
                        if (connecting && requester != null) {
                            // handling input-part of SSL-tunneling, reading from client and writing to remote server
                            keep = false;
                            byte buf[] = new byte[Support.bufferLength];
                            try {
                                for (int n; requester.isOpen() && (n = rawIn.read(buf)) > 0;) {
                                    requester.output.write(buf, 0, n);
                                    requester.output.flush();
                                    byteCountSent += n;
                                }
                            } catch (IOException ex) {
                                if (w3server.logLevel > 3) w3server.log(ex.toString(), ex);
                            }
                            break;
                        }
                        if (w3server.socketChannels && started) {
                            channelSocket.setSuspended(true);
                            break;
                        }
                        // waiting request from client
                        Store store = new Store();
                        if (HeaderList.parseHeaders(plainIn, store.headerList = new HeaderList(), true) == -1 || store.headerList.size() == 0) break;
                        if (w3server.verbose)
                            store.headerList.dump(System.out);
                        store.request = store.headerList.getFirst().getValue();
                        int i = store.request.indexOf(' ');
                        if (i != -1) {
                            store.method = store.request.substring(0, i).trim().toUpperCase();
                            int j = store.request.lastIndexOf(' ');
                            store.uri = j > i ? store.request.substring(i + 1, j).trim() : "";
                            if (!store.uri.equals("") && (store.protocol = store.request.substring(j + 1).trim()).startsWith("HTTP/")) store.mime = true;
                            else store.uri = store.request.substring(i + 1).trim();
                        }
                        boolean post = false;
                        connecting = false;
                        receiving = store.receiving = store.method != null && ((post = store.method.equals("POST")) || store.method.equals("PUT") || (connecting = store.method.equals("CONNECT")) || store.method.equals("PROPFIND")) || store.headerList.hasHeader("content-length");
                        String ct;
                        store.parsePostData = post && ((ct = store.headerList.getHeaderValue("content-type")) == null || (ct = Support.getParameters(ct, null)).toLowerCase().equals("application/x-www-form-urlencoded"));
                        String s;
                        store.simple = (s = store.headerList.getHeaderValue("connection")) != null;
                        store.keep = keep = (w3server.keepAliveCount == -1 || w3server.keepAliveCount > requestNumber) && (store.protocol.compareTo(W3URLConnection.protocol_1_1) >= 0 ? s == null || !s.equalsIgnoreCase("close") : (s != null || (s = store.headerList.getHeaderValue("proxy-connection")) != null) && s.equalsIgnoreCase("keep-alive"));
                        synchronized (sender) {
                            requestHeaders.add(store);
                            sender.notifyAll();
                        }
                        started = true;
                    } while (connecting || !mustClose && keep);
                } catch (Exception ex) {
                    if (ex instanceof InterruptedException) mustStop = true;
                    else if (!(ex instanceof SocketException)) w3server.handle(ex);
                    else if (w3server.logLevel > 3) w3server.log(ex.toString(), ex);
                } catch (Error er) {
                    w3server.handle(er);
                    throw er;
                } finally {
                    synchronized (sender) {
                        if (!requestHeaders.isEmpty()) {
                            Store store = requestHeaders.get(0);
                            requestHeaders.clear();
                            requestHeaders.add(store);
                        }
                        mustClose = true;
                        sender.notifyAll();
                    }
                    try {
                        senderLock.lock();
                    } catch (InterruptedException ex) {
                        mustClose = true;
                        mustStop = true;
                    }
                    if (mustStop) break;
                    if (w3server.logLevel > 1) w3server.log("Request " + getName() + " releasing");
                    try {
                        if (w3server.socketChannels) {
                            if (w3server.socketChannelServer.release(this)) break;
                        } else if (w3server.release(this)) break;
                    } catch (InterruptedException ex) {
                        break;
                    }
                    if (w3server.logLevel > 3) w3server.log("Request " + getName() + " resuming");
                }
        } finally {
            respondLock.release();
            responderLock.release();
            if (responder != null && responder.isAlive()) {
                responder.interrupt();
                try {
                    responder.join(1000L);
                } catch (InterruptedException ex) {}
            }
            if (sender != null && sender.isAlive()) {
                sender.interrupt();
                try {
                    sender.join(1000L);
                } catch (InterruptedException ex) {}
            }
            if (w3server.logLevel > 1) w3server.log("Request " + getName() + " stopping");
        }
    }

    protected void finalize() {
        mustStop = true;
        if (isAlive()) interrupt();
    }

}

