import java.util.concurrent.ConcurrentHashMap;

/**
*
* @author quyin
*/
public class DomainRuntimeInfoTable
{

private ConcurrentHashMap<String, DomainInfo>        domainInfos;
public DomainRuntimeInfo getOrCreate(String domain)
{
DomainRuntimeInfo result = table.get(domain);
if (result == null)

