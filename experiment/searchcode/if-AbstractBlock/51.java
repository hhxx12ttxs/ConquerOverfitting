@SuppressWarnings({&quot;UnusedDeclaration&quot;})
public abstract class Function6 extends AbstractBlock implements IFunction6 {

public Object invokeWithArgs(Object[] args) {
if(args.length != 6) {
throw new IllegalArgumentException(&quot;You must pass 6 args to this block, but you passed&quot; + args.length);

