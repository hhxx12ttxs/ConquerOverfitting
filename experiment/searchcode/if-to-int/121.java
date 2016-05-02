/*
 * AdroitLogic UltraESB Enterprise Service Bus
 *
 * Copyright (c) 2010-2012 AdroitLogic Private Ltd. (http://adroitlogic.org). All Rights Reserved.
 *
 * GNU Affero General Public License Usage
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program (See LICENSE.AGPL).
 * If not, see http://www.gnu.org/licenses/agpl-3.0.html
 *
 * Commercial Usage
 *
 * Licensees holding valid UltraESB Commercial licenses may use this file in accordance with the UltraESB Commercial
 * License Agreement provided with the Software or, alternatively, in accordance with the terms contained in a written
 * agreement between you and AdroitLogic.
 *
 * If you are unsure which license is appropriate for your use, or have questions regarding the use of this file,
 * please contact AdroitLogic at info@adroitlogic.com
 */

package org.adroitlogic.ultraesb.transport.http.util;

import org.adroitlogic.ultraesb.core.ProxyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author asankha
 */
public class EnhancedURIPatternMatcher {

    private static final Logger logger = LoggerFactory.getLogger(EnhancedURIPatternMatcher.class);

    private final Map<String, Holder> map = new HashMap<String, Holder>();

    /**
     * Registers the Proxy Service for URI matching
     *
     * @param uri the URI of the service
     * @param pattern a regex pattern for the service e.g. "/service/accountDetails/{customerId:(\d*)}/account/{accountId:(\d*)}
     * @param ps the Proxy Service.
     */
    public synchronized void register(String uri, String pattern, final ProxyService ps) {
        if (uri == null) {
            throw new IllegalArgumentException("URI request pattern may not be null");
        }

        List<String> names = null;

        if (pattern != null) {
            names = new ArrayList<String>();
            StringTokenizer st = new StringTokenizer(pattern, "{");

            StringBuilder sb = new StringBuilder(128);
            int colon;
            int end;
            while (st.hasMoreTokens()) {
                String tok = st.nextToken();
                colon = tok.indexOf(':');
                end   = tok.indexOf('}');
                if (colon != -1 && end != -1) {
                    names.add(tok.substring(0, colon));
                    sb.append(tok.substring(colon + 1, end));
                    sb.append(tok.substring(end+1));
                } else {
                    sb.append(tok);
                }
            }

            pattern = sb.toString();
            logger.debug("Proxy : " + ps.getId() + " resulting regex pattern : " + pattern + " URI path params : ", names);
        }

        this.map.put(uri, new Holder(pattern == null ? null : Pattern.compile(pattern), names, ps));
    }

    /**
     * Removes registered object, if exists, for the given pattern.
     *
     * @param uri the pattern to un-register.
     */
    public synchronized void unRegister(final String uri) {
        if (uri == null) {
            return;
        }
        this.map.remove(uri);
    }

    /**
     * Looks up an object matching the given request URI.
     *
     * @param requestURI the request URI
     * @return MatchResult or <code>null</code> if no match is found.
     */
    public MatchResult lookup(String requestURI) {

        if (requestURI == null) {
            throw new IllegalArgumentException("Request URI may not be null");
        }

        // Strip away the query part part if found
        int index = requestURI.indexOf("?");
        if (index != -1) {
            requestURI = requestURI.substring(0, index);
        }

        Holder h = this.map.get(requestURI);
        if (h != null) {
            // direct match
            return new MatchResult(h.getProxy());

        } else {

            // simple URI pattern match?
            String bestMatch = null;
            for (Map.Entry<String, Holder> e : map.entrySet()) {
                if (e.getValue().getPattern() != null) {
                    // skip entries specified with full regex patterns
                    continue;
                }

                String pattern = e.getKey();
                if (matchUriRequestPattern(pattern, requestURI)) {
                    // we have a match. is it any better?
                    if (bestMatch == null
                        || (bestMatch.length() < pattern.length())
                        || (bestMatch.length() == pattern.length() && pattern.endsWith("*"))) {
                        h = e.getValue();
                        bestMatch = pattern;
                    }
                }
            }

            if (h != null) {
                return new MatchResult(h.getProxy());
            }

            Map<String, String> uriPathParams = new HashMap<String, String>();
            // full regex match with value extraction
            for (Map.Entry<String, Holder> e : map.entrySet()) {
                Pattern p  = e.getValue().getPattern();

                if (p != null) {
                    Matcher matcher = p.matcher(requestURI);
                    if (matcher.find()) {
                        int i = 1;
                        for (String name : e.getValue().getNames()) {
                            uriPathParams.put(name, matcher.group(i++));
                        }
                        return new MatchResult(e.getValue().getProxy(), uriPathParams);
                    }
                }
            }
        }
        return null;
    }

    private boolean matchUriRequestPattern(final String pattern, final String requestUri) {
        if (pattern.equals("*")) {
            return true;
        } else {
            return
            (pattern.endsWith("*") && requestUri.startsWith(pattern.substring(0, pattern.length() - 1))) ||
            (pattern.startsWith("*") && requestUri.endsWith(pattern.substring(1, pattern.length())));
        }
    }

    private static class Holder {
        private Pattern pattern;
        private List<String> names;
        private ProxyService proxy;

        private Holder(Pattern pattern, List<String> names, ProxyService ps) {
            this.pattern = pattern;
            this.names = names;
            this.proxy = ps;
        }

        public Pattern getPattern() {
            return pattern;
        }

        public List<String> getNames() {
            return names;
        }

        public ProxyService getProxy() {
            return proxy;
        }
    }

    public static class MatchResult {
        private final ProxyService proxy;
        private final Map<String, String> uriPathParms;

        MatchResult(ProxyService proxy, Map<String, String> uriPathParms) {
            this.proxy = proxy;
            this.uriPathParms = uriPathParms;
        }

        MatchResult(ProxyService proxy) {
            this.proxy = proxy;
            uriPathParms = null;
        }

        public ProxyService getProxy() {
            return proxy;
        }

        public Map<String, String> getUriPathParms() {
            return uriPathParms;
        }
    }

}

