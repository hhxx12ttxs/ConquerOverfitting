this.isRegex = isRegex;
if (isRegex) {
if (tablePattern != null){
String tableRegex = &quot;/&quot; + tablePattern.replaceAll(&quot;%&quot;,&quot;.*&quot;) + &quot;/&quot;;
this.viewPattern = viewPattern;
}


public Boolean tableMatchesRegex(String input) {
if (tableRegexPattern == null ){

