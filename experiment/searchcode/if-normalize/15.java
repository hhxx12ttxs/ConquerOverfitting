public void normalizeScale()
{
normalizeScale(&quot;37&quot;, &quot;37.00&quot;);
normalizeScale(&quot;37&quot;, &quot;37&quot;, false);

normalizeScale(&quot;37.0&quot;, &quot;37.00&quot;);
normalizeScale(&quot;37.0&quot;, &quot;37.0&quot;, false);

normalizeScale(&quot;37.4&quot;, &quot;37.40&quot;);

