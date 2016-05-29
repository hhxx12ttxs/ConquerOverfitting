for (int i = 0; i < S.length(); i++) {

if (S.charAt(i) == &#39;(&#39;) {
testBracket = testBracket + 1;
} else {
testBracket = testBracket - 1;
}

if (testBracket < 0)
return 0;
}

if (testBracket == 0)
return 1;
else
return 0;

}

}

