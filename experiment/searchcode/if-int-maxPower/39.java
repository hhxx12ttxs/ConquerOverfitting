public static int addPower(int power, int addedPower, int MAXPOWER){
if((power + addedPower) <=MAXPOWER){
power += addedPower;
}
return power;
}

public static int subtractPower(int power, int subtractedPower){
if((power - subtractedPower) >= 0){

