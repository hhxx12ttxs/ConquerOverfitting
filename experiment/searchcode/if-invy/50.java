while (rs.next()){
String p = rs.getString(&quot;uuid&quot;);
if (!p.equals(&quot;MASTER&quot;)){
File invF = new File(plugin.getDataFolder() + File.separator + &quot;inventories&quot; + File.separator + p + &quot;.dat&quot;);
YamlConfiguration invY = new YamlConfiguration();
invY.load(invF);
int size = invY.getInt(&quot;size&quot;);
Set<String> keys = invY.getKeys(false);

