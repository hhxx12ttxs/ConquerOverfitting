import generator.jim.composite.AbstractBlock;
import generator.jim.composite.InterfaceComponent;

public class BlockColon extends AbstractBlock {

public BlockColon() {
super(&quot;::&quot;, &quot;&quot;);
}

@Override
public void removeEmptyIf() {

