
public class Triangle {

public String getType (int a, int b, int c){
if(a <=0 || b <=0 || c <= 0){
return &quot;error&quot;;
} else if ((a+b <=c) || (a+c <=b) || (b+c <=a)){
return &quot;error&quot;;

