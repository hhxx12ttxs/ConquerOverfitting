String fu=featureUnits.get(i);
//			if(fu.length()>1){
//				if(normalizeUnderscoreSeqences &amp;&amp; fu.startsWith(&quot;__&quot;) &amp;&amp; fu.endsWith(&quot;__&quot;)){
//					fu=&quot;___&quot;;
//				}
//			}
if(normalizeNumbers &amp;&amp; Character.isDigit(fu.charAt(0)) &amp;&amp; Character.isDigit(fu.charAt(fu.length()-1))){

