import org.openjena.atlas.junit.BaseTest ;
import org.openjena.riot.pipeline.normalize.CanonicalizeLiteral ;

import com.hp.hpl.jena.graph.Node ;
public class TestNormalization extends BaseTest
{
// TODO lang tags

@Test public void normalize_int_01()        { normalize(&quot;23&quot;, &quot;23&quot;) ; }

