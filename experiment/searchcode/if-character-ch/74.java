public boolean isIsomorphic(String s, String t) {
if(s.length() != t.length())
return false;
Map<Character, Integer> mapS = new HashMap<Character, Integer>();
Character chS = s.charAt(i);
Character chT = t.charAt(i);
if( !mapS.containsKey(chS) ) {
mapS.put(chS, ++cntS);
}
if( !mapT.containsKey(chT) ) {

