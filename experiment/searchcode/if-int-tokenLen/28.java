String lastFour;
/*Token的长度*/
int tokenLen;
/*Token*/
String token = &quot;&quot;;
/*Token的有效日期*/
int tokenExpDate = -1;
JSONObject pkg2 = vault.insertToken(js2);

int responseCode2 = pkg2.getInt(&quot;responseCode&quot;);
if(ResponseCode.INSERT_TOKEN_TOKENLEN_SUCCESS == responseCode2) {

