// Logger.debug(&quot;Hello=============&quot;+Play.application().configuration().getString(&quot;sessionTimeout&quot;));

//* see if user is logged in*//*
if (ctx.session().get(&quot;username&quot;) == null)
long timeout = Long.valueOf(Play.application().configuration().getString(&quot;sessionTimeout&quot;)) * 1000 * 60;
if ((currentT - previousT) > timeout) {
// session expired

