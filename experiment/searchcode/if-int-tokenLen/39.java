} else {
String segmentText = segment.getTextExtractor().toString();
int tokenLen = segmentText.split(&quot; &quot;).length;
for ( int i = 0 ; i < tokenLen ; ++i ) {
builder.append(&#39;T&#39;);

