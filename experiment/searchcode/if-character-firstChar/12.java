public Token tryReadToken(String input, int offset) {
char firstChar = input.charAt(offset);
if(&quot;(){}[];,.&quot;.indexOf(firstChar) > -1)
return new Token(&quot;sep&quot;, Character.toString(firstChar));
else
return null;
}
}

