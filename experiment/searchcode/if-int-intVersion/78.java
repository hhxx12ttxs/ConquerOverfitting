public static byte[] getFrameBuffData(Frame f) {
byte[] Buff = null;
byte[] dataBuff = null;
int intDataLen = 0;

if (f.strData != null &amp;&amp; !f.strData.equals(&quot;&quot;)) {
IntToHighLowByte(Buff, 9, intDataLen);
// 数据内容

if(intDataLen>0){
System.arraycopy(dataBuff, 0, Buff, 11, intDataLen);

