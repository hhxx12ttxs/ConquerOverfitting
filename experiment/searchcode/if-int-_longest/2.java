lookupMap = new HashMap<String, CharSequence>();
int _shortest = Integer.MAX_VALUE;
int _longest = 0;
if (lookup != null) {
this.lookupMap.put(seq[0].toString(), seq[1]);
final int sz = seq[0].length();
if (sz < _shortest) {

