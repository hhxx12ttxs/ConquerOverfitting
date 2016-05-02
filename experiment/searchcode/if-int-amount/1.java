package com.theladders.bankkata.money;

public class Amount {
	private int amount;

	public Amount(int dollars, int cents) {
		amount = dollars * 100 + cents;
	}

	private Amount(int amount) {
		this.amount = amount;
	}

	private int getDollars() {
		return Math.abs(amount / 100);
	}

	private int getCents() {
		return Math.abs(amount % 100);
	}

	public boolean isNegative() {
		return amount < 0;
	}

	public boolean isZero() {
		return amount == 0;
	}

	public Amount negate() {
		return new Amount(amount * -1);
	}

	public Amount add(Amount other) {
		return new Amount(amount + other.amount);
	}

	public Object violate(AmountViolator violator) {
		return violator.violate(getDollars(), getCents());
	}

	public String leadingZero() {
		if (getCents() < 10)
			return "0";
		return "";
	}

	public String toString() {
		return "" + getDollars() + "." + leadingZero() + getCents();
	}

	public int compareTo(Amount other) {
		int otherAmount = other.amount;
		return AmountComparator.compare(amount, otherAmount);
	}

}

