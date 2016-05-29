private Service charNormalize;

public EntityImpl() throws Exception
{
charNormalize = Outside.service(this,&quot;gus.string.transform.normalize.diacritics.lower&quot;);
public boolean f(Object obj) throws Exception
{
if(obj==null) return false;
String str = normalize(obj.toString());

