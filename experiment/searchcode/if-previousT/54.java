public static String getLoggedInUser() {
try {
final String loggedInUser = session().get(&quot;loggedInUser&quot;);
if (loggedInUser != null) {
long diff = (currentT - previousT);
if (diff > timeout) {
// session expired

