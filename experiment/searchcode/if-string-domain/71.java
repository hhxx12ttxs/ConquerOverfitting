public static void add(String type, DomainObject domainObject) {
if (!DOMAIN_OBJECTS.containsKey(type)) {
byTitle.get(domainObject.name()).add(domainObject);
}

public static DomainObject get(String type, String name) {
if (DOMAIN_OBJECTS.containsKey(type)) {

