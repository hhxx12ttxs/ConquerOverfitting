Pattern.compile(&quot;\\A\\(\\?:(.*)\\)\\?\\Z&quot;),
Pattern.compile(&quot;\\A(.|\\(.*\\))\\?\\Z&quot;)
};
public static String escape(String pattern) {
return pattern.replaceAll(&quot;([\\.\\-\\[\\]])&quot;, &quot;\\\\$1&quot;);
}

public static String optionalize(String pattern) {

