int n=sc.nextInt();
int diff=n-1900;
int spec=diff/4;
if((diff%4==0 &amp;&amp; diff%100!=0) || diff%400==0){
else
diff=diff+spec+1;
diff=diff%7;
if(diff==1){

