* md4校验码
*/
private byte[] md4CheckSum = null;
/**
* Hash索引
*/
private int hashIndex = 0;
public void addNextSum(MySum mySum) {
int i = 0;
if (this.mySum == null) {
this.mySum = new MySum[8000];

