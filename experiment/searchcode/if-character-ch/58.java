//comment
s = s.toLowerCase();
for(int i=0;i<s.length();i++){
Character ch = s.charAt(i);
if(ch.isAlphabetic(ch) || ch.isDigit(ch)){
stack.add(ch);

