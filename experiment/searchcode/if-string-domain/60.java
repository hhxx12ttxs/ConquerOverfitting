* Demo for a class which returns valid values for a string domain
* @author FaKod
*
*/
public class ColorShadingDomainList implements DomainList {
public ValidDomainEntries<String> getDomainEntryList(String domainId) {
if (domainId.equals(&quot;ColorShadingDomain&quot;))
return vde;

