public Tree(int value){
this.value = value;
}
public Tree(){}

public void addTree(int newValue){
if(newValue != this.value){
Tree side;
if(newValue < this.value){
side = this.right;
}
else {

