package compreter;

import java.util.regex.Pattern;

public interface Patterns {
String patternStart = &quot;[\\s]*&quot;;
String patternEnd = &quot;[\\s]+.*&quot;;
public static final Pattern
PUNCTUATORS =Pattern.compile(patternStart + &quot;(\\{|\\}|\\(|\\)|\\[|\\]&quot; +

