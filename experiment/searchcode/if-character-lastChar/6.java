public ExprToken next() throws IOException {
if (lastChar == 0 || Character.isWhitespace(lastChar))
return readToken();
}

private ExprToken readToken() throws IOException {
if (Character.isDigit(lastChar)) {

