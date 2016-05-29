private Service charNormalize;

public EntityImpl() throws Exception
{charNormalize = Outside.service(this,&quot;gus.string.transform.normalize.diacritics.lower&quot;);}


private String normalize(String s) throws Exception

