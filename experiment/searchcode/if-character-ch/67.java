break;
if (Character.isUpperCase(ch))
ch += (ch < &#39;M&#39;) ? 13 : -13;
else if (Character.isLowerCase(ch))
ch += (ch < &#39;m&#39;) ? 13 : -13;
else if (Character.isDigit(ch))

