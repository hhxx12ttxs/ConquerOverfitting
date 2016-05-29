public static boolean isJsonObject(String o) {
if (o == null) return false;

if (o.startsWith(&quot;{&quot;) &amp;&amp; o.endsWith(&quot;}&quot;) || o.startsWith(&quot;[&quot;) &amp;&amp; o.endsWith(&quot;]&quot;)) {
return true;
}

return false;
}

}

