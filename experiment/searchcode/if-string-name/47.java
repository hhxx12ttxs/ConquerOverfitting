String objectName,
String propertyName)
throws Exception {
if (value==null) return;
if (value.toString().indexOf(getString()) >= 0) {
errors.add(&quot;exclude_string&quot;, propertyName, objectName, getString());

