//		tmpDouble = (double) InitTmp * (double) 1670.17 + (double) 25891.34;
//		tmpFormat = new DecimalFormat(&quot;#####0&quot;);
//
//		if(tmpFormat.format(tmpDouble).length() == 5) tmpString = &quot;0&quot; + tmpFormat.format(tmpDouble);
tmpFormat = new DecimalFormat(&quot;#####0&quot;);

if(tmpFormat.format(tmpDouble).length() == 5) tmpString = &quot;0&quot; + tmpFormat.format(tmpDouble);

