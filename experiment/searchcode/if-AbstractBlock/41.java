import java.util.List;
import java.util.Map;

import org.asciidoctor.ast.AbstractBlock;

public class YellBlock extends BlockProcessor {
public YellBlock(String name, Map<String, Object> config) {
super(name, config);
}

@Override
public Object process(AbstractBlock parent, Reader reader, Map<String, Object> attributes) {

