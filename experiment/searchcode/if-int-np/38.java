sb.append(&quot;</td>\n&quot;);
for (int j = 0; j < dependency[i].length; ++j) {
sb.append(&quot;<td>&quot;);
if (dependency[i][j] == null)
String indexedCat = null;

int line = 0;
while (s != null){
if (s.equals(&quot;EOP&quot;)) break;
line++;

