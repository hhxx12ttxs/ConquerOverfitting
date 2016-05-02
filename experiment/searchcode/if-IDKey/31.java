/**
 * Copyright (c) 2010 Washington University
 */
package org.nrg.net;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.netbeans.spi.wizard.ResultProgressHandle;
import org.nrg.IOUtils;
import org.nrg.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Kevin A. Archie <karchie@wustl.edu>
 *
 */
public final class RestServer {
	private static final Pattern userInfoPattern = Pattern.compile("([^:@/]*):([^:@]*)");
	private static final String GET = "GET", PUT = "PUT", POST = "POST";
	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final Map<String,String> defaultHeaders = new HashMap<String,String>();
	static {
		defaultHeaders.put("User-Agent", "XNATUploadAssistant");
		defaultHeaders.put("Accept", "*/*");
	}
	
	private final Logger logger = LoggerFactory.getLogger(RestServer.class);

	private final URL base;
	
	public RestServer(final URL url) {
		final StringBuilder sb = new StringBuilder(url.toString());
		for (int i = sb.length() - 1; '/' == sb.charAt(i); i--) {
			sb.deleteCharAt(i);
		}
		try {
			this.base = new URL(sb.toString());
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);	// can't imagine how this would happen
		}
		
		final String userInfo = url.getUserInfo();
		if (null != userInfo) {
			final Matcher m = userInfoPattern.matcher(userInfo);
			if (m.matches()) {
				final Service service = new Service(url);
				final PasswordAuthentication auth = new PasswordAuthentication(m.group(1), m.group(2).toCharArray());
				passstore.put(service, auth);
			}
		}
	}
	
	public RestServer(final String url) throws MalformedURLException {
		this(new URL(url));
	}
	
	private final Map<Service,PasswordAuthentication> passstore = new HashMap<Service,PasswordAuthentication>();
	private final Map<Service,String> descriptions = new HashMap<Service,String>();

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
			result = 37*result + protocol.hashCode();
			result = 37*result + host.hashCode();
			result = 37*result + port;
			return result;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(final Object o) {
			if (! (o instanceof Service)) return false;
			final Service other = (Service)o;
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
				return new URL(protocol,host,port,"").toString();
			} catch (MalformedURLException e) {
				return MessageFormat.format("{0}://{1}:{2}",
						new Object[]{protocol, host, Integer.valueOf(port)});
			}
		}
	}

	
	/**
	 * Add authentication for the indicated service
	 * @param service URL for the service to be authenticated
	 * @param auth authentication
	 */
	public void addAuthentication(final URL service, final PasswordAuthentication auth) {
		passstore.put(new Service(service), auth);
	}

	static interface JSONDecoder {
		public void decode(JSONObject o) throws JSONException;
	}

	private static final class JSONValuesExtractor implements JSONDecoder {
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
			if (o.has(key)) c.add(o.get(key));
		}

		public Collection<Object> getValues() { return c; }
	}

	private static boolean isNullOrEmpty(final String s) { return null == s || "".equals(s); }

	private static final class JSONAliasesExtractor implements JSONDecoder {
		private final Map<String,String> m;
		private final String aliasKey, idKey;

		JSONAliasesExtractor(final Map<String,String> m, final String aliasKey, final String idKey) {
			this.m = m;
			this.aliasKey = aliasKey;
			this.idKey = idKey;
		}

		/*
		 * (non-Javadoc)
		 * @see org.nrg.net.RestOpManager.JSONDecoder#decode(org.json.JSONObject)
		 */
		public void decode(final JSONObject o) throws JSONException {
			final String alias = o.has(aliasKey) ? o.getString(aliasKey) : null;
			final String id = o.has(idKey) ? o.getString(idKey) : null;
			if (!isNullOrEmpty(alias)) {
				m.put(alias, isNullOrEmpty(id) ? alias : id);
			} else if (!isNullOrEmpty(id)) {
				m.put(id, id);
			}
		}

		public Map<String,String> getAliases() { return m; }
	}


	static JSONObject extractJSONEntity(final InputStream in)
	throws IOException,JSONException {
		return new JSONObject(new JSONTokener(new InputStreamReader(in)));
	}

	static JSONArray extractResultFromEntity(final JSONObject entity)
	throws JSONException {
		return entity.getJSONObject("ResultSet").getJSONArray("Result");
	}

	private static void addBasicAuthorizationToHeaderMap(final Map<String,String> m,
			final PasswordAuthentication auth) {
		final StringBuilder unenc = new StringBuilder();
		unenc.append(auth.getUserName());
		unenc.append(":");
		unenc.append(auth.getPassword());

		final StringBuilder enc = new StringBuilder("Basic ");
		enc.append(Base64.encode(unenc.toString()));
		m.put(AUTHORIZATION_HEADER, enc.toString());
	}


	private void doGet(final String path, final JSONDecoder decoder)
	throws IOException,JSONException {
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

	private void request(final String path, final String method,
			final HttpURLConnectionProcessor processor)
	throws Exception {
		final Map<String,String> headers = new LinkedHashMap<String,String>(defaultHeaders);
		final StringBuilder sb = new StringBuilder(base.toString());
		if ('/' != path.charAt(0)) {
			sb.append('/');
		}
		sb.append(path);
		final URL url = new URL(sb.toString());
		logger.trace(this + " preparing request " + url);
		if (logger.isTraceEnabled()) {
			final Callable<List<String>> callable = new Callable<List<String>>() {
				public List<String> call() throws IOException,URISyntaxException {
					final CookieHandler ch = CookieHandler.getDefault();
					final Map<String,List<String>> h = ch.get(url.toURI(), new HashMap<String, List<String>>());
					return h.get("Cookie");
				}
			};
			final ExecutorService es = Executors.newSingleThreadExecutor();
			try {
				final List<String> cookies = es.invokeAny(Collections.singleton(callable), 10, TimeUnit.SECONDS);
				logger.trace("session cookies: " + cookies);
			} catch (Exception e) {
				logger.error("Unable to query cookie store", e);
			}
		}
		
		final Service service = new Service(url);
		if (passstore.containsKey(service)) {
			addBasicAuthorizationToHeaderMap(headers, (PasswordAuthentication)passstore.get(service));
		}

		logger.trace("opening connection to " + url);
		final HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		connection.setRequestMethod(method);
		for (final Iterator<Map.Entry<String,String>> i = headers.entrySet().iterator(); i.hasNext(); ) {
			final Map.Entry<String,String> me = i.next();
			connection.setRequestProperty(me.getKey(), me.getValue());
		}
		processor.prepare(connection);

		TRY_PUT: for (;;) try {
			final int responseCode = connection.getResponseCode();
			switch (responseCode) {
			case HttpURLConnection.HTTP_OK:
				processor.process(connection);
				connection.disconnect();
				return;

			case HttpURLConnection.HTTP_UNAUTHORIZED:
				if (logger.isDebugEnabled()) {
					logger.debug("Received status code 401 (Unauthorized); headers:");
					for (final Map.Entry<String,List<String>> me: connection.getHeaderFields().entrySet()) {
						logger.trace("Header " + me.getKey() + ": " + me.getValue());
					}
				}
				final PasswordAuthentication auth;
				synchronized(passstore) {
					if (headers.containsKey(AUTHORIZATION_HEADER)) {
						passstore.remove(service);
					}
					final String prompt = "Enter credentials for " +
					(descriptions.containsKey(service) ? descriptions.get(service) : service);
					auth = Authenticator.requestPasswordAuthentication(url.getHost(),
							null, url.getPort(), url.getProtocol(),
							prompt, url.getProtocol());
					passstore.put(service, auth);
				}
				addBasicAuthorizationToHeaderMap(headers, auth);
				continue TRY_PUT;

			default:
				connection.disconnect();
				final InputStream errorStream = connection.getErrorStream();
				if (null != errorStream) {
					IOUtils.copy(System.out, errorStream);
				}
				throw new RemoteException(connection.getRequestMethod()
						+ " to " + url
						+ " returned " + responseCode + ": " + connection.getResponseMessage());
			}
		} finally {
			connection.disconnect();
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
	
	public void doPut(final String path, final HttpURLConnectionProcessor processor)
	throws Exception {
		request(path, PUT, processor);
	}
	
	
	/**
	 * Performs a POST request with empty entity.
	 * @param path
	 * @throws IOException
	 */
	public void doPost(final String path) throws IOException {
		request(path, POST);
	}

	/**
	 * Performs a PUT request with empty entity.
	 * @param path
	 * @throws IOException
	 */
	public void doPut(final String path) throws IOException {
		request(path, PUT);
	}

	public void doPut(final String path, final File f, final String mimeMediaType,
			final ResultProgressHandle progress) throws IOException {
		request(path, PUT, f, mimeMediaType, progress);
	}
	
	public Collection<Object> getValues(final String path, final String key)
	throws IOException,JSONException {
		final JSONValuesExtractor extractor = new JSONValuesExtractor(new LinkedHashSet<Object>(), key);
		doGet(path, extractor);
		return extractor.getValues();
	}

	public Map<String,String> getAliases(final String path, final String aliasKey, final String idKey)
	throws IOException,JSONException {
		final JSONAliasesExtractor extractor = new JSONAliasesExtractor(new LinkedHashMap<String,String>(), aliasKey, idKey);
		doGet(path, extractor);
		return extractor.getAliases();
	}

	public URL getURL() { return base; }
	
	public void setDescription(final URL url, final String description) {
		descriptions.put(new Service(url), description);
	}
}
