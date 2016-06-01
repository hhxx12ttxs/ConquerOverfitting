public class RegexValidator implements Validator
{


private Pattern pattern;

public RegexValidator(String pattern)
{
this(pattern, 0);
}

public RegexValidator(String pattern, int flags)
{
this.pattern = Pattern.compile(pattern, flags);

