private Code() {
}
public static int ReturnCode(String code){
int res=0;
if(code.startsWith(&quot;Code&quot;))
res=1;
else if(code.startsWith(&quot;Name&quot;))
res=2;
else if(code.startsWith(&quot;Surname&quot;))
res=3;

