/*
 * org.nrg.net.RestServer
 * XNAT http://www.xnat.org
 * Copyright (c) 2013, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 12:19 PM
 */
package org.nrg.net;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.netbeans.spi.wizard.ResultProgressHandle;
import org.nrg.IOUtils;
import org.nrg.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.net.HttpURLConnection.*;

public class RestServer {
    private static final Pattern userInfoPattern = Pattern.compile("([^:@/]*):([^:@]*)");
    private static final String GET = "GET", PUT = "PUT", POST = "POST";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    final static String TITLE;
    final static String VERSION;

    static {
        final Properties props = new Properties();
        final ClassLoader cl = RestServer.class.getClassLoader();
        try {
            props.load(cl.getResourceAsStream("META-INF/application.properties"));
        } catch (Throwable t) {
            LoggerFactory.getLogger(RestServer.class).error("Unable to load properties", t);
        }
        TITLE = props.getProperty("application.name");
        VERSION = props.getProperty("application.version");
    }

    private static final Map<String, String> defaultHeaders = ImmutableMap.of(
            "User-Agent", "XNATUploadAssistant/" + VERSION,
            "Accept", "*/*");

    private final Logger logger = LoggerFactory.getLogger(RestServer.class);
    private final URL _base;
    private final JSESSIONIDCookie _jsessionidCookie;

    public RestServer(final URL url, final JSESSIONIDCookie jsessionidCookie) {

        final StringBuilder sb = new StringBuilder(url.toString());
        for (int i = sb.length() - 1; '/' == sb.charAt(i); i--) {
            sb.deleteCharAt(i);
        }
        try {
            _base = new URL(sb.toString());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);    // can't imagine how this would happen
        }

        final String userInfo = url.getUserInfo();
        if (null != userInfo) {
            final Matcher m = userInfoPattern.matcher(userInfo);
            if (m.matches()) {
                final Service service = new Service(url);
                final PasswordAuthentication auth = new PasswordAuthentication(m.group(1), m.group(2).toCharArray());
                passStore.put(service, auth);
            }
        }

