lastChar = text.charAt(charIndex - 1);
byte b1;
if (Character.isLowSurrogate(lastChar))
{
int i = Character.codePointBefore(text, charIndex);
lastChar = text.charAt(charIndex);
byte b1;
if (Character.isHighSurrogate(lastChar))
{
int i = Character.codePointAt(text, charIndex);

