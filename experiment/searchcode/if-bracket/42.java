int index = indexOf(OPENING_BRACKETS, bracket);
if (index >= 0) {
return CLOSING_BRACKETS[index];
}
index = indexOf(CLOSING_BRACKETS, bracket);
if (index >= 0) {

