@SuppressWarnings({&quot;UnusedDeclaration&quot;})
public abstract class Function0 extends AbstractBlock implements IFunction0 {

public Object invokeWithArgs(Object[] args) {
if(args.length != 0) {
throw new IllegalArgumentException(&quot;You must pass 0 args to this block, but you passed&quot; + args.length);

