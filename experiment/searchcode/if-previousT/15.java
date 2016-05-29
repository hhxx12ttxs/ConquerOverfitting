public String getUsername(Context ctx) {
// if the userId is found, this will return not null and allow
long timeout = Long.valueOf(Play.application().configuration().getString(&quot;sessionTimeout&quot;)) * 1000 * 60;
if((currentT - previousT) > timeout) {
// session expired
ctx.session().put(&quot;userId&quot;, &quot;&quot;);

