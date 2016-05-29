paraMap.put(&quot;defaultYear&quot;, session.get(&quot;defaultYear&quot;));
long wnall = 0; //警告
String hqli = null;
if(listType==1){
List<Object> num = baseService.query(hqli, paraMap);
if (num != null &amp;&amp; !num.isEmpty()) {
Object[] o;
for (int i = 0; i < num.size(); i++) {

