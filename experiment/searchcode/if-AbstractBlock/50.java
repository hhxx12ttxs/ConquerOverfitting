@SuppressWarnings({&quot;UnusedDeclaration&quot;})
public abstract class Function9 extends AbstractBlock implements IFunction9 {

public Object invokeWithArgs(Object[] args) {
if(args.length != 9) {
throw new IllegalArgumentException(&quot;You must pass 9 args to this block, but you passed&quot; + args.length);

