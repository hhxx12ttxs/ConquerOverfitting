public SysUser getLoggedInUser() {
if (Controller.session().get(&quot;username&quot;) == null) {
long timeout = Long.valueOf(Play.application().configuration().getString(&quot;sessionTimeout&quot;)) * 1000 * 60;
if ((currentT - previousT) > timeout) {

