* Return array of possible character which matches number code. This uses
* formula as 3*code + 59 to find character. For example for code 4, above
public static char[] getPossibleAlphabates(char ch) {
int code = Character.getNumericValue(ch);
char firstChar;
switch (code) {

