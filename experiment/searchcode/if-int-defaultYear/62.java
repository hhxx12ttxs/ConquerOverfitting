map.put(&quot;defaultYear&quot;, session.get(&quot;defaultYear&quot;));//从session中获取当前显示年份
if(listType == 1){//一般项目
hql.append(HQL4General);
item = (Object[]) p;
for (int i = 0; i < item.length; i++) {
if (item[i] == null) {// 如果字段值为空，则以&quot;&quot;替换

