result=&quot;0&quot;+result;
}
return result;
}

public static int lengthFrom(String str){
int startIndex=0;
while(startIndex<str.length()&amp;&amp;str.indexOf(startIndex)==&#39;0&#39;)startIndex++;
if(startIndex>=0&amp;&amp;startIndex<str.length()){

