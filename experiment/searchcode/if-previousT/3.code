//check for user logged on
if (session(&quot;currentUser&quot;) == null)
return null;

String previousTick = session(&quot;userTime&quot;);
long timeout = Long.valueOf(Play.application().configuration().getString(&quot;sessionTimeout&quot;)) * 1000 * 60;
if ((currentT - previousT) > timeout) {

