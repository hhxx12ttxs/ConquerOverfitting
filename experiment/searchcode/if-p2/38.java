p1=p1.getNext();
p2=p2.getNext().getNext();
if(p1==p2){
return true;
}
}
return false;
}
public LinkedList Start(LinkedList head,boolean cycle){

if(cycle ==true)
p1=head;
while(p1!=p2){

