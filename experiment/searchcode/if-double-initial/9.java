package account;

public class Account {

private double balance;

public Account(){}

public Account(double initialBalance){
if(initialBalance>0) balance=initialBalance;
}

public void credit(double amount){

