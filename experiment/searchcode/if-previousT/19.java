public String getUsername(Context ctx) {

// see if the session is expired
String previousTick = ctx.session().get(&quot;userTime&quot;);
long timeout = Long.valueOf(Play.application().configuration().getString(&quot;sessionTimeout&quot;)) * 1000 * 60;
if ((currentT - previousT) > timeout) {
// session expired

