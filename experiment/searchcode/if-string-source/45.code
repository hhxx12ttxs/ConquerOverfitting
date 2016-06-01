protected String[] split;
protected int index;

public RegexSplitStringFilter(String pattern, StringSource source) {
super(pattern, source);
private void startNextInput() {
String input = source.nextString();
if (input == null) {
split = null;

