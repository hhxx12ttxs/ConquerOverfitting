return null;
}
return substring;
}
if ( Character.isWhitespace(ch) ) {
return text.substring(start,pos);
} else if (Character.isJavaIdentifierStart(ch)) { // identifier

