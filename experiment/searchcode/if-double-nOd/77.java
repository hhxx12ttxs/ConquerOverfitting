convert((BNode)getRoot());	//parcurgerea arborelui si inlocuirea cheilor cu valorile
}
private void convert(BNode nod) {	//parcurgere inordine a arborelui
if(nod.isLeaf()==false) {
public Double eval() {	//evaluare expresie dintr-un arbore binar
return evalRec((BNode)getRoot());
}
public Double evalRec (BNode nod) {
if(isNumeric(nod.getData().toString())==true)	//daca nodul contine un numar

