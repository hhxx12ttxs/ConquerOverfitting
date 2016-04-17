package facebookTests;

public class TPerson {
	private int age;
	private boolean isTired;
	
	public TPerson(int age) {
		this.age = age;
		this.isTired = false;
	}
	public TPerson(TPerson p) {
		this.age = p.age;
		this.isTired = p.isTired;
	}
	
	public void celebrate(int hours) {
		isTired = hours > 3 && age > 20;
	}
	
	public void rest(int hours) {
		isTired = (hours < 10);
	}
	
	public void dance(int hours){
		isTired = (hours > 5) && age > 18;
	}
	
	public void jumps(int hours){
		if (hours==3)
			isTired = !isTired;
	}
	
	public void reads(int hours){
		isTired = (hours > 5) && age > 18;
	}
	
	public String howAreYou() {
		return isTired ? "Tired" : "Fine";
	}
	
	
	public Integer howOldYou() {
		return age;
	}
}

