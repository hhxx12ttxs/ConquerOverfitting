if (keyNames != null &amp;&amp; keyNames.length > 0 &amp;&amp; keyName != null) {
for (int i = 0; i < keyNames.length; i++) {
if (keyName == keyNames[i]) {
result = &quot;error&quot;;
}
}
return result;
}

public static int KeysNumberID(String KeyName) {
int KeyIndex = 0;
if (KeyName.equals(&quot;-&quot;)) {

