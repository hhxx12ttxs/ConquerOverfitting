/*
 * $Id$
 *
 * Copyright (c) 2012,2013 Runal House.
 * All Rights Reserved.
 */
package net.runal.jtool.net;

import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import net.runal.jtool.storage.StorageEntry;
import net.runal.jtool.util.CryptoAPI;
import net.runal.jtool.util.Period;

/**
 * Reference
 *   http://msdn.microsoft.com/en-us/library/windowsazure/dd179355.aspx
 * 
 * @author yasuo
 */
public class RestAPI {

    private static final Logger LOGGER = Logger.getLogger(RestAPI.class.getName());
    private static void log(Level level, String pattern, Object... args) {
        LOGGER.log(level, pattern, args);
    }
    private static Exception log(Level level, String msg, Throwable thrown) throws Error {
        LOGGER.log(level, msg, thrown);
        if(thrown instanceof Exception) {
            return (Exception)thrown;
        } else if(thrown instanceof Error) {
            throw (Error)thrown;
        } else {
            return new IllegalArgumentException(thrown);
        }
    }

////////////////////////////////////////////////////////////////////////////////

    public static class Constants {
        /**
         * Line Feed
         */
        public static final String LF = "\n";

        /**
        * An empty <code>String</code> to use for comparison.
        */
        public static final String EMPTY_STRING = "";

        /**
        * The master Windows Azure Storage header prefix.
        */
        public static final String PREFIX_FOR_STORAGE_HEADER = "x-ms-";

        /**
        * Defines constants for use with HTTP headers.
        */
        public static class HeaderConstants {
            /**
            * The Accept header.
            */
            public static final String ACCEPT = "Accept";

            /**
            * The Accept header.
            */
            public static final String ACCEPT_CHARSET = "Accept-Charset";

            /**
            * The Authorization header.
            */
            public static final String AUTHORIZATION = "Authorization";

            /**
            * The ContentEncoding header.
            */
            public static final String CONTENT_ENCODING = "Content-Encoding";

            /**
            * The ContentLangauge header.
            */
            public static final String CONTENT_LANGUAGE = "Content-Language";

            /**
            * The ContentLength header.
            */
            public static final String CONTENT_LENGTH = "Content-Length";

            /**
            * The ContentMD5 header.
            */
            public static final String CONTENT_MD5 = "Content-MD5";

            /**
            * The ContentType header.
            */
            public static final String CONTENT_TYPE = "Content-Type";

            /**
            * The header that specifies the date.
            */
            public static final String DATE = "Date";

            /**
            * The header to delete snapshots.
            */
            public static final String DELETE_SNAPSHOT_HEADER = PREFIX_FOR_STORAGE_HEADER + "delete-snapshots";

            /**
            * The IfMatch header.
            */
            public static final String IF_MATCH = "If-Match";

            /**
            * The IfModifiedSince header.
            */
            public static final String IF_MODIFIED_SINCE = "If-Modified-Since";

            /**
            * The IfNoneMatch header.
            */
            public static final String IF_NONE_MATCH = "If-None-Match";

            /**
            * The IfUnmodifiedSince header.
            */
            public static final String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";

            /**
            * The header that specifies lease ID.
            */
            public static final String LEASE_ID_HEADER = PREFIX_FOR_STORAGE_HEADER + "lease-id";

            /**
            * The header that specifies lease action.
            */
            public static final String LEASE_ACTION = PREFIX_FOR_STORAGE_HEADER + "lease-action";

            /**
            * The header that specifies lease duration.
            */
            public static final String LEASE_DURATION = PREFIX_FOR_STORAGE_HEADER + "lease-duration";

            /**
            * The header that specifies proposed lease ID.
            */
            public static final String PROPOSED_LEASE_ID = PREFIX_FOR_STORAGE_HEADER + "proposed-lease-id";

            /**
            * The Range header.
            */
            public static final String RANGE = "Range";

            /**
            * The header for storage version.
            */
            public static final String STORAGE_VERSION_HEADER = PREFIX_FOR_STORAGE_HEADER + "version";

            /**
            * The current storage version header value.
            */
            public static final String TARGET_STORAGE_VERSION = "2012-02-12";

            /**
            * The UserAgent header.
            */
            public static final String USER_AGENT = "User-Agent";

            /**
            * Specifies the value to use for UserAgent header.
            */
            public static final String USER_AGENT_PREFIX = "WA-Storage";

            /**
            * Specifies the value to use for UserAgent header.
            */
            public static final String USER_AGENT_VERSION = "Client v0.1.2";

            /**
            * The header that specifies the date.
            */
            public static final String X_MS_DATE = PREFIX_FOR_STORAGE_HEADER + "date";
        }
    }

    public static final class TableConstants {
        public static class HeaderConstants {
            public static final String ETAG = "ETag";
            public static final String ACCEPT_TYPE = "application/atom+xml,application/xml";
            public static final String ATOMPUB_TYPE = "application/atom+xml";
            public static final String MULTIPART_MIXED_FORMAT = "multipart/mixed; boundary=%s";
            public static final String DATA_SERVICE_VERSION = "DataServiceVersion";
            public static final String DATA_SERVICE_VERSION_VALUE = "1.0;NetFx";
            public static final String MAX_DATA_SERVICE_VERSION = "MaxDataServiceVersion";
            public static final String MAX_DATA_SERVICE_VERSION_VALUE = "2.0;NetFx";

