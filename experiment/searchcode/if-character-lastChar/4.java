this(new StringReader(str));
}

public ExprToken next() throws IOException {
if (lastChar == 0 || Character.isWhitespace(lastChar))
sb.append((char) lastChar);
lastChar = reader.read();
}

if (Character.isWhitespace(lastChar)) {

