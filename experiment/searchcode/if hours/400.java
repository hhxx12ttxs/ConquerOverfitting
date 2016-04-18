package tutorial.tutorial10Week10;

public class HourlyWorker extends Worker {

	public HourlyWorker(String name, double rate) {
		super(name, rate);
	}

	@Override
	public double computePay(int hours) {
		if (hours<=40){
			return rate*hours;
		}
		return rate*40+rate*(hours-40)*1.5;
	}

	
}

