private Hashtable<String, Domain> domainTable = new Hashtable<String, Domain>();

private DomainManager() {
domainTable.put(institutionShortForm, toBeAddedDomain);
return true;
}
}

public boolean removeDomains(String domainKey) {
if (domainTable.containsKey(domainKey)) {

