while( nextChar !=  (char) -1 ) {

/* Beginning of a comment */
if( lastChar == &#39;(&#39; &amp;&amp; nextChar == &#39;*&#39; ) {
nextChar = (char) r.read();

// update character #
charNumber++;
}

if( lastChar != (char) -1 )