            public HeaderConstants() {
            }
        }
        public static final int TABLE_DEFAULT_TIMEOUT_IN_MS = 60000;
        public static final String TABLE_SERVICE_PREFIX_FOR_TABLE_CONTINUATION = "x-ms-continuation-";
        public static final String TABLE_SERVICE_NEXT_PARTITION_KEY = "NextPartitionKey";
        public static final String TABLE_SERVICE_NEXT_ROW_KEY = "NextRowKey";
        public static final String TABLE_SERVICE_NEXT_MARKER = "NextMarker";
        public static final String TABLE_SERVICE_NEXT_TABLE_NAME = "NextTableName";
        public static final String PARTITION_KEY = "PartitionKey";
        public static final String ROW_KEY = "RowKey";
        public static final String TIMESTAMP = "Timestamp";
        public static final String TABLES_SERVICE_TABLES_NAME = "Tables";
        public static final String TABLE_NAME = "TableName";
        public static final String FILTER = "$filter";
        public static final String TOP = "$top";
        public static final String SELECT = "$select";

        private TableConstants() {
        }
    }
    
    
// Utility /////////////////////////////////////////////////////////////////////

    /**
     * Returns the GTM date/time for the specified value using the RFC1123 pattern.
     * 
     * @param inDate
     *            A <code>Date</code> object that represents the date to convert to GMT date/time in the RFC1123
     *            pattern.
     * 
     * @return A <code>String</code> that represents the GMT date/time for the specified value using the RFC1123
     *         pattern.
     */
    public static String getGMTTime(final Date inDate) {
        final DateFormat rfc1123Format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        rfc1123Format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return rfc1123Format.format(inDate);
    }

    /**
     * Returns the standard header value from the specified connection request, or an empty string if no header value
     * has been specified for the request.
     * 
     * @param conn
     *            An <code>HttpURLConnection</code> object that represents the request.
     * @param headerName
     *            A <code>String</code> that represents the name of the header being requested.
     * 
     * @return A <code>String</code> that represents the header value, or <code>null</code> if there is no corresponding
     *         header value for <code>headerName</code>.
     */
    public static String getStandardHeaderValue(final HttpURLConnection conn, final String headerName) {
        final String headerValue = conn.getRequestProperty(headerName);

        // Coalesce null value
        return headerValue == null ? Constants.EMPTY_STRING : headerValue;
    }

    /**
     * Trims whitespace from the beginning of a string.
     * 
     * @param value
     *            A <code>String</code> that represents the string to trim.
     * 
     * @return The string with whitespace trimmed from the beginning.
     */
    public static String trimStart(final String value) {
        int spaceDex = 0;
        while (spaceDex < value.length() && value.charAt(spaceDex) == ' ') {
            spaceDex++;
        }

        return value.substring(spaceDex);
    }
    
    /**
     * Performs safe decoding of the specified string, taking care to preserve each <code>+</code> character, rather
     * than replacing it with a space character.
     * 
     * @param stringToDecode
     *            A <code>String</code> that represents the string to decode.
     * 
     * @return A <code>String</code> that represents the decoded string.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public static String safeDecode(final String stringToDecode) throws UnsupportedEncodingException {
        if (stringToDecode == null) {
            return null;
        }

        if (stringToDecode.length() == 0) {
            return Constants.EMPTY_STRING;
        }

        try {
            if (stringToDecode.contains("+")) {
                final StringBuilder outBuilder = new StringBuilder();

                int startDex = 0;
                for (int m = 0; m < stringToDecode.length(); m++) {
                    if (stringToDecode.charAt(m) == '+') {
                        if (m > startDex) {
                            outBuilder.append(URLDecoder.decode(stringToDecode.substring(startDex, m), "UTF-8"));
                        }

                        outBuilder.append("+");
                        startDex = m + 1;
                    }
                }

                if (startDex != stringToDecode.length()) {
                    outBuilder.append(URLDecoder.decode(stringToDecode.substring(startDex, stringToDecode.length()),
                            "UTF-8"));
                }

                return outBuilder.toString();
            }
            else {
                return URLDecoder.decode(stringToDecode, "UTF-8");
            }
        }
        catch (final UnsupportedEncodingException e) {
            throw e;
        }
    }

    /**
     * Returns a value that indicates whether the specified string is <code>null</code> or empty.
     * 
     * @param value
     *            A <code>String</code> being examined for <code>null</code> or empty.
     * 
     * @return <code>true</code> if the specified value is <code>null</code> or empty; otherwise, <code>false</code>
     */
    public static boolean isNullOrEmpty(final String value) {
        return value == null || value.length() == 0;
    }

// Utility /////////////////////////////////////////////////////////////////////
// Path Utility ////////////////////////////////////////////////////////////////

