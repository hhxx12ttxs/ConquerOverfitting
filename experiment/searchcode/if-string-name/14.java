
public class Thing extends Object{
protected String n;
public Thing(String name){
n=name;
}
public String toString(){
String c=getClass().getSimpleName();
if(c.equals(&quot;Thing&quot;)) return n;
return n+&quot; &quot;+c;

}
}

