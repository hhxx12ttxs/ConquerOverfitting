this.to = to;
}

/**
* @param rangeString format: (from,to)
* @return null if can&#39;t convert
rangeString = rangeString.replaceAll(&quot;\\s&quot;, &quot;&quot;);

if(!rangeString.matches(&quot;\\(\\d+,\\d+\\)&quot;))
return null;

// remove &#39;(&#39; &#39;)&#39;

