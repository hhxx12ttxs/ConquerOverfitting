if (tokens[j].trim().subSequence(10, 11).equals(&quot;T&quot;) &amp;&amp; tokens[j].trim().subSequence(19, 20).equals(&quot;Z&quot;)) {
if (tokens[j].trim().equals(theTDs.getContent(j)+&quot;Z&quot;)) continue;
else System.out.println(tokens[6].trim() + &quot; &quot; + tokens[j].trim() + &quot; &quot; + theTDs.getContent(j));
}
else {
if (tokens[j].trim().equals(theTDs.getContent(j))) continue;

