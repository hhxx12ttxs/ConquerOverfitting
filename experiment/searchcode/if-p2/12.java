while(p2 != null &amp;&amp; p2!=p1){
if(p1 == null) p1 = head;
p1 = p1.next;
p2 = p2.next;
if(p2 != null) p2 = p2.next;
}

if(p2 == null) return null;

