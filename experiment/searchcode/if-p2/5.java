int begin, end;
if(A.length==0){return 0;}
// points p2 to a next element;
while(p2 < A.length){
if(A[p1]!=A[p2])
break;
p2++;
}
end = p2;

