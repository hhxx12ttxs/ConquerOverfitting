private int maxSize;
private int listSize;
private int curr;

public AList(){
this(default_size);
assert listSize < maxSize : &quot;Max size exceeded&quot;;
for(int i = listSize; i<curr; i--)
listArray[i] = listArray[i-1];

