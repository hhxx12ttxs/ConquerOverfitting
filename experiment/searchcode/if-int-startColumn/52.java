lines.add(currentLine);
}


public String getSnippet(int startLine, int startColumn, int endLine, int endColumn) {
if (startLine == endLine &amp;&amp; startColumn == endColumn) {
return null;

