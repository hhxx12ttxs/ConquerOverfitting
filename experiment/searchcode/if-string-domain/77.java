public class DomainManagerComp {

private Map<String, DomainComponent> m_domains = new HashMap<String, DomainComponent>();
public DomainComponent createDomain(String domainId) {
DomainComponent domain = new DomainComponent(domainId);
m_domains.put(domainId, domain);

