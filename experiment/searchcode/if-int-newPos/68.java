public void enqueue(int x1, int y1){
MazeNode newpos = new MazeNode(x1,y1);
if (empty()){
tail = newpos;
head = newpos;
public void enqueue(MazeNode newpos){
if (empty()){
tail = newpos;
head = newpos;
}
else {
tail.setNext(newpos);

