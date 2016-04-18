package week1;

public class Employee {
	private int hours;
	private double rate;

	public double calculatePaycheck(){
		if (40 - hours >= 0){
			return hours * rate;
		}
		else {
			return hours * rate - (1.5*(40 - hours)*rate);
		}
	}
}
