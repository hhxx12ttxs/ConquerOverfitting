package org.lensfield.glob;

import org.apache.commons.io.IOCase;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a glob pattern.
 *
 * Patterns obey the following format:
 *   '*' matches any number of characters in one directory (ie a single directory name or filename)
 *   '**' matches any number of directories (zero or more), but does not match file names
 *   '/' is a directory separator
 *   '\' escapes the following character
 *   '{...}' indicates a named pattern match
 *   all other characters match themselves.
 * @author sea36
 */
public class Glob {

    private final String glob;

    private Pattern pattern;
    private String format;

    private List<GlobSegment> segments = new ArrayList<GlobSegment>();

    private LinkedHashMap<String,Integer> groupIndex = new LinkedHashMap<String, Integer>();
    private List<String> groupNames = new ArrayList<String>();

    /**
     * Creates a new Glob.
     * @param glob string representation of the Glob
     * @throws IllegalArgumentException if the glob pattern is invalid.
     */
    public Glob(String glob) {
        this.glob = glob;
        parseGlob();
    }

    private void parseGlob() {

        StringBuilder formatPattern = new StringBuilder();
        StringBuilder pattern = new StringBuilder();
        StringBuilder segPattern = new StringBuilder();

        boolean wildcardDir = false;
        boolean wildcardSeg = false;

        boolean escapeNext = false;
        boolean newDir = true;
        int segStart = 0;
        for (int i = 0; i < glob.length(); i++) {
            char c = glob.charAt(i);
            if (escapeNext) {
                if (escape(c)) {
                    segPattern.append('\\');
                }
                segPattern.append(c);
                formatPattern.append(c);
                escapeNext = false;
            } else {
                switch (c) {
                    case '*':
                        if ((i+1) < glob.length() && glob.charAt(i+1) == '*') {
                            if (!newDir) {
                                throw new IllegalArgumentException("Directory wildcard '**' cannot be prefixed");
                            }
                            if ((i+2) >= glob.length() || glob.charAt(i+2) != '/') {
                                throw new IllegalArgumentException("Directory wildcard '**' cannot be suffixed");
                            }
                            if (groupIndex.containsKey("**")) {
                                throw new IllegalArgumentException("Only a single directory wildcard '**' is allowed");
                            }
                            i += 2;
                            int x = groupIndex.size();
                            groupIndex.put("**", x);
                            groupNames.add("**");
                            segPattern.append("(.*/|)");
                            formatPattern.append('<').append(Integer.toString(x)).append('>').append('/');
                            wildcardDir = true;
                        }
                        else {
                            if (groupIndex.containsKey("*")) {
                                throw new IllegalArgumentException("Only a single file wildcard '*' is allowed");
                            }
                            int x = groupIndex.size();
                            groupIndex.put("*", x);
                            groupNames.add("*");
                            segPattern.append("([^/]*)");
                            formatPattern.append('<').append(Integer.toString(x)).append('>');
                            wildcardSeg = true;
                        }
                        break;

                    case '{':
                        segPattern.append("[^/]*");
                        wildcardSeg = true;
                        int i0 = glob.indexOf('}', i);
                        if (i0 == -1) {
                            throw new IllegalArgumentException("Bad wildcard - no closing '}'");
                        }
                        String n = glob.substring(i+1, i0);
                        if (groupIndex.containsKey(n)) {
                            throw new IllegalArgumentException("Only a single file wildcard '"+n+"' is allowed");
                        }
                        int x = groupIndex.size();
                        groupIndex.put(n, x);
                        groupNames.add(n);
                        segPattern.append("([^/]*)");
                        formatPattern.append('<').append(Integer.toString(x)).append('>');
                        wildcardSeg = true;
                        i = i0;
                        break;

                    case '/':
                        if (newDir) {
                            // collapse '//+'
                            continue;
                        }
                        if (!wildcardDir) {
                            pattern.append(segPattern).append('/');
                            GlobSegment seg = new GlobSegment(glob.substring(segStart,i+1), Pattern.compile(segPattern.toString()), wildcardSeg, wildcardDir, false);
                            segPattern.setLength(0);
                            segments.add(seg);
                            segStart = i+1;
                            wildcardSeg = false;
                            newDir = true;
                        } else {
                            segPattern.append('/');
                        }
                        formatPattern.append('/');

                        continue;

                    // Escape next character
                    case '\\':
                        if (i == glob.length()) {
                            throw new IllegalArgumentException("Bad escape character: "+glob);
                        }
                        escapeNext = true;
                        break;

                    // Special character for formatting
                    case '<':
                        segPattern.append(c);
                        formatPattern.append("<<");
                        break;

                    default:
                        if (escape(c)) {
                            segPattern.append('\\');
                        }
                        segPattern.append(c);
                        formatPattern.append(c);
                }
            }
            newDir = false;
        }

        // Append last segment
        pattern.append(segPattern);

        GlobSegment seg = new GlobSegment(glob.substring(segStart), Pattern.compile(segPattern.toString()), wildcardSeg, wildcardDir, true);
        segments.add(seg);

        if (IOCase.SYSTEM.isCaseSensitive()) {
            this.pattern = Pattern.compile(pattern.toString());
        } else {
            this.pattern = Pattern.compile(pattern.toString(), Pattern.CASE_INSENSITIVE);
        }

        this.format = formatPattern.toString();
    }

    private static boolean escape(char ch) {
        switch (ch) {
            case '(':
            case ')':
            case '[':
            case ']':
            case '{':
            case '}':
            case '.':
            case '+':
            case '*':
            case '?':
            case '\\':
            case '^':
            case '$':
            case '|':
                return true;
            default:
                return false;
        }
    }

    public GlobSegment getSegment(int i) {
        return segments.get(i);
    }

    public boolean matches(String s) {
        return pattern.matcher(s).matches();
    }

    public GlobMatch match(String s) {
        Matcher m = pattern.matcher(s);
        if (m.matches()) {
            String[] groups = groupNames.toArray(new String[groupNames.size()]);
            String[] values = new String[groups.length];
            for (int i = 0; i < values.length; i++) {
                values[i] = m.group(i+1);
            }
            GlobMatch match = new GlobMatch(groups, values);
            return match;
        }
        return null;
    }

    public String format(Map<String,String> params) throws MissingParameterException {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < format.length(); i++) {
            char c = format.charAt(i);
            if (c == '<') {
                if (format.charAt(i+1) == '<') {
                    s.append('<');
                    i++;
                } else {
                    int i0 = ++i;
                    while (format.charAt(i) != '>') {
                        i++;
                    }
                    String key = format.substring(i0, i);
                    String name = groupNames.get(Integer.valueOf(key));
                    String value = params.get(name);
                    if (value == null) {
                        if (params.containsKey(key)) {
                            throw new MissingParameterException("Parameter '"+name+"' is null");
                        } else {
                            throw new MissingParameterException("Parameter '"+name+"' is undefined");
                        }
                    }
                    if ("**".equals(name) && "".equals(value)) {
                        // skip '/'
                        i++;
                    } else {
                        s.append(value);
                    }
                }
            } else {
                // Collapse '//+'
                if (c != '/' || s.length() == 0 || s.charAt(s.length()-1) != '/') {
                    s.append(c);
                }
            }
        }
        return s.toString();
    }

    public String getGlob() {
        return glob;
    }

    public List<String> getGroupNames() {
        return new ArrayList<String>(groupNames);
    }

}

