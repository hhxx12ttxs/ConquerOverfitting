
public class WeekToPayLoan {

static int getWeeksToPayLoan(float loan, int weeks, int moneyPerWeek){
if(loan<moneyPerWeek){
return weeks;
}
else{
weeks++;
return getWeeksToPayLoan(loan-moneyPerWeek, weeks, moneyPerWeek);