    /**
     * Parses a query string into a one to many hashmap.
     * 
     * @param parseString
     *            the string to parse
     * @return a HashMap<String, String[]> of the key values.
     * @throws StorageException
     */
    public static HashMap<String, String[]> parseQueryString(String parseString) throws UnsupportedEncodingException {
        final HashMap<String, String[]> retVals = new HashMap<String, String[]>();
        if (isNullOrEmpty(parseString)) {
            return retVals;
        }

        // 1. Remove ? if present
        final int queryDex = parseString.indexOf("?");
        if (queryDex >= 0 && parseString.length() > 0) {
            parseString = parseString.substring(queryDex + 1);
        }

        // 2. split name value pairs by splitting on the 'c&' character
        final String[] valuePairs = parseString.contains("&") ? parseString.split("&") : parseString.split(";");

        // 3. for each field value pair parse into appropriate map entries
        for (int m = 0; m < valuePairs.length; m++) {
            final int equalDex = valuePairs[m].indexOf("=");

            if (equalDex < 0 || equalDex == valuePairs[m].length() - 1) {
                continue;
            }

            String key = valuePairs[m].substring(0, equalDex);
            String value = valuePairs[m].substring(equalDex + 1);

            key = safeDecode(key);
            value = safeDecode(value);

            // 3.1 add to map
            String[] values = retVals.get(key);

            if (values == null) {
                values = new String[] { value };
                if (!value.equals(Constants.EMPTY_STRING)) {
                    retVals.put(key, values);
                }
            }
            else if (!value.equals(Constants.EMPTY_STRING)) {
                final String[] newValues = new String[values.length + 1];
                System.arraycopy(values, 0, newValues, 0, values.length);
                newValues[newValues.length -1] = value;
            }
        }

        return retVals;
    }

// Path Utility ////////////////////////////////////////////////////////////////

    /**
     * Gets all the values for the given header in the one to many map, performs a trimStart() on each return value
     * 
     * @param headers
     *            a one to many map of key / values representing the header values for the connection.
     * @param headerName
     *            the name of the header to lookup
     * @return an ArrayList<String> of all trimmed values cooresponding to the requested headerName. This may be empty
     *         if the header is not found.
     */
    private static ArrayList<String> getHeaderValues(final Map<String, List<String>> headers, final String headerName) {

        final ArrayList<String> arrayOfValues = new ArrayList<String>();
        List<String> values = null;

        for (final Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (entry.getKey().toLowerCase(Locale.US).equals(headerName)) {
                values = entry.getValue();
                break;
            }
        }
        if (values != null) {
            for (final String value : values) {
                // canonicalization formula requires the string to be left
                // trimmed.
                arrayOfValues.add(trimStart(value));
            }
        }
        return arrayOfValues;
    }

    /**
     * Gets the canonicalized resource string for a Blob or Queue service request under the Shared Key Lite
     * authentication scheme.
     * 
     * @param address
     *            the resource URI.
     * @param accountName
     *            the account name for the request.
     * @return the canonicalized resource string.
     * @throws StorageException
     */
    private static String getCanonicalizedResource(final java.net.URL address, final String accountName) throws UnsupportedEncodingException {
        // Resource path
        final StringBuilder resourcepath = new StringBuilder("/");
        resourcepath.append(accountName);

        // Note that AbsolutePath starts with a '/'.
        resourcepath.append(address.getPath());
        final StringBuilder canonicalizedResource = new StringBuilder(resourcepath.toString());

        // query parameters
        final Map<String, String[]> queryVariables = parseQueryString(address.getQuery());

        final Map<String, String> lowercasedKeyNameValue = new HashMap<String, String>();

        for (final Map.Entry<String, String[]> entry : queryVariables.entrySet()) {
            // sort the value and organize it as comma separated values
            final List<String> sortedValues = Arrays.asList(entry.getValue());
            Collections.sort(sortedValues);

            final StringBuilder stringValue = new StringBuilder();

            for (final String value : sortedValues) {
                if (stringValue.length() > 0) {
                    stringValue.append(",");
                }

                stringValue.append(value);
            }

            // key turns out to be null for ?a&b&c&d
            lowercasedKeyNameValue.put(entry.getKey() == null ? null : entry.getKey().toLowerCase(Locale.US),
                    stringValue.toString());
        }

        final ArrayList<String> sortedKeys = new ArrayList<String>(lowercasedKeyNameValue.keySet());

        Collections.sort(sortedKeys);

        for (final String key : sortedKeys) {
            final StringBuilder queryParamString = new StringBuilder();

            queryParamString.append(key);
            queryParamString.append(":");
            queryParamString.append(lowercasedKeyNameValue.get(key));

            canonicalizedResource.append(Constants.LF);
            canonicalizedResource.append(queryParamString.toString());
        }

        return canonicalizedResource.toString();
    }

