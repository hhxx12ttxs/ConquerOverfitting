@Autowired
private JyXmManager jyXmManager;

@PostConstruct
public void intRmMap() {
hql.append(&quot;from PjXm where (fmemo1<>&#39;1&#39; OR fmemo1 is null)  &quot;);
if (fstatus != null &amp;&amp; fstatus == 1) {

