ArrayList<Token> tokens = lex(processed.toString());
if (tokens.size() == 0) {
break;
}
int offset = 0;
processed.replace(token.start - offset, token.end - offset, &quot;{}&quot;);
int tokenLen = token.end - token.start;
offset += (tokenLen - 2);

