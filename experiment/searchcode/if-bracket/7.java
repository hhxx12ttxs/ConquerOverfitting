for(int i=0, len=chkStr.length(); i<len; i++) {
Bracket bracket = null;
if((bracket = bl.getBracket(chkStr.charAt(i))) == null) {
continue;
}

if(bracket.isOpen()) {
stack.push(bracket);
} else {
Bracket openBracket = stack.pop();