    static String canonicalizeHttpRequest(final java.net.URL address, final String accountName,
            final String method, final String contentType, final long contentLength, final String date,
            final HttpURLConnection conn/*, final OperationContext opContext*/) throws UnsupportedEncodingException {

        // The first element should be the Method of the request.
        // I.e. GET, POST, PUT, or HEAD.
        final StringBuilder canonicalizedString = new StringBuilder(conn.getRequestMethod());

        // The next elements are
        // If any element is missing it may be empty.
        
        canonicalizedString.append(Constants.LF);
        canonicalizedString.append(getStandardHeaderValue(conn, Constants.HeaderConstants.CONTENT_ENCODING));
        canonicalizedString.append(Constants.LF);
        canonicalizedString.append(getStandardHeaderValue(conn, Constants.HeaderConstants.CONTENT_LANGUAGE));
        canonicalizedString.append(Constants.LF);
        canonicalizedString.append(contentLength == -1 ? Constants.EMPTY_STRING : String.valueOf(contentLength));
        canonicalizedString.append(Constants.LF);
        canonicalizedString.append(getStandardHeaderValue(conn, Constants.HeaderConstants.CONTENT_MD5));
        canonicalizedString.append(Constants.LF);
        canonicalizedString.append(contentType != null ? contentType : Constants.EMPTY_STRING);

        final String dateString = getStandardHeaderValue(conn, Constants.HeaderConstants.X_MS_DATE);
        // If x-ms-date header exists, Date should be empty string
        canonicalizedString.append(Constants.LF);
        canonicalizedString.append(dateString.equals(Constants.EMPTY_STRING) ? date
                : Constants.EMPTY_STRING);

        String modifiedSinceString = Constants.EMPTY_STRING;
        if (conn.getIfModifiedSince() > 0) {
            modifiedSinceString = getGMTTime(new Date(conn.getIfModifiedSince()));
        }

        canonicalizedString.append(Constants.LF);
        canonicalizedString.append(modifiedSinceString);
        canonicalizedString.append(Constants.LF);
        canonicalizedString.append(getStandardHeaderValue(conn, Constants.HeaderConstants.IF_MATCH));
        canonicalizedString.append(Constants.LF);
        canonicalizedString.append(getStandardHeaderValue(conn, Constants.HeaderConstants.IF_NONE_MATCH));
        canonicalizedString.append(Constants.LF);
        canonicalizedString.append(getStandardHeaderValue(conn, Constants.HeaderConstants.IF_UNMODIFIED_SINCE));
        canonicalizedString.append(Constants.LF);
        canonicalizedString.append(getStandardHeaderValue(conn, Constants.HeaderConstants.RANGE));

        final Map<String, List<String>> headers = conn.getRequestProperties();
        final ArrayList<String> httpStorageHeaderNameArray = new ArrayList<String>();

        for (final String key : headers.keySet()) {
            if (key.toLowerCase(Locale.US).startsWith(Constants.PREFIX_FOR_STORAGE_HEADER)) {
                httpStorageHeaderNameArray.add(key.toLowerCase(Locale.US));
            }
        }

        Collections.sort(httpStorageHeaderNameArray);

        // Now go through each header's values in the sorted order and append
        // them to the canonicalized string.
        for (final String key : httpStorageHeaderNameArray) {
            final StringBuilder canonicalizedElement = new StringBuilder(key);
            String delimiter = ":";
            final ArrayList<String> values = getHeaderValues(headers, key);

            // Go through values, unfold them, and then append them to the
            // canonicalized element string.
            for (final String value : values) {
                // Unfolding is simply removal of CRLF.
                final String unfoldedValue = value.replace("\r\n", Constants.EMPTY_STRING);

                // Append it to the canonicalized element string.
                canonicalizedElement.append(delimiter);
                canonicalizedElement.append(unfoldedValue);
                delimiter = ",";
            }

            // Now, add this canonicalized element to the canonicalized header
            // string.
            canonicalizedString.append(Constants.LF);
            canonicalizedString.append(canonicalizedElement.toString());
        }
       
        
        
        canonicalizedString.append(Constants.LF);
        canonicalizedString.append(getCanonicalizedResource(address, accountName));

        return canonicalizedString.toString();
    }

////////////////////////////////////////////////////////////////////////////////

    static Response send(HttpURLConnection http, String authorization, InputStream content) throws IOException {
        // 認証
        if(authorization == null) {
            log(Level.WARNING, "authorization is null.");
        } else {
            http.setRequestProperty(Constants.HeaderConstants.AUTHORIZATION, authorization);
        }

        // リクエストボディ送信
        if(http.getDoOutput()) {
            OutputStream out = http.getOutputStream();
            try {
                InputStream in = content;
                if(in == null) {
                    log(Level.WARNING, "content is null.");
                } else {
                    byte[] buf = new byte[4096];
                    for(int read = in.read(buf); read != -1; read = in.read(buf)) {
                        out.write(buf, 0, read);
                    }
                }
            } finally {
                out.close();
            }
        }

        // レスポンス取得
        final Response response = new Response(http);
        log(Level.INFO, "Response: {0}", response);
        return response;
    }

////////////////////////////////////////////////////////////////////////////////

    public static Response requestSharedKey(
        final String requestUrl, 
        final String method, 
        final long contentLength, 
        final InputStream content, 

        final Credentials credentials, 
        final Map<String, String> additionalHeaders
        ) throws IOException {


        // ここから - REST APIによる通信処理 - ここから
        final Date date = new Date();

        URL url = new URL(requestUrl);
        URLConnection con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection) con;

        // メソッド設定
        http.setRequestMethod(method);
        // ストレージバージョン設定
        http.setRequestProperty(Constants.HeaderConstants.STORAGE_VERSION_HEADER,
            Constants.HeaderConstants.TARGET_STORAGE_VERSION);
        // 時刻設定
        http.setRequestProperty(Constants.HeaderConstants.X_MS_DATE, getGMTTime(date));

        // 追加のヘッダ
        if(additionalHeaders != null) {
            for(Map.Entry<String,String> header :additionalHeaders.entrySet()) {
                http.setRequestProperty(header.getKey(), header.getValue());
            }
        }

        if("PUT".equals(method)) {
            http.setDoOutput(true);
            // リクエストボディ長設定
            http.setRequestProperty(Constants.HeaderConstants.CONTENT_LENGTH, Long.toString(contentLength));
        }
        else if("GET".equals(method)) {
        } else {
            throw new IllegalArgumentException();
        }


