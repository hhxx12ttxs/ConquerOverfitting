int test = &quot;)]}&quot;.indexOf(bracket);
if(test != -1){
return true;
}else{
return false;
}
}
public boolean isOpen(){
int test = &quot;([{&quot;.indexOf(bracket);
if(test != -1){
return true;
}else{
return false;
}
}
}

