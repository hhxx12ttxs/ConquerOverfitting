TrieHashNode t=getChild(firstChar);
if(t==null){
child=new TrieHashNode(firstChar);
children.put(firstChar, child);
}else{
child=t;
}

if(word.length()>1){

