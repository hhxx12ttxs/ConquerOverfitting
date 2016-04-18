
public class Task6 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		//Program transforms time from military format to AM/PM format
		
		int hours = 1;
		int minutes = 1;
		
		if (hours > 0 && hours < 1) {
			hours += 12;
			System.out.printf("%d:%d = %d.%d AM", hours, minutes , hours, minutes);
		}else if(hours >= 1 && hours < 12){
			System.out.printf("%d:%d = %d.%d AM", hours, minutes , hours, minutes);
		}else if (hours > 1 && hours <= 24){
			System.out.printf("%d:%d = %d.%d PM", hours, minutes , hours-12, minutes);
		}

	}

}

