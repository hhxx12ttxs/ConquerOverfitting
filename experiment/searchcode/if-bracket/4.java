for (char bracket : input.toCharArray()) {
if (!handleBracket(bracket)) {
return false;
}
}

return brackets.isEmpty();
}

private boolean handleBracket(char current) {
if (isOpening(current)) {

