* @exception IllegalArgumentException fromYearがtoYearより後の場合
*/
public static List<LocalDate> allThirteenFriday(int fromYear, int toYear)
{
final int targetDay = 13;

if(fromYear > toYear)
throw new IllegalArgumentException(&quot;&#39;fromYear&#39; must not be larger than &#39;toYear&#39;.&quot;);