        // 認証が必要な文字列
        final String accountName = credentials.getAccountName();
        final String accountKey = credentials.getAccountKey();
        final String stringToSign = canonicalizeHttpRequest(url, accountName, method, 
                getStandardHeaderValue(http, Constants.HeaderConstants.CONTENT_TYPE), contentLength, null, http);
        log(Level.INFO, "stringToSign: {0}", stringToSign);

        // Mac符号化
        final String computedBase64Signature = CryptoAPI.mac(accountKey, stringToSign);
        log(Level.INFO, "computedBase64Signature: {0}", computedBase64Signature);

        
        return send(http, String.format("%s %s:%s", "SharedKey", accountName, computedBase64Signature), content);
    }

////////////////////////////////////////////////////////////////////////////////

    /**
     * Gets the canonicalized resource string for a Blob or Queue service request under the Shared Key Lite
     * authentication scheme.
     * 
     * @param address
     *            the resource URI.
     * @param accountName
     *            the account name for the request.
     * @return the canonicalized resource string.
     * @throws StorageException
     */
    private static String getCanonicalizedResourceLite(final java.net.URL address, final String accountName) throws UnsupportedEncodingException {
        // Resource path
        final StringBuilder resourcepath = new StringBuilder("/");
        resourcepath.append(accountName);

        // Note that AbsolutePath starts with a '/'.
        resourcepath.append(address.getPath());
        final StringBuilder canonicalizedResource = new StringBuilder(resourcepath.toString());

        // query parameters
        final Map<String, String[]> queryVariables = parseQueryString(address.getQuery());

        final String[] compVals = queryVariables.get("comp");

        if (compVals != null) {

            final List<String> sortedValues = Arrays.asList(compVals);
            Collections.sort(sortedValues);

            canonicalizedResource.append("?comp=");

            final StringBuilder stringValue = new StringBuilder();
            for (final String value : sortedValues) {
                if (stringValue.length() > 0) {
                    stringValue.append(",");
                }
                stringValue.append(value);
            }

            canonicalizedResource.append(stringValue);
        }

        return canonicalizedResource.toString();
    }

    /**
     * http://d.hatena.ne.jp/waritohutsu/20100522/1274513671
     */
    private static Response requestSharedKeyLite(
        final String requestUrl, 
        final String method, 
        final long contentLength, 
        final InputStream content,

        final Credentials credentials,
        final Map<String,String> additionalHeaders
        ) throws IOException {

        
        // ここから - REST APIによる通信処理 - ここから
        final Date date = new Date();

        URL url = new URL(requestUrl);
        URLConnection con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection) con;

        // メソッド設定
        http.setRequestMethod(method);
        // ストレージバージョン設定
        http.setRequestProperty(Constants.HeaderConstants.STORAGE_VERSION_HEADER,
            Constants.HeaderConstants.TARGET_STORAGE_VERSION);

//        http.setRequestProperty(Constants.HeaderConstants.USER_AGENT, 
//            String.format("%s/%s", Constants.HeaderConstants.USER_AGENT_PREFIX,
//                Constants.HeaderConstants.USER_AGENT_VERSION));

        // Note : accept behavior, java by default sends Accept behavior
        // as text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2
        http.setRequestProperty(Constants.HeaderConstants.ACCEPT, TableConstants.HeaderConstants.ACCEPT_TYPE);
        http.setRequestProperty(Constants.HeaderConstants.ACCEPT_CHARSET, "UTF-8");

        //
        http.setRequestProperty(TableConstants.HeaderConstants.MAX_DATA_SERVICE_VERSION,
                TableConstants.HeaderConstants.MAX_DATA_SERVICE_VERSION_VALUE);

        http.setRequestProperty(Constants.HeaderConstants.CONTENT_TYPE,
                TableConstants.HeaderConstants.ATOMPUB_TYPE);

        String eTag = null;
        if (!isNullOrEmpty(eTag)) {
            http.setRequestProperty(Constants.HeaderConstants.IF_MATCH, eTag);
        }

        // 時刻設定
        http.setRequestProperty(Constants.HeaderConstants.X_MS_DATE, getGMTTime(date));
        
        if("PUT".equals(method)) {
            http.setDoOutput(true);
            // リクエストボディ長設定
            http.setRequestProperty(Constants.HeaderConstants.CONTENT_LENGTH, Long.toString(contentLength));
        } else if("GET".equals(method)) {
        } else {
            throw new IllegalArgumentException();
        }

        final String dateString = getStandardHeaderValue(http, Constants.HeaderConstants.X_MS_DATE);
        if (isNullOrEmpty(dateString)) {
            throw new IllegalArgumentException(
                    "Canonicalization did not find a non empty x-ms-date header in the request. Please use a request with a valid x-ms-date header in RFC 123 format.");
        }


        // 認証が必要な文字列
        final String accountName = credentials.getAccountName();
        final String accountKey = credentials.getAccountKey();

        final StringBuilder canonicalizedString = new StringBuilder(dateString);
        canonicalizedString.append(Constants.LF);
        canonicalizedString.append(getCanonicalizedResourceLite(http.getURL(), accountName));
        final String stringToSign = canonicalizedString.toString();
        log(Level.INFO, "stringToSign: {0}", stringToSign);


        // Mac符号化
        final String computedBase64Signature = CryptoAPI.mac(accountKey, stringToSign);
        log(Level.INFO, "computedBase64Signature: {0}", computedBase64Signature);

        return send(http, String.format("%s %s:%s", "SharedKeyLite", accountName, computedBase64Signature), content);
    }

