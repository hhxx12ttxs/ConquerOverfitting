public class PatternSupplier implements Supplier<Pattern> {

private String patternString;
private PatternFactory factory;
this.patternString = patternString;
this.factory = factory;
}

@Override
public Pattern get() {
if (patternString == null) return null;

