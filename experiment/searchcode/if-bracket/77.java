if(brackets[i] == bracketOneOpen || brackets[i] == bracketTwoOpen || brackets[i] == bracketThreeOpen) {
myStack.push(brackets[i]);
} else if(brackets[i] == bracketOneClose || brackets[i] == bracketTwoClose || brackets[i] == bracketThreeClose) {
if(myStack.isEmpty()) {
return false;

