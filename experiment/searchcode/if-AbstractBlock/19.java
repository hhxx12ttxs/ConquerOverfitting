import java.util.List;

public class IfElse extends AbstractBlock
{
private BooleanFunction condition;
private AbstractBlock ifBlock;
private AbstractBlock elseBlock;

public IfElse(BooleanFunction condition, AbstractBlock ifBlock)

