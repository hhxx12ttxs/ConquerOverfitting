@NamedQuery(name = &quot;Usertab2.findByIdkey&quot;, query = &quot;SELECT u FROM Usertab2 u WHERE u.idkey = :idkey&quot;)})
public class Usertab2 implements Serializable {
Usertab2 other = (Usertab2) object;
if ((this.idkey == null &amp;&amp; other.idkey != null) || (this.idkey != null &amp;&amp; !this.idkey.equals(other.idkey))) {