////////////////////////////////////////////////////////////////////////////////

    public static boolean isValidAuthorization(HttpServletRequest request) throws MalformedURLException, UnsupportedEncodingException {
        // authorizationの検証
        String authorization = request.getHeader("Authorization");
        log(Level.INFO, "Authorization: {0}", authorization);

        // 形式の確認
        final String type;
        final String accountName;
        final String base64Signature;
        final Matcher m = Pattern.compile("(SharedKey(Lite)?) (\\w+):([A-Za-z0-9+/=]+)").matcher(authorization);
        if(m.matches()) {
            type = m.group(1);
            accountName = m.group(3);
            base64Signature = m.group(4);
        } else {
            type = null;
            accountName = null;
            base64Signature = null;
        }

        final String requestURL = request.getRequestURL().toString();
        final String queryString = request.getQueryString();
        final URL url;
        if(queryString == null) {
            url = new URL(requestURL);
        } else {
            url = new URL(requestURL +"?" +queryString);
        }
        final boolean isTable = Pattern.matches("https?://.+\\.table\\.core\\..+", requestURL);
        
        final ArrayList<String> headers = Collections.list(request.getHeaderNames());
        final Set<String> httpStorageHeaderNameSet = new TreeSet<String>();
        for (final String key : headers) {
            final String lowerCase = key.toLowerCase(Locale.US);
            if (lowerCase.startsWith(Constants.PREFIX_FOR_STORAGE_HEADER)) {
                httpStorageHeaderNameSet.add(lowerCase);
            }
        }

        // 署名情報の生成
        final StringBuilder canonicalizedString;
        if("SharedKeyLite".equals(type)) {
            if(isTable) {
                // Tableの場合
                final String dateString = request.getHeader(Constants.HeaderConstants.DATE);
                canonicalizedString = new StringBuilder(dateString);
            } else {
                // Blob|Queueの場合
                canonicalizedString = new StringBuilder(request.getMethod());

                canonicalizedString.append(Constants.LF);
                final String contentMD5 = request.getHeader(Constants.HeaderConstants.CONTENT_MD5);
                if(contentMD5 == null) {
                    canonicalizedString.append(Constants.EMPTY_STRING);
                } else {
                    canonicalizedString.append(contentMD5);
                }
                canonicalizedString.append(Constants.LF);
                final String contentType = request.getContentType();
                if(contentType == null) {
                    canonicalizedString.append(Constants.EMPTY_STRING);
                } else {
                    canonicalizedString.append(contentType);
                }
                canonicalizedString.append(Constants.LF);
                if(httpStorageHeaderNameSet.contains(Constants.HeaderConstants.X_MS_DATE)) {
                    canonicalizedString.append(Constants.EMPTY_STRING);
                } else {
                    canonicalizedString.append(request.getHeader(Constants.HeaderConstants.DATE));
                }

                for (final String key : httpStorageHeaderNameSet) {
                    final StringBuilder canonicalizedElement = new StringBuilder(key);
                    String delimiter = ":";
                    final List<String> values = Collections.list(request.getHeaders(key));

                    for (final String value : values) {
                        final String unfoldedValue = value.replace("\r\n", Constants.EMPTY_STRING);

                        canonicalizedElement.append(delimiter);
                        canonicalizedElement.append(unfoldedValue);
                        delimiter = ",";
                    }

                    canonicalizedString.append(Constants.LF);
                    canonicalizedString.append(canonicalizedElement.toString());
                }
            }
            canonicalizedString.append(Constants.LF);
            canonicalizedString.append(getCanonicalizedResourceLite(url, accountName));
        } else if("SharedKey".equals(type)) {
            if(isTable) {
                // Tableの場合
                canonicalizedString = new StringBuilder(request.getMethod());

                canonicalizedString.append(Constants.LF);
                final String contentMD5 = request.getHeader(Constants.HeaderConstants.CONTENT_MD5);
                if(contentMD5 == null) {
                    canonicalizedString.append(Constants.EMPTY_STRING);
                } else {
                    canonicalizedString.append(contentMD5);
                }
                canonicalizedString.append(Constants.LF);
                final String contentType = request.getContentType();
                if(contentType == null) {
                    canonicalizedString.append(Constants.EMPTY_STRING);
                } else {
                    canonicalizedString.append(contentType);
                }
                canonicalizedString.append(Constants.LF);
                if(httpStorageHeaderNameSet.contains(Constants.HeaderConstants.X_MS_DATE)) {
                    canonicalizedString.append(Constants.EMPTY_STRING);
                } else {
                    canonicalizedString.append(request.getHeader(Constants.HeaderConstants.DATE));
                }
            } else {
                // Blob|Queueの場合
                canonicalizedString = new StringBuilder(request.getMethod());

                canonicalizedString.append(Constants.LF);
                final String characterEncoding = request.getHeader(Constants.HeaderConstants.CONTENT_ENCODING);
                if(characterEncoding == null) {
                    canonicalizedString.append(Constants.EMPTY_STRING);
                } else {
                    canonicalizedString.append(characterEncoding);
                }
                canonicalizedString.append(Constants.LF);
                final String contentLanguage = request.getHeader(Constants.HeaderConstants.CONTENT_LANGUAGE);
                if(contentLanguage == null) {
                    canonicalizedString.append(Constants.EMPTY_STRING);
                } else {
                    canonicalizedString.append(contentLanguage);
                }
                canonicalizedString.append(Constants.LF);
                final String contentLength = request.getHeader(Constants.HeaderConstants.CONTENT_LENGTH);
                if(contentLength == null) {
                    canonicalizedString.append(Constants.EMPTY_STRING);
                } else {
                    canonicalizedString.append(contentLength);
                }

                canonicalizedString.append(Constants.LF);
                final String contentMD5 = request.getHeader(Constants.HeaderConstants.CONTENT_MD5);
                if(contentMD5 == null) {
                    canonicalizedString.append(Constants.EMPTY_STRING);
                } else {
                    canonicalizedString.append(contentMD5);
                }
                canonicalizedString.append(Constants.LF);
                final String contentType = request.getContentType();
                if(contentType == null) {
                    canonicalizedString.append(Constants.EMPTY_STRING);
                } else {
                    canonicalizedString.append(contentType);
                }
                canonicalizedString.append(Constants.LF);
                if(httpStorageHeaderNameSet.contains(Constants.HeaderConstants.X_MS_DATE)) {
                    canonicalizedString.append(Constants.EMPTY_STRING);
                } else {
                    canonicalizedString.append(request.getHeader(Constants.HeaderConstants.DATE));
                }

                canonicalizedString.append(Constants.LF);
                String modifiedSinceString = request.getHeader(Constants.HeaderConstants.IF_MODIFIED_SINCE);
                if (modifiedSinceString == null) {
                    canonicalizedString.append(Constants.EMPTY_STRING);
                } else {
                    canonicalizedString.append(modifiedSinceString);
                }
                canonicalizedString.append(Constants.LF);
                final String ifMatch = request.getHeader(Constants.HeaderConstants.IF_MATCH);
                if (ifMatch == null) {
                    canonicalizedString.append(Constants.EMPTY_STRING);
                } else {
                    canonicalizedString.append(ifMatch);
                }
                canonicalizedString.append(Constants.LF);
                final String ifNoneMatch = request.getHeader(Constants.HeaderConstants.IF_NONE_MATCH);
                if (ifNoneMatch == null) {
                    canonicalizedString.append(Constants.EMPTY_STRING);
                } else {
                    canonicalizedString.append(ifNoneMatch);
                }
                canonicalizedString.append(Constants.LF);
                final String ifUnmodifiedSince = request.getHeader(Constants.HeaderConstants.IF_UNMODIFIED_SINCE);
                if (ifUnmodifiedSince == null) {
                    canonicalizedString.append(Constants.EMPTY_STRING);
                } else {
                    canonicalizedString.append(ifUnmodifiedSince);
                }
                canonicalizedString.append(Constants.LF);
                final String range = request.getHeader(Constants.HeaderConstants.RANGE);
                if (range == null) {
                    canonicalizedString.append(Constants.EMPTY_STRING);
                } else {
                    canonicalizedString.append(range);
                }

                for (final String key : httpStorageHeaderNameSet) {
                    final StringBuilder canonicalizedElement = new StringBuilder(key);
                    String delimiter = ":";
                    final List<String> values = Collections.list(request.getHeaders(key));

                    for (final String value : values) {
                        final String unfoldedValue = value.replace("\r\n", Constants.EMPTY_STRING);

                        canonicalizedElement.append(delimiter);
                        canonicalizedElement.append(unfoldedValue);
                        delimiter = ",";
                    }

                    canonicalizedString.append(Constants.LF);
                    canonicalizedString.append(canonicalizedElement.toString());
                }
            }
            canonicalizedString.append(Constants.LF);
            canonicalizedString.append(getCanonicalizedResource(url, accountName));
        } else {
            return false;
        }
        final String stringToSign = canonicalizedString.toString();
        log(Level.INFO, "stringToSign: {0}", stringToSign);

        // Mac符号化
        final String computedBase64Signature = CryptoAPI.mac(stringToSign);
        log(Level.INFO, "computedBase64Signature: {0}", computedBase64Signature);

        // 比較
        return base64Signature == null || !base64Signature.equals(computedBase64Signature);
    }

