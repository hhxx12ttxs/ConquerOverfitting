
public class Account {
String accountNo;
String ownerName;
int balance;
void deposit(int amount) {
balance += amount;
}
int withraw(int amount) throws Exception {
if (balance < amount)

