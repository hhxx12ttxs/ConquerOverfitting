Intent intentr;
PayDAO payDAO;
IncomeDAO incomeDAO;
Time time ;
int defaultMonth;
int defaultYear;
defaultMonth=intentr.getIntExtra(&quot;default&quot;, defaultMonth);
defaultYear=intentr.getIntExtra(&quot;defaulty&quot;, defaultYear);
int type=intentr.getIntExtra(&quot;type&quot;,0);//为0，选择上下月，为1，选择任意时间

