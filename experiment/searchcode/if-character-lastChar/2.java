int j;
if (Character.isHighSurrogate(lastChar))
{
j = Character.codePointAt(text, charIndex);
while ((charIndex < length) &amp;&amp; (m == 0))
{
lastChar = text.charAt(charIndex);
int k;
if (Character.isHighSurrogate(lastChar))

