CaseFilter cf = it.next();
for(String searchChar : searchChars) {
cf.setLastName(searchChar);
log.info(&quot;Searching with last name: &quot;+searchChar);
// check for interruption
if(Thread.interrupted()) {

