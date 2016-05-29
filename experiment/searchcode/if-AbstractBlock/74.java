import org.jruby.RubyObject;

public class AbstractBlockImpl implements AbstractBlock {

protected AbstractBlock delegate;
protected Ruby runtime;

public AbstractBlockImpl(AbstractBlock blockDelegate, Ruby runtime) {

