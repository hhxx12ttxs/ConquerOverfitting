private Character firstChar;
private Pattern pattern;
int i = 0;

public Punctuation() {
public boolean characterTypeMatches(Character c) {
Matcher m = pattern.matcher(c.toString());
if (!m.matches()) {

