//Constructor
Tax (double gi, int nod, String st) {
grossIncome = gi;
numberOfDependents = nod;
State = st;
}
*/
public double calcTax(){
if (grossIncome < 50000) {
return grossIncome*0.06;
}
else {
return grossIncome*0.08;
}
}
}

