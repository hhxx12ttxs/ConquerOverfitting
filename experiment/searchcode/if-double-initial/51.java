public FlowExAccount(double initialBalance){
balance = initialBalance > 0 ? initialBalance : 0;
}

public void credit(double amount){
balance += amount > 0 ? amount : 0;
}

public double debit(double amount){
if(amount > balance){

