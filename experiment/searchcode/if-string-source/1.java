String subSource =&quot;&quot;;
String tempString =&quot;&quot;;
for ( int i=0; i < sourceString.length(); i ++){
if ( tempString.indexOf(source[i]) == -1){
tempString+= source[i];
}else{
if ( subSource.length() < tempString.length()) {

