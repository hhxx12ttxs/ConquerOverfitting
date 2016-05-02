/*
 * Copyright 2011 DeepDiff Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package deepdiff.pointprocessor;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.log4j.Logger;

import deepdiff.core.ConfigProperty;
import deepdiff.core.Configurable;
import deepdiff.core.DiffPoint;
import deepdiff.core.DiffPointProcessor;
import deepdiff.core.DiffPointProcessorFactory;
import deepdiff.core.IllegalConfigException;

/**
 * A {@link DiffPointProcessor} that selectively ignores some DiffPoints based on configurable
 * regular expressions, and then passes the remaining {@link DiffPoint}s on to another
 * {@link DiffPointProcessor} for further processing.
 */
public class RegexFilterDiffPointProcessor implements DiffPointProcessor, Configurable {
    private static final String TYPE_CHILD = "child";
    private static final String TYPE_FILTER = "filter";

    private static final Logger log = Logger.getLogger(RegexFilterDiffPointProcessor.class);

    private final Collection<Pattern[]> filters = new LinkedList<Pattern[]>();
    private DiffPointProcessor child;

    public DiffPointProcessor getChild() {
        return child;
    }

    /**
     * Processes a {@link DiffPoint}. First, it checks the {@link DiffPoint} against the configured
     * regular expressions to see if it should be ignored. If not, it delegates further processing
     * to the child processor.
     * 
     * @param diffPoint the {@link DiffPoint} to process
     */
    public void processDiffPoint(DiffPoint diffPoint) {
        if (!isVetoed(diffPoint)) {
            child.processDiffPoint(diffPoint);
        }
    }

    /**
     * Checks whether the specified {@link DiffPoint} matches any of the configured regular
     * expressions.
     * 
     * @param diffPoint the {@link DiffPoint} to check against the regular expressions
     * 
     * @return whether the specified {@link DiffPoint} should be ignored due to the configured
     *         regular expressions
     */
    protected boolean isVetoed(DiffPoint diffPoint) {
        String scopedPath = diffPoint.getDiffUnit().getScopedPath();
        for (Iterator<Pattern[]> it = filters.iterator(); it.hasNext();) {
            Pattern[] filter = it.next();
            Pattern pathPattern = filter[0];
            Pattern messagePattern = filter[1];
            if (patternApplies(pathPattern, scopedPath)) {
                String message = diffPoint.getMessage();
                if (patternApplies(messagePattern, message)) {
                    log.debug("Diff point (" + scopedPath + ", " + message
                            + " vetoed due to filter");
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks whether a specific Pattern matches a String. If no Pattern is specified, it matches
     * all content.
     * 
     * @param pattern the Pattern to check against the content; if null, all content is considered a
     *            match
     * @param content the content to check against the Pattern
     * 
     * @return whether the pattern matches the content
     */
    private boolean patternApplies(Pattern pattern, String content) {
        if (pattern != null) {
            Matcher matcher = pattern.matcher(content);
            return matcher.matches();
        } else {
            // Null pattern means match everything
            return true;
        }
    }

    /**
     * Adds a property to the processor configuration
     * 
     * @param property the property to add
     * 
     * @throws IllegalConfigException if the property isn't valid
     */
    public void addProperty(ConfigProperty property) throws IllegalConfigException {
        String type = property.getType();
        if (TYPE_CHILD.equals(type)) {
            if (child != null) {
                throw new IllegalConfigException("Filter already has a " + TYPE_CHILD);
            }
            String id = property.getValue();
            child = DiffPointProcessorFactory.get(id);
        } else if (TYPE_FILTER.equals(type)) {
            String pathRegex = property.getName();
            String messageRegex = property.getValue();
            try {
                Pattern pathPattern = Pattern.compile(pathRegex);
                Pattern messagePattern = Pattern.compile(messageRegex);
                Pattern[] filter = { pathPattern, messagePattern };
                filters.add(filter);
            } catch (PatternSyntaxException pse) {
                throw new IllegalConfigException("Invalid pattern specified", pse);
            }
        } else {
            throw new IllegalConfigException("Unsupported property type: " + type);
        }
    }

    /**
     * Checks that a valid child processor was specified
     * 
     * @throws IllegalConfigException if no valid child processor was specified
     */
    public void validateProperties() throws IllegalConfigException {
        if (child == null) {
            throw new IllegalConfigException("No property with type " + TYPE_CHILD + " specified");
        }
        log.info("Configured " + filters.size() + " filters");
    }
}

