public static int titleToNumber(String s) {
if(s==null) return 0;
int res = 0;
int strlen = s.length();
for(int i = strlen-1; i >=0; i-- ){
res += Math.pow(26, (strlen-1-i))*(int)(s.charAt(i)-&#39;A&#39;+1);

