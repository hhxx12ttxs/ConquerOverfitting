String version = new String(outputStream.toByteArray(), &quot;UTF-8&quot;);
System.out.println(&quot;Latest version: &quot; + version);
int intVersion = Integer.parseInt(version);
if (intVersion > buildNumber) {
int select = JOptionPane.showConfirmDialog(null, &quot;There are &quot; + (intVersion - buildNumber) + &quot; patches available. Update?&quot;, null, JOptionPane.YES_NO_OPTION);

