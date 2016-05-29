public IntStack(){
contain = new int[10];
end = -1 ;
}

public int pop(){
if( end == -1 ){
return -1 ;
}
return contain[end--];
}

public void push(int i ){
if( end == contain.length -1 ){
int[] new_arr = new int[contain.length * 2 ];

