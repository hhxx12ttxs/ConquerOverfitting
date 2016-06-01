String previousTick = ctx.session().get(&quot;userTime&quot;);
if (previousTick != null &amp;&amp; !previousTick.equals(&quot;&quot;)) {
long previousT = Long.valueOf(previousTick);
long timeout = Long.valueOf(play.Play.application().configuration().getString(&quot;sessionTimeout&quot;)) * 1000 * 60;
if ((currentT - previousT) > timeout) {
// session expired
ctx.session().clear();

