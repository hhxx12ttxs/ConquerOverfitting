public class RepaymentTimeAccount extends SavingAccount {
private double penalty;
private Date paymentDate;

@Override
public boolean withdrawWithCheck(double amount){
if(balans - amount < 0){
return false;
}
balans = balans * (1 - penalty);

