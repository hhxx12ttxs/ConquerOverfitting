protected Double doubleSet(Double d){
if(d != null){
return d/100;
}
return d;
}
protected Double doubleGet(Double d){
if(d == null){
return 0.00;
}else{
return d/100;
}
}
protected Long getLong(Long l){

