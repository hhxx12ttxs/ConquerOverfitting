// Only allow:
// alpha-numeric, whitespace, and special: . , - _
private static final String InvalidChars = &quot;[^a-zA-Z0-9 \\.,\\-_]&quot;;

public static String clean(final String name) {
if (name == null)
return null;

return name.replaceAll(InvalidChars, &quot;&quot;);
}

}

