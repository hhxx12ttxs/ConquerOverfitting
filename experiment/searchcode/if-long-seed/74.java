public class SeedAction extends BaseAction implements ModelDriven<Seed>{

private static final long serialVersionUID = 1L;

@Resource
private SeedService seedService;
search = this.getSearch(search);
search.addFilterEqual(&quot;enterprise&quot;, enterprise);

if(seed.getSeed_name()!=null &amp;&amp;!&quot;&quot;.equals(seed.getSeed_name()))

