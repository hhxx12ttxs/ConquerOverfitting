public <T> void reverse(T arr[]){

int startindex = 0;
int endindex = arr.length-1;

while(true){

if(startindex>=endindex){
break;
}

T temp = arr[startindex];
arr[startindex] = arr[endindex];

