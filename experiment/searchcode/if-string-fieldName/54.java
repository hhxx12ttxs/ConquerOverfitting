if (isCheckbox &amp;&amp; value != null) value = &quot;y&quot;;
else if (isCheckbox) value = &quot;n&quot;;
String fieldName=&quot;&quot;, contractId=&quot;&quot;;
Logger log = Logger.getLogger(CloseoutJSON.class);
if (fieldId != null) {
fieldName = fieldId.substring(0,2);

