char currentBracket = parentheses.charAt(i);
if (currentBracket == &#39;(&#39; || currentBracket == &#39;{&#39; || currentBracket == &#39;[&#39;) {
return;
}

char lastBracket = stack.pop();
if ((lastBracket != &#39;(&#39; || currentBracket != &#39;)&#39;) &amp;&amp;

