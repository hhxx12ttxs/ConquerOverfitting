public void add(T newInfo){
NodTree<T> newNod = new NodTree(newInfo);
if(root == null){
checkAvl(root);
}

private void addRec(NodTree<T> father, NodTree<T> newNod){
if(newNod.compareTo(father) <= 0){