        _jsessionidCookie = jsessionidCookie;
    }

    public RestServer(final String url, final JSESSIONIDCookie jsessionidCookie) throws MalformedURLException {
        this(new URL(url), jsessionidCookie);
    }

    private final Map<Service, PasswordAuthentication> passStore = Maps.newHashMap();
    private final Map<Service, String> descriptions = Maps.newHashMap();

    private static final class Service {
        private final String protocol;
        private final String host;
        private final int port;

        Service(final String protocol, final String host, final int port) {
            this.protocol = protocol;
            this.host = host;
            this.port = port;
        }

        Service(final URL url) {
            this(url.getProtocol(), url.getHost(), url.getPort());
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        public int hashCode() {
            int result = 17;
            result = 37 * result + protocol.hashCode();
            result = 37 * result + host.hashCode();
            result = 37 * result + port;
            return result;
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(final Object o) {
            if (!(o instanceof Service)) return false;
            final Service other = (Service) o;
            return protocol.equals(other.protocol) &&
                    host.equals(other.host) &&
                    port == other.port;
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString() {
            try {
                return new URL(protocol, host, port, "").toString();
            } catch (MalformedURLException e) {
                return MessageFormat.format("{0}://{1}:{2}", protocol, host, port);
            }
        }
    }

    /**
     * Get the logged in user from the server
     *
     * @return logged in user string
     * @throws IOException When error occurs during server I/O.
     */
    public String getUserAuthMessage() throws IOException {
        final StringResponseProcessor rp = new StringResponseProcessor();
        try {
            request("/data/auth", "GET", rp);
            return rp.toString();
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            final IOException ioe = new IOException();
            ioe.initCause(e);
            throw ioe;
        }
    }

    static interface JSONDecoder {
        void decode(JSONObject o) throws JSONException;
    }

    private static final class JSONValuesExtractor implements JSONDecoder {
        private final Logger logger = LoggerFactory.getLogger(JSONValuesExtractor.class);
        private final Collection<Object> c;
        private final String key;

        JSONValuesExtractor(final Collection<Object> c, final String key) {
            this.c = c;
            this.key = key;
        }

        /*
         * (non-Javadoc)
         * @see org.nrg.net.RestOpManager.JSONDecoder#decode(org.json.JSONObject)
         */
        public void decode(final JSONObject o) throws JSONException {
            logger.trace("decoding {} from {}", key, o);
            if (o.has(key)) {
                c.add(o.get(key));
            }
        }

        public Collection<Object> getValues() {
            return c;
        }
    }

    private static boolean isNullOrEmpty(final String s) {
        return null == s || "".equals(s);
    }

    private static final class JSONAliasesExtractor implements JSONDecoder {
        private final Logger logger = LoggerFactory.getLogger(JSONAliasesExtractor.class);
        private final Map<String, String> m;
        private final String aliasKey, idKey;

        JSONAliasesExtractor(final Map<String, String> m, final String aliasKey, final String idKey) {
            this.m = m;
            this.aliasKey = aliasKey;
            this.idKey = idKey;
        }

        /*
         * (non-Javadoc)
         * @see org.nrg.net.RestOpManager.JSONDecoder#decode(org.json.JSONObject)
         */
        public void decode(final JSONObject o) throws JSONException {
            logger.trace("decoding {} using {} -> {}", new Object[]{o, aliasKey, idKey});
            final String alias = o.has(aliasKey) ? o.getString(aliasKey) : null;
            final String id = o.has(idKey) ? o.getString(idKey) : null;
            if (!isNullOrEmpty(alias)) {
                m.put(alias, isNullOrEmpty(id) ? alias : id);
            } else if (!isNullOrEmpty(id)) {
                m.put(id, id);
            }
        }

        public Map<String, String> getAliases() {
            return m;
        }
    }


    static JSONObject extractJSONEntity(final InputStream in)
            throws IOException, JSONException {
        return new JSONObject(new JSONTokener(new InputStreamReader(in)));
    }

    static JSONArray extractResultFromEntity(final JSONObject entity)
            throws JSONException {
        return entity.getJSONObject("ResultSet").getJSONArray("Result");
    }

    private static String makeBasicAuthorization(final PasswordAuthentication auth) {
        final StringBuilder unencoded = new StringBuilder();
        unencoded.append(auth.getUserName());
        unencoded.append(":");
        unencoded.append(auth.getPassword());

        final StringBuilder enc = new StringBuilder("Basic ");
        enc.append(Base64.encode(unencoded.toString()));
        return enc.toString();
    }

    private static void addBasicAuthorizationToHeaderMap(final Map<String, String> m,
            final PasswordAuthentication auth) {
        m.put(AUTHORIZATION_HEADER, makeBasicAuthorization(auth));
    }

    private static void addBasicAuthorization(final URLConnection conn, final PasswordAuthentication auth) {
        conn.addRequestProperty(AUTHORIZATION_HEADER, makeBasicAuthorization(auth));
    }


    private void doGet(final String path, final JSONDecoder decoder)
            throws IOException, JSONException {
        try {
            doGet(path, new JSONResultExtractor(decoder));
        } catch (IOException e) {
            throw e;
        } catch (JSONException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void request(final String path, final String method, final HttpURLConnectionProcessor processor) throws Exception {

        final Map<String, String> headers = Maps.newLinkedHashMap(defaultHeaders);
        final StringBuilder sb = new StringBuilder(_base.toString());
        if ('/' != path.charAt(0)) {
            sb.append('/');
        }
        sb.append(path);
        final URL url = new URL(sb.toString());
        logger.trace("{} preparing request {}", this, url);
        if (logger.isTraceEnabled()) {
            final Callable<List<String>> callable = new Callable<List<String>>() {
                public List<String> call() throws IOException, URISyntaxException {
                    final CookieHandler ch = CookieHandler.getDefault();
                    final Map<String, List<String>> h = ch.get(url.toURI(), new HashMap<String, List<String>>());
                    return h.get("Cookie");
                }
            };
            final ExecutorService es = Executors.newSingleThreadExecutor();
            try {
                final List<String> cookies = es.invokeAny(Collections.singleton(callable), 10, TimeUnit.SECONDS);
                logger.trace("session cookies: {}", cookies);
            } catch (Exception e) {
                logger.error("Unable to query cookie store", e);
            }
        }

        final Service service = new Service(url);
        synchronized (passStore) {
            if (passStore.containsKey(service)) {
                addBasicAuthorizationToHeaderMap(headers, passStore.get(service));
            }
        }

        logger.trace("opening connection to {}", url);

        HttpURLConnection connection = null;
        int attempts = 0;
        TRY_PUT:
            for (; ; )
                try {
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod(method);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setUseCaches(false);
                    for (final Map.Entry<String, String> me : headers.entrySet()) {
                        if (!connection.getRequestProperties().containsKey(me.getKey())) {
                            connection.setRequestProperty(me.getKey(), me.getValue());
                        }
                    }
                    getJSESSIONIDCookie().setInRequestHeader(connection);
                    processor.prepare(connection);
                    final int responseCode = connection.getResponseCode();

                    switch (responseCode) {
                    case HTTP_ACCEPTED:
                    case HTTP_NOT_AUTHORITATIVE:
                    case HTTP_NO_CONTENT:
                    case HTTP_RESET:
                    case HTTP_PARTIAL:
                    case HTTP_MOVED_PERM:
                        logger.trace(connection.getRequestMethod() + " to {} returned "
                                + responseCode + " ({})",
                                url, connection.getResponseMessage());

                    case HTTP_OK:
                    case HTTP_CREATED:
                        processor.process(connection);
                        return;

                        // Handle 302, at least temporarily: Spring auth redirects to login page,
                        // so assume that's what's happened when we see a redirect at this point.
                    case HTTP_MOVED_TEMP:
                    case HTTP_UNAUTHORIZED:
                        if (logger.isDebugEnabled()) {
                            logger.debug("Received status code " + (responseCode == HTTP_MOVED_TEMP ? "302 (Redirect)" : "401 (Unauthorized)"));
                            for (final Map.Entry<String, List<String>> me : connection.getHeaderFields().entrySet()) {
                                logger.trace("Header {} : {}", me.getKey(), me.getValue());
                            }
                            logger.debug("Will request credentials for {}", url);
                        }
                        addBasicAuthorizationToHeaderMap(headers, getPasswordAuthentication(service, url));

                        if (attempts++ < 3) {
                            continue TRY_PUT;
                        }
                        throw new HttpException(responseCode, "Unable to connect to " + url.toString(), "Ugh");

                    case HTTP_CONFLICT:
                        throw new ConflictHttpException(getErrorEntity(connection));

                    default:
                        final StringBuilder message = new StringBuilder();
                        message.append(connection.getRequestMethod());
                        message.append(" to ").append(url).append(" failed ");
                        try {
                            final String entity = getErrorEntity(connection);
                            if (null != entity) {
                                message.append(": ").append(entity);
                            }
                        } catch (Throwable t) {
                            message.append(" - ").append(t.getMessage());
                        }
                        throw new HttpException(responseCode, connection.getResponseMessage(), message.toString());
                    }
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
    }

    private static String getErrorEntity(final HttpURLConnection connection) throws IOException {
        final InputStream errorStream = connection.getErrorStream();
        try {
            if (null == errorStream) {
                final ByteArrayOutputStream stream = new ByteArrayOutputStream();
                IOUtils.copy(stream, errorStream);
                if (stream.size() > 0) {
                    return stream.toString();
                }
            }
            return null;
        } finally {
            if (errorStream != null) {
                try {
                    errorStream.close();
                } catch (IOException ignored) {
                    // Just ignore this if it happens. This will allow exceptions from the top through and just not say
                    // anything if the close fails. Java gets unhappy about throwing exceptions from finally blocks.
                }
            }
        }
    }

    private void request(final String path, final String method) throws IOException {
        try {
            request(path, method, new EmptyRequest());
        } catch (IOException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void request(final String path, final String method,
            final InputStream in, final String mimeMediaType, final Integer contentLength,
            final ResultProgressHandle progress)
                    throws IOException {
        try {
            request(path, method, new StreamUploadProcessor(in, mimeMediaType, contentLength, progress));
        } catch (IOException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void request(final String path, final String method,
            final File f, final String mimeMediaType, final ResultProgressHandle progress)
                    throws IOException {
        request(path, method, new FileInputStream(f),
                mimeMediaType, Long.valueOf(f.length()).intValue(), progress);
    }

    public void doGet(final String path, final HttpURLConnectionProcessor processor)
            throws Exception {
        request(path, GET, processor);
    }

    public void doPost(final String path, final HttpURLConnectionProcessor processor)
            throws Exception {
        request(path, POST, processor);
    }

    public void doPut(final String path, final HttpURLConnectionProcessor processor) throws Exception {
        request(path, PUT, processor);
    }


    /**
     * Performs a POST request with empty entity.
     *
     * @param path Path to POST resource.
     * @throws IOException When error occurs during server I/O.
     */
    public void doPost(final String path) throws IOException {
        request(path, POST);
    }

    /**
     * Performs a PUT request with empty entity.
     *
     * @param path Path to PUT resource.
     * @throws IOException When error occurs during server I/O.
     */
    public void doPut(final String path) throws IOException {
        request(path, PUT);
    }

    public void doPut(final String path, final File f, final String mimeMediaType, final ResultProgressHandle progress) throws IOException {
        request(path, PUT, f, mimeMediaType, progress);
    }

    public Collection<Object> getValues(final String path, final String key)
            throws IOException, JSONException {
        final JSONValuesExtractor extractor = new JSONValuesExtractor(new LinkedHashSet<Object>(), key);
        doGet(path, extractor);
        return extractor.getValues();
    }

    public Map<String, String> getAliases(final String path, final String aliasKey, final String idKey) throws IOException, JSONException {
        final JSONAliasesExtractor extractor = new JSONAliasesExtractor(new LinkedHashMap<String, String>(), aliasKey, idKey);
        doGet(path, extractor);
        return extractor.getAliases();
    }

    public URL getURL() {
        return _base;
    }

    public void setDescription(final URL url, final String description) {
        descriptions.put(new Service(url), description);
    }

    public JSESSIONIDCookie getJSESSIONIDCookie() {
        return _jsessionidCookie;
    }

    private PasswordAuthentication getPasswordAuthentication(final Service service, final URL url) {
        synchronized (passStore) {
            PasswordAuthentication auth = passStore.get(service);
            if (null == auth) {
                final String prompt = "Enter credentials for " +
                        (descriptions.containsKey(service) ? descriptions.get(service) : service);
                auth = Authenticator.requestPasswordAuthentication(url.getHost(),
                        null, url.getPort(), url.getProtocol(),
                        prompt, url.getProtocol());
                passStore.put(service, auth);            
            }
            return auth;
        }
    }

    private PasswordAuthentication getPasswordAuthentication(final URL url) {
        return getPasswordAuthentication(new Service(url), url);
    }

    /**
     * Adds an authorization-equivalent header to the provided request: a JSESSION cookie if
     * that's available, or Authorization: Basic otherwise.
     * @param connection
     * @return connection
     */
    public URLConnection addAuthorization(final URLConnection connection) {
        if ("".equals(_jsessionidCookie.toString())) {
            addBasicAuthorization(connection, getPasswordAuthentication(connection.getURL()));
        } else {
            _jsessionidCookie.setInRequestHeader(connection);
        }
        return connection;
    }
}

