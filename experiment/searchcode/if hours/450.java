public class Calc {
	private static double oil;
	private static double water;
	private static double gas;
	
	public static double prod_rate(double month_vol, int hours){
		return (month_vol / hours);
	}
	public static double prod_rate_daily(double month_vol, int hours){
		double daily = month_vol * 24 / hours;
		return daily;
	}
	public static int down_time(String month, int actual_work_hours){
		int total_work_hours = total_month_hours(month);
		return (total_work_hours - actual_work_hours);
	}
	public static double loss(String month, double month_vol, int hours){
		double x = prod_rate(month_vol, hours);
		int y = down_time(month, hours);
		double z = x * y * 24;
		return z;
	}
	public static double price_loss(String month, double month_vol, int hours, double oil_price){
		total_month_hours(month);
		double x = prod_rate(month_vol, hours);
		int y = down_time(month, hours);
		double z = x * y * 24 * oil_price;
		return z;
	}
	public static int total_month_hours(String month){
		int hours = 0;
		if (month.equals("Jan") || month.equals("Mar") || month.equals("May") || month.equals("Jul") ||month.equals("Aug") || month.equals("Oct") || month.equals("Dec"))
			hours = 31 * 24;
		else if (month.equals("Feb"))
			hours = 28 * 24;
		else if (month.equals("Apr"))
			hours = 30 * 24;
		else if (month.equals("Jun"))
			hours = 30 * 24;
		else if (month.equals("Sep"))
			hours = 30 * 24;
		else if (month.equals("Nov"))
			hours = 30 * 24;
		else 
			System.out.println("Incorrect Month Input");
		return hours;
	}
}
