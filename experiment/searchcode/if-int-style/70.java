boolean characterStyle;
boolean sectionStyle;
public String styleName;
public int number;
int basedOn;
int nextStyle;
public boolean handleKeyword(String keyword, int parameter)
{
if (keyword.equals(&quot;s&quot;)) {
characterStyle = false;
sectionStyle = false;

