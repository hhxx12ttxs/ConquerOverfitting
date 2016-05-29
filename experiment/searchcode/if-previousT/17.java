String previousTick = session(&quot;userTime&quot;);
if(previousTick != null &amp;&amp; !previousTick.equals(&quot;&quot;)) {
long timeOut = Play.application().configuration().getLong(&quot;sessionTimeout&quot;) * 1000 * 60;
if ((currentT - previousT) > timeOut) {
long sessionId = Application.sessionId;

