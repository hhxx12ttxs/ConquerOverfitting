
public class Tax {
double grossIncome;
String state;
int numberOfDependents;

Tax(double gi, String s, int nod){
grossIncome = gi;
state = s;
numberOfDependents = nod;
}

public double calcTax(){

