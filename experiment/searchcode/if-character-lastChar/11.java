public ExprToken next() throws IOException {
if (lastChar == 0 || Character.isWhitespace(lastChar))
lastChar = reader.ignoreWhitespace();
private ExprToken readToken() throws IOException {
if (Character.isDigit(lastChar)) {
return readNumber();

