public static String getFibonacci(String n) {

long num=Integer.parseInt(n);
String retString;

retString = &quot;&quot;;
if(num == 0) {
for(long i=2;i<num;i++) {
c = a+b;
a=b;
b=c;
if(c<0) {
retString = &quot;-1&quot;;
break;
}
retString=retString+&quot; &quot;+Long.toString(c);
}

return retString;

}

}

