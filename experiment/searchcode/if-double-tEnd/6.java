tend = keyboard.nextDouble();

if (price > tend) {
stillOwes(price, tend);
transactionComplete = true;
tend = keyboard.nextDouble();
if (tend > owed) {
price = owed;
billsBack(tend, price);
owed = 0;

