return newTuple.getResult();
}
if(oldTuple == null){
double sum = result + newTuple.getResult();
return sum;
}
double oldResult = result;
double newResult = oldResult - oldTuple.getResult()+newTuple.getResult();

