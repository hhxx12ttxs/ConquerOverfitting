public void saveSeed(SeedDO seed) {
save(seed);
}

public boolean isSeedNameExists(String seedName){
long size = count(&quot;select count(*) from SeedDO where name like &#39;&quot;+seedName+&quot;&#39;&quot;,true);
if(size==0)
return false;
else

