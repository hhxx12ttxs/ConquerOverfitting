private final static String DOMAIN_ATTR_KEY = &quot;domain&quot;;

protected boolean hasDomain() {
return getAttributes().containsAttribute(DOMAIN_ATTR_KEY);
}

public String getDomain() {
if (hasDomain()) {
return getAttributes().getAttributeValue(DOMAIN_ATTR_KEY);

