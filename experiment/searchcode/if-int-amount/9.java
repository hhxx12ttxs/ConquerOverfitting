this(0);
}

BankAccount(int amount) {
if (amount < 0) {
amount = 0;
}

this.balance = amount;
}

int withdraw(int amount) {
if (amount < 0) return 0;
if (amount > balance) return 0;

