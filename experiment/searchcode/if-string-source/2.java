public class SQLInjectionFilter
{
// SQL injection 방지
public String removeInjection(String source) {

String result = StringUtils.EMPTY;

if (StringUtils.isNotEmpty(source)) {

source = StringUtils.replace(source, &quot;&amp;lsquo;&quot;, &quot;‘&quot;);

