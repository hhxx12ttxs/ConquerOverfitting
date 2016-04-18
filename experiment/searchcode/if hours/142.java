
public class Hours {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		int hours = 12;
		int minutes = 15;

		if (hours >= 12) {
			System.out.println((hours - 12) + ":" + minutes + " AM ");
		} else {
			System.out.println(hours + ":" + minutes + " PM ");
		}
	}

}

