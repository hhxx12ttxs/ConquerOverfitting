package ex04;

public class RecursiveBracketChecker {
public boolean validate(String input) {
return validate(reader) &amp;&amp; !reader.hasNext();
}

private boolean validate(StringReader reader) {
if (!reader.hasNext() || !isOpeningBracket(reader.current())) {

