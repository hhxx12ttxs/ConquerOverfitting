public CnFieldParseInfo(CnFormat cnFormat, CnType cnType, int length, boolean must, boolean addLen, String align) {
if (cnFormat == null) {
throw new IllegalArgumentException(&quot;cnFormat cannot be null&quot;);
}else if(cnType == null){
throw new IllegalArgumentException(&quot;cnType cannot be null&quot;);

