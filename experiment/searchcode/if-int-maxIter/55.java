/**
 * Copyright (C) 2010 Peter Karich <jetwick_@_pannous_._info>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.jetwick.tw;

import de.jetwick.util.Helper;
import de.jetwick.data.UrlEntry;
import de.jetwick.data.JTweet;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class extracts links, users and hashtags of one tweet.
 *
 * Used for UI to render links, users and hashtags but also for indexing
 * to detect users in retweets.
 *
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class Extractor {

    private Logger logger = LoggerFactory.getLogger(Extractor.class);
    protected JTweet tweet;
    protected String text;
    protected Map<Integer, UrlEntry> urlMap = new LinkedHashMap<Integer, UrlEntry>(3);
    protected StringBuilder sb;

    public Extractor setTweet(JTweet tweet) {
        this.tweet = tweet;
        Collection<UrlEntry> coll = tweet.getUrlEntries();
        urlMap.clear();
        for (UrlEntry e : coll) {
            urlMap.put(e.getIndex(), e);
        }
        return setText(tweet.getText());
    }

    public Extractor setText(String text) {
        this.text = text;
        return this;
    }

    /**
     *
     * @deprecated use setText(str).run().toString instead
     */
    public String toSaveHtml(String str) {
        return setText(str).run().toString();
    }

    public Extractor run() {
        if (text == null)
            throw new NullPointerException("before usage set text via setText or indirectly via setTweet!");

        sb = new StringBuilder();
        int newLineCounter = 0;
        for (int index = 0; index < text.length(); index++) {
            if (text.charAt(index) == '@') {
                // if @ is NOT at the beginning or if it could be part of an email:
                if (index == 0 || index > 0 && !Character.isJavaIdentifierPart(text.charAt(index - 1))) {
                    int lastIndex = -1;
                    for (int i = index + 1; i < text.length(); i++) {
                        char c = text.charAt(i);
                        if (!Character.isJavaIdentifierPart(c)) {
                            lastIndex = i;
                            break;
                        }
                    }
                    if (lastIndex < 0)
                        lastIndex = text.length();

                    // preserve probably existing camel case (no toLowerCase)
                    String user = text.substring(index + 1, lastIndex).trim();
                    if (user.length() > 0) {
                        if (onNewUser(index, user)) {
                            index = lastIndex - 1;
                            continue;
                        }
                    }
                }
            } else if (text.charAt(index) == '#') {
                // if # is NOT at the beginning or if it could be part of an http
                if (index == 0 || index > 0 && !Character.isJavaIdentifierPart(text.charAt(index - 1))) {
                    int lastIndex = text.indexOf(" ", index + 1);
                    if (lastIndex < 0)
                        lastIndex = text.length();
                    String link = text.substring(index + 1, lastIndex).trim();
                    if (link.length() > 0) {
                        if (onNewHashTag(index, link)) {
                            index = lastIndex - 1;
                            continue;
                        }
                    }
                }
            } else if (text.charAt(index) == '\n') {
                newLineCounter++;
                // do not allow too 'high' tweets:
                if (newLineCounter < 6)
                    sb.append("<br/>");

                continue;
            } else {
                int lastIndex = onNewRawUrl(index, sb);
                if (lastIndex > 0) {
                    index = lastIndex - 1;
                    continue;
                }
            }

            // TODO allow bolding
            sb.append(Strings.escapeMarkup("" + text.charAt(index)));
        }

        return this;
    }

    @Override
    public String toString() {
        if (sb == null)
            return "";

        return sb.toString();
    }

    public boolean onNewHashTag(int index, String tag) {
        try {
            tag = "#" + tag;
            String cleanTag = Helper.stripOutLuceneHighlighting(tag);
            cleanTag = URLEncoder.encode(cleanTag, Helper.UTF8);
            String newLink = createTagMarkup(tag, cleanTag);
            sb.append(newLink);

            return true;
        } catch (Exception ex) {
            logger.warn("Cannot create link for " + tag, ex);
        }
        return false;
    }

    public String createTagMarkup(String tag, String cleanTag) {
        return Helper.toJetwickSearch(tag, cleanTag);
    }

    public boolean onNewUser(int index, String user) {
        try {
            user = "@" + user;
            String cleanUserName = Helper.stripOutLuceneHighlighting(user);
            cleanUserName = URLEncoder.encode(cleanUserName, Helper.UTF8);
            String newUser = Helper.toJetwickUser(user, cleanUserName);
            sb.append(newUser);
            return true;
        } catch (Exception ex) {
            logger.warn("Cannot create link for " + user, ex);
        }
        return false;
    }

    public int onNewRawUrl(int index, StringBuilder tmpSb) {
        String tmpStr = text.substring(index);
        int minLength = 0;
        if (tmpStr.startsWith("http://"))
            minLength = 7;
        else if (tmpStr.startsWith("https://"))
            minLength = 8;
        else if (tmpStr.startsWith("www."))
            minLength = 4;

        if (minLength > 0) {
            // if http starts NOT with a space
            if (index == 0 || index > 0 && (text.charAt(index - 1) == ' ' || text.charAt(index - 1) == '\n')) {
                int maxIter = text.length() - index;
                if (maxIter > 0) {
                    StringBuilder sb = new StringBuilder(maxIter);
                    int lastIndex = index;
                    for (; lastIndex < text.length(); lastIndex++) {
                        char c = text.charAt(lastIndex);
                        if (c == ' ' || c == '\n' || c == '"')
                            break;

                        sb.append(c);
                    }

                    String url = sb.toString();
                    if (url.length() > minLength) {
                        String title = url;
                        UrlEntry entry = urlMap.get(index);
                        if (entry != null) {
                            if (lastIndex == entry.getLastIndex()) {
                                if (!Helper.isEmpty(entry.getResolvedTitle()))
                                    title = Strings.escapeMarkup(entry.getResolvedTitle()).toString();

                                if(entry.getResolvedUrl() != null)
                                    url = entry.getResolvedUrl();
                            }
                        }

                        tmpSb.append(toLink(url, title));
                        return lastIndex;
                    }
                }
            }
        }
        return -1;
    }
    
    int getUrlEntrySize() {
        return urlMap.size();
    }

    public String toLink(String url, String title) {
        if (url.startsWith("www."))
            url = "http://" + url;

        String shortTitle = title;
        if (title.length() > 50)
            shortTitle = title.substring(0, 47) + "...";

        return createLinkMarkup(shortTitle, title, Helper.stripOutLuceneHighlighting(url), "ex-tw-link");
    }

    public String createLinkMarkup(String shortTitle, String title, String url, String clazz) {
        return "<a title=\"" + title + "\" class=\"" + clazz + "\" target=\"_blank\" href=\"" + url + "\">" + shortTitle + "</a>";
    }
}

