for(char c : firstThree) {
if(!Character.isAlphabetic(c)) {
valid = false;
}
}
for(char c : lastThree) {
if(!Character.isDigit(c)) {
valid = false;
}
}

if(valid) {
this.regNr = regNr;

