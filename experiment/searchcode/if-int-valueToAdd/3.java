public void addVariable(double valueToAdd){
this.values.add(valueToAdd);

// look if min or max values changes
if(firstValue){
minValue = valueToAdd;
firstValue = false;
}else{
if (maxValue < valueToAdd){
maxValue = valueToAdd;

