int position = mProgramStream.getPosition();
token.setPosition(position);

if (lastChar == &#39;\0&#39;) {
else if (lastChar == &#39;$&#39;) {
matchCharacter(token, lastChar);
}

else if (lastChar == &#39;#&#39;) {
if (forwardChar() == &#39;(&#39;)

