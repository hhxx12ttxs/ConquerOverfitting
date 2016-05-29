import org.apache.jena.riot.process.normalize.CanonicalizeLiteral ;
import org.junit.Test ;

import com.hp.hpl.jena.graph.Node ;
public class TestNormalization extends BaseTest
{
@Test public void normalize_int_01()        { normalize(&quot;23&quot;, &quot;23&quot;) ; }

