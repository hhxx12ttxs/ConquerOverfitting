Map<Character, Integer> map = new HashMap<Character, Integer>();
int longestLen = 0;
int startIndex = 0;
int endIndex = 0;
for (;endIndex < s.length(); endIndex++) {
Integer index = map.get(val);
map.put(val, endIndex);
if (index == null || index < startIndex) {

