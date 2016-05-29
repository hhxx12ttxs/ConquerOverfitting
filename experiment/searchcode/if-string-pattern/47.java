import java.util.regex.Pattern;

public class PatternRule extends AbstractRule<String> {

private static final String RULE = &quot;matchPattern&quot;;
private static final String RULE_HAS = &quot;hasPattern&quot;;

private String rule;
private Pattern pattern;

