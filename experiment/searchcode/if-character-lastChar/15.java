int lastChar = c.length - 1;
if (c[lastChar] == &#39;=&#39;) { // = is only possible follow-up
// symbol continues
context.setState(this, false);
} else {
if ((&#39;A&#39; <= c[lastChar] &amp;&amp; c[lastChar] <= &#39;Z&#39;)

