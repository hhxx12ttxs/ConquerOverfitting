private Matcher matcher;

private static final String EMAIL_PATTERN = &quot;^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@&quot; + &quot;[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$&quot;;
public static int actualLength(String phone)
{
Long number=Long.parseLong(phone);

if(number>0)
{
int length = (int)(Math.log10(number)+1);

