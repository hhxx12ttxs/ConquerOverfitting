private UserBaseManager userBaseManager;

@PostConstruct
public void intRmMap() {
JyXmMapUtil.initRyMap(jyXmManager);
if(taskId==null) {
JyXmYj jyXmYj = null;
JyXm dest = null; // 存储JyXm 对象

if (id != null &amp;&amp; xmid != null) { // 从点击编辑出返回

