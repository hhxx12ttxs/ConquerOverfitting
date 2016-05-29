private static final String TABLE_NAME =&quot;b2b_business.bsp_domain&quot;;

public String getCompanyIdByPrefix(String prefix) {
domain.setPrefix(prefix);
Domain resultDomain = domainMapper.getDomain(domain);
if(resultDomain !=null)
return resultDomain.getCompanyId();

