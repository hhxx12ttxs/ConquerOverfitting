lastChar = (char) buffer.read();

// identifier: [a-zA-Z][a-zA-Z0-9_]*
if (Character.isLetter(lastChar)) {
} while (Character.isLetterOrDigit(lastChar) || lastChar == &#39;_&#39;);

// check if identifier matches keyword

