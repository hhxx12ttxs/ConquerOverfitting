@NamedQuery(name = &quot;Seckey.findByIdKey&quot;, query = &quot;SELECT s FROM Seckey s WHERE s.idKey = :idKey&quot;)
})
public class Seckey implements Serializable {
Seckey other = (Seckey) object;
if ((this.idKey == null &amp;&amp; other.idKey != null) || (this.idKey != null &amp;&amp; !this.idKey.equals(other.idKey))) {

