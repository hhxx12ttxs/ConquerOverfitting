private static void addOdd(LinkedList linked){
for(int i = 1; i < listSize; i++){
if(i%2 == 1){
linked.addSorted(i);
private static void addEven(LinkedList linked){
for(int i = 2; i <= listSize; i++){
if(i%2 == 0){
linked.addSorted(i);

