public static void main(String[] args) {
//Map的最后一次扩容
int mapSize =16;
for(int i=0;i<100;i++){
mapSize = mapSize * 2;
if(mapSize > 40*10000){
System.out.println(&quot;map的最后一次扩容：&quot; + (mapSize *3/4));
return;
}
}

int listSize = 10;
for (int i = 1; i < 1000; i++) {
listSize = (listSize * 3) / 2 + 1;

