//Object�t�B�[���h�ɂ�set,get���\�b�h��ǉ�
//listSize, index�ɂ�get���\�b�h�̂ݒǉ�

private int listSize;
private Object[] object;
private int index = 0;
public void addMultipleContents(Object[] object){

for(int i=0; i<object.length; i++){
if(index <= listSize - 1){
this.object[index + i] = object[i];