////////////////////////////////////////////////////////////////////////////////

    private static final long MINUTE_UNIT = (1L * 60L * 1000L);
    public static String sas(Credentials credentials, String blobEndpoint, String containerName, String blobName, Period period, String si) throws UnsupportedEncodingException, URISyntaxException {
        final String accountName = credentials.getAccountName();
        final String accountKey = credentials.getAccountKey();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        Date start;
        Date expiry;
        if(period == null || !period.isValid()) {
            long minute; {
                long time = System.currentTimeMillis();
                if(time == 0) {
                    minute = 0;
                } else {
                    minute = time / MINUTE_UNIT;
                }
            }
            start = new Date((minute -1) * MINUTE_UNIT);
            expiry = new Date(start.getTime() +( 2 * MINUTE_UNIT));
        } else {
            start = period.from();
            expiry = period.to();
        }

        // Signed identifier
        final String identifier;
        if(si == null) {
            identifier = Constants.EMPTY_STRING;
        } else {
            identifier = si;
        }

        // Signed version
        final String version = "2012-02-12";
        // Signed resource
        final String resource = "b";
        // Signed permissions
        final String permissions = "r";


        String stringToSign = String.format("%s\n%s\n%s\n%s\n%s\n%s", permissions, sdf.format(start), sdf.format(expiry),
                "/" +accountName +"/" +containerName +"/" +blobName, identifier, version);


        String sig = URLEncoder.encode(CryptoAPI.mac(accountKey, stringToSign), "UTF-8");


        StringBuilder sb = new StringBuilder();
        sb.append("sp=").append(permissions);
        sb.append("&sr=").append(resource);
        sb.append("&sv=").append(version);
        sb.append("&se=");
        sb.append(URLEncoder.encode(sdf.format(expiry), "UTF-8"));
        sb.append("&st=");
        sb.append(URLEncoder.encode(sdf.format(start), "UTF-8"));
        sb.append("&sig=".concat(sig));
        if(!isNullOrEmpty(identifier)) {
            sb.append("&si=").append(identifier);
        }
        log(Level.INFO, "sas: {0}", sb);

        return sb.toString();
    }

    public static class Storage {
        final Credentials credentials;
        final String blobEndpoint;
        public Storage(String aBlobEndpoint, Credentials aCredentials) {
            blobEndpoint = aBlobEndpoint;
            credentials = aCredentials;
        }
    }

    public static Response copy(Storage dst, String srcUrl, Credentials credentials) throws Exception {
        URL url = new URL(srcUrl);
        String path = url.getPath();
        String query = url.getQuery();
        Matcher m = Pattern.compile(Pattern.quote(path) + "(.*)$").matcher(srcUrl);
        StringBuffer source = new StringBuffer();
        while(m.find()) {
            m.appendReplacement(source, "");
        }
        m.appendTail(source);
        StorageEntry entry;
        if(path.startsWith("/")) {
            entry = new StorageEntry(path.substring(1));
        } else {
            entry = new StorageEntry(path);
        }

        String containerName = entry.getContainerName();
        String blobName = entry.getBlobName();
        String sas = sas(credentials, source.toString(), containerName, blobName, null, null);

        source.append("/");
        source.append(containerName);
        source.append("/");
        source.append(blobName);
        source.append("?");
        if(query == null) {
        } else {
            source.append(query);
            source.append("&");
        }

        source.append(sas);



        final StringBuilder requestURI = new StringBuilder(dst.blobEndpoint);
        requestURI.append(containerName);
        requestURI.append("/");
        requestURI.append(blobName);
        

        final String method = "PUT";
        final String contentType = null;
        final long contentLength = 0;


        final URI uri = new URI(requestURI.toString());
        final Date date = new Date();

        URLConnection con = uri.toURL().openConnection();
        HttpURLConnection http = (HttpURLConnection) con;

        // メソッド設定
        http.setRequestMethod(method);
        // コンテントタイプ
        if(contentType != null) {
            http.setRequestProperty("Content-Type", contentType);
        }
        if("PUT".equals(http.getRequestMethod())) {
            http.setDoOutput(true);
            // リクエストボディ長設定
            http.setRequestProperty("Content-Length", Long.toString(contentLength));
        }


        // ストレージバージョン設定
        http.setRequestProperty("x-ms-version", "2012-02-12");
        // 時刻設定
        http.setRequestProperty("x-ms-date", RestAPI.getGMTTime(date));
        //
        http.setRequestProperty("x-ms-copy-source", source.toString());

        // 認証が必要な文字列
        final String stringToSign = RestAPI.canonicalizeHttpRequest(http.getURL(), dst.credentials.getAccountName(), method, 
                contentType, contentLength, null, http);

        // 認証設定
        final String authorization = String.format("%s %s:%s", "SharedKey", dst.credentials.getAccountName(), CryptoAPI.mac(dst.credentials.getAccountKey(), stringToSign));
        final Response response = RestAPI.send(http, authorization, null);
        return response;
    }

    public static Response list(final RestAPI.Storage storage, final String containerName, final String query) throws URISyntaxException, MalformedURLException, IOException {
        final String method = "GET";
        final String contentType = null;
        final long contentLength = -1;

        // ここから - REST APIによる通信処理 - ここから
        final StringBuilder sb = new StringBuilder(storage.blobEndpoint);
        if(!RestAPI.isNullOrEmpty(containerName)) {
            sb.append(containerName);
        }
        sb.append("?");
        sb.append(query);
        final URI uri = new URI(sb.toString());
        final Date date = new Date();

        URL url = uri.toURL();
        URLConnection con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection) con;

        // メソッド設定
        http.setRequestMethod(method);
        // コンテントタイプ
        if(contentType != null) {
            http.setRequestProperty("Content-Type", contentType);
        }

        // ストレージバージョン設定
        http.setRequestProperty("x-ms-version", "2012-02-12");
        // 時刻設定
        http.setRequestProperty("x-ms-date", RestAPI.getGMTTime(date));


        // 認証が必要な文字列
        final String stringToSign = RestAPI.canonicalizeHttpRequest(url, storage.credentials.getAccountName(), method, 
                contentType, contentLength, null, http);

        // 認証設定
        final String authorization = String.format("%s %s:%s", "SharedKey", storage.credentials.getAccountName(), CryptoAPI.mac(storage.credentials.getAccountKey(), stringToSign));
        final Response response = RestAPI.send(http, authorization, null);
        return response;
    }

}

