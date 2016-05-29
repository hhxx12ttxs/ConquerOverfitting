public boolean checkBracketBalance(String bracketSequence){
if(bracketSequence == null)
return false;
for(char element : bracketSequenceArray){
if(bracketStack.isEmpty())
bracketStack.push(element);
else{

