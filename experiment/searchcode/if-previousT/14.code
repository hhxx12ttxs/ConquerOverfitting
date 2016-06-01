public class Secured extends Authenticator {

@Override
public String getUsername(Context ctx) {
if (ctx.session().get(&quot;facebookId&quot;) == null)
return null;

// see if the session is expired
String previousTick = ctx.session().get(&quot;userTime&quot;);

