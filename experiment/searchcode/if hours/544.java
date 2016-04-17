package practice;

public class HourlyWorker extends Worker{
	
	public HourlyWorker(String n, double r)  {
	      super(n, r);
	   }
	
	//they get paid 1.5 times pay rate for hours over 40
	
	public double computePay(double hours){
		double payOvertime=0;
		double payTotal=0;
		double hoursOver40=0;
		if(hours > 40){
			hoursOver40=hours - 40;
			payOvertime=(getWorkesRatePay()*1.5)*hoursOver40;
			payTotal+=payOvertime+(getWorkesRatePay()*40);
		}
		
		else{
			payTotal=getWorkesRatePay()*hours;
		}
		return payTotal;
		
	}



}

