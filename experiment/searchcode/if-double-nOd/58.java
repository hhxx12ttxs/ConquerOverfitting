String state;

Tax (int nod, double gi, String st){
numberOfDependents=nod;
grossIncome=gi;
state=st;
}

public double calcTax(){
if (grossIncome < 50000){
return grossIncome*0.06;
}
else {
return grossIncome*0.08;
}
}
}

