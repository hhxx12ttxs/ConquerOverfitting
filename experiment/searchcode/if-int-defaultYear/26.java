Integer k = 1;
if (expertLink != null &amp;&amp; !expertLink.isEmpty()) {
for (int j = 0; j < expertLink.size(); j++) {
Integer displayedYear = (Integer) httpSession.get(&quot;defaultYear&quot;);	//首页上选择的显示年份
if (!currentYear.equals(displayedYear)) {//如果两个年份不相同，禁止执行

