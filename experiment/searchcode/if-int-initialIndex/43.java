throws IllegalArgumentException {

String after = rsm.getAfter();
String before = rsm.getBefore();

int initialIndex = rsm.getIndex();
int lastIndex = -1;

if (after != null) {
initialIndex = Integer.valueOf(after);

