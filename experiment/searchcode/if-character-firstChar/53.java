protected String originalSequence;
protected char firstChar;

protected int count;

public LineStartCharacterComparator(String seq) {
originalSequence = seq;
if (originalSequence.length()>0) {
firstChar = originalSequence.charAt(0);

