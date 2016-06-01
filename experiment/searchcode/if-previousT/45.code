String loggedInTime = session().get(&quot;loggedInTime&quot;);
if (loggedInTime != null) {
long previousT = Long.valueOf(loggedInTime);
long timeout = APPLICATION_SESSION_TIMEOUT * 1000 * 60;
long diff = (currentT - previousT);
if (diff > timeout) {

