public class WarrantyDataCompareTime implements WarrantyDataCountedI {

private long diffTime = 0;


public boolean isCounted (WarrantyAuditCompareI a, WarrantyAuditCompareI b) {
long diff = (long) ( (double)a.getTime()/(double)1000 + 0.5 ) - (long) ( (double)b.getTime()/(double)1000  + 0.5) ;

if ( (diff>=(-diffTime))  &amp;&amp; (diff<=diffTime) )

