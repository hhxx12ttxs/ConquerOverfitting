setType(type);
}

@Override
public Object getResult(){
Map.Entry<K, Double> entry = getWinner();
if(entry == null){
// The specified value was not encountered during scoring
if(result == null){
return 0d;
}

return result;
}

Map.Entry<K, Double> getWinner(){

