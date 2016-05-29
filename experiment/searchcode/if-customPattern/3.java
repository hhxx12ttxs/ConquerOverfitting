private static final FilenameFilter validFileFilter = new FilenameFilter() {
public boolean accept(File dir, String name) {
if(validFilePattern.matcher(name).matches()){
public boolean accept(File dir, String name) {

if(validFilePattern.matcher(name).matches() &amp;&amp; this.customPattern.matcher(name).matches()){

