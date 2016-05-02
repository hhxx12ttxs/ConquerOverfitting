package com.github.mailerific.server;

import javax.servlet.http.HttpServletRequest;

public class UrlUtils {

    private UrlUtils() {
    }

    /**
     * a more lax method of decoding uri components. This method does not throw
     * exceptions, even for malformed URIs. Syntax errors are ignored.
     * 
     * @param uri
     *            the uri to be decoded
     * @return the decoded uri
     */
    public static String decode(final String uri) {
        if (uri == null) {
            return null;
        }
        if (uri.indexOf('%') < 0) {
            return uri;
        }
        final StringBuilder builder = new StringBuilder(uri);
        decodeUri(builder, 0, builder.length());
        return builder.toString();
    }

    public static String fullUrl(final HttpServletRequest request) {
        String url = request.getRequestURL().toString();
        String query = request.getQueryString();
        if (query != null) {
            url += "?" + query;
        }
        return url;
    }

    public static String[] splitPath(final String url) {
        int startIndex = 0;
        int endIndex;
        if (url.startsWith("/")) {
            startIndex = 1;
            endIndex = url.indexOf("/", 1);
        } else {
            endIndex = url.indexOf("/");
        }
        if (endIndex == -1) {
            endIndex = url.length();
        }
        String[] paths = new String[2];
        paths[0] = url.substring(startIndex, endIndex);
        if (endIndex == url.length()) {
            paths[1] = "";
        } else {
            paths[1] = url.substring(endIndex + 1);
        }
        return paths;
    }

    /**
     * generates the base URL of the request
     * 
     * @param request
     *            the request where the url is to be extracted
     * @return the base URL
     */
    public static String baseUrl(final HttpServletRequest request) {
        StringBuilder url = new StringBuilder(30);
        String protocol = "http://";
        if (request.isSecure()) {
            protocol = "https://";
        }

        url.append(protocol);
        url.append(request.getServerName());

        if (request.getServerPort() != 443) {
            url.append(':');
            url.append(request.getServerPort());
        }
        return url.toString();
    }

    private static void decodeUri(final StringBuilder uri, final int offset,
            final int length) {
        int index = offset; // so I have a prettier name
        int count = length; // me too!
        for (; count > 0; count--, index++) {
            final char ch = uri.charAt(index);
            if (ch != '%') {
                continue;
            }
            if (count < 3) {
                continue;
            }

            // Decode
            int dig1 = Character.digit(uri.charAt(index + 1), 16);
            int dig2 = Character.digit(uri.charAt(index + 2), 16);
            if (dig1 == -1 || dig2 == -1) {
                continue;
            }
            char value = (char) (dig1 << 4 | dig2);

            // Replace
            uri.setCharAt(index, value);
            uri.delete(index + 1, index + 3);
            count -= 2;
        }
    }

    /**
     * convenience method for appending a query string with a decoded path
     * 
     * @param path
     *            the decoded path
     * @param request
     *            request where the query string is pulled from
     * @return the whole string decoded
     */
    public static String appendPathWithQueryString(final String path,
            final HttpServletRequest request) {
        String newPath = path == null ? "" : path;
        String query = request.getQueryString();
        if (query != null) {
            newPath += "?" + UrlUtils.decode(query);
        }
        return newPath;
    }

}

