
public class DomainNameValidate {
String domainIdentifier=&quot;((\\p{Alnum})([-]|(\\p{Alnum}))*(\\p{Alnum}))|(\\p{Alnum})&quot;;
public boolean validateDomainName(String domainName) {
if((domainName == null) || (domainName.length()>63)) {
return false;

