int middle = 0;
while(left <= right){
middle = (left - right)/2 + right;

if(middle * middle == x){
result = middle;
break;
}else if(middle * middle < x){

if( (long)(middle+1) * (long)(middle+1) <= x ){

