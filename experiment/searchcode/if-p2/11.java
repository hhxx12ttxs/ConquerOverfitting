while(p2 != null &amp;&amp; p2.next != null){
p1 = p1.next;
p2 = p2.next.next;
if(p1 == p2){
break;
}
}
if(p2 == null || p2.next == null){ // important
return null;

