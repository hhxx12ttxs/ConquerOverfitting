public class DomainIndex {

private final String PREFIX = &quot;D&quot;; //$NON-NLS-1$
private Map<String, String> _idIndex;
private Map<String, Domain> _domainIndex;

private DomainIndex() {
_idIndex = new HashMap<String, String>();

