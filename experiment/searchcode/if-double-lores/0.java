ClanPlayer cp = SimpleClans.getInstance().getClanManager().getClanPlayer(p);
String team = &quot;Civile&quot;;
if(cp != null) {
float kdr	= cp.getKDR();
double rkdr = Math.round(kdr * 100D)/100D ;
meta0.setOwner(p.getName());
lores.add(prefixg);
if(expir != null &amp;&amp; grade.contains(&quot;guerrier&quot;)) {

