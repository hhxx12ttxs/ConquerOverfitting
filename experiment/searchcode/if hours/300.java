
public class Miillllss {

	public static void main(String[] args) {
		
		
		
		long milliseconds =System.currentTimeMillis();
		int seconds = (int) (milliseconds / 1000) % 60 ;
		int minutes = (int) ((milliseconds / (1000*60)) % 60);
		int hours   = (int) ((milliseconds / (1000*60*60)) % 24);
		int hoursBalkan = hours+2;
		
		System.out.printf("%d:%d:%d\n",hoursBalkan,minutes,seconds);
		
		if (hoursBalkan>=6 && hoursBalkan<10) {
			System.out.println("Jutro");
		}
		else if (hoursBalkan>=10 && hoursBalkan<18) {
			System.out.println("Dan");
		}
		else if (hoursBalkan>=18 && hoursBalkan<22) {
			System.out.println("Vece");
		}
		else if (hoursBalkan>=22 && hoursBalkan<6) {
			System.out.println("Noc");
		}
	
		

	}

}

