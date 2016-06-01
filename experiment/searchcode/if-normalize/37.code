public class TClientTest {


@Test
public void testNormalizeFile() throws IOException {

assertEquals( null, TClient.normalizeFiles(null) );
assertEquals( &quot;hola&quot;, TClient.normalizeFiles(&quot;hola&quot;) );

File file = new File(&quot;./testNormalizeFile&quot;);
if( file.exists() ) file.delete();
IO.writeContent(&quot;Hola&quot;, file);

