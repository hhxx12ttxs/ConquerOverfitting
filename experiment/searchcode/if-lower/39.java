* @date 2016年3月14日上午11:31:46
* dataprovider
*/
public class dataprovider {

public boolean isBetween(int n, int lower, int upper) {
if (n >= lower) {
return true;
} else {
return false;
}
}

@Test(dataProvider = &quot;range-provider&quot;)

