public void parameterDeal(String fromYear,String fromMonth) throws BillySearchException{
if(fromYear == null || (fromMonth != null &amp;&amp; fromMonth.length()>0 &amp;&amp; !Tool.isRightMonth(fromMonth) || !Tool.isRightYear(fromYear))){
fromDate = fromYear + &quot;-&quot; + fromMonth + &quot;-&quot; + &quot;01&quot;;
if(yearCompute){
toDate = &quot;&quot;+(Integer.parseInt(fromYear)+1) + &quot;-01-01&quot;;
}else{

