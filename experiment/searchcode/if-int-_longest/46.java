int _shortest = Integer.MAX_VALUE;
int _longest = 0;
for(CharSequence[] seq : lookup) {
this.lookupMap.put(seq[0], seq[1]);
int sz = seq[0].length();
if(sz < _shortest) {

