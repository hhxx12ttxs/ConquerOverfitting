@SuppressWarnings({&quot;UnusedDeclaration&quot;})
public abstract class Function5 extends AbstractBlock implements IFunction5 {

public Object invokeWithArgs(Object[] args) {
if(args.length != 5) {
throw new IllegalArgumentException(&quot;You must pass 5 args to this block, but you passed&quot; + args.length);

