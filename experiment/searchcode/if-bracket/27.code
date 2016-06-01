int bracketCounter  = 0;
for(char c : S.toCharArray()) {
switch(c) {
case &#39;(&#39; :  bracketCounter++;
default  :  throw new IllegalArgumentException();
}
if(bracketCounter < 0)
return 0;
}

return (bracketCounter == 0) ? 1 : 0;
}
}

