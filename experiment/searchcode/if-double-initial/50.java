* public void setBalance(double newBalance) { balance = newBalance; }
*/

// constructor
public B_Account(double initialBalance) {
// validate that initial balance is bigger than 0
if (initialBalance > 0.0)
balance = initialBalance;

