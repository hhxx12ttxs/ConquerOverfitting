public String getName() {
return this.name;
}

public void setWorkingHours(int hours) {
if (hours>=0) {
this.workingHours=hours;
public Task(String name, int workingHours) {
if(name != null &amp;&amp; name != &quot;&quot;) {
this.name = name;
}
else {

