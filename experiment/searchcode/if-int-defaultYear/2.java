public class CityInformation implements HowOldIn{
static String country = &quot;Ukraine&quot;;
String name;
int population;
double square;
int wasFounded;
int d = year - wasFounded;
if (d < 0) {
d = defaultYear - wasFounded;
System.out.println(&quot;year &quot;+ year + &quot; is changed to &quot; + defaultYear);

