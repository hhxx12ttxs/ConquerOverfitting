public static String getClassNameInput(String tpIntegracao) {
String name = &quot;&quot;;
String in[] = tpIntegracao.split(&quot;-&quot;);
if (in[0].equalsIgnoreCase(&quot;a&quot;)) {
name = ClassDatabaseStringName.getClassStringAS400();
}
if (in[0].equalsIgnoreCase(&quot;o&quot;)) {
name = ClassDatabaseStringName.getClassStringOracle();

