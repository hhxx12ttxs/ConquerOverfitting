
public class Timer {
	public static String type = "down";
	public static int seconds = 0;
	public static int minutes = 0;
	public static int hours = 0;
	public static String extraSeconds;
	public static String extraMinutes;
	public static String extraHours;
public static void main(String[] args) throws InterruptedException{
	if (type == "down"){seconds=0;minutes=0;hours=0;}
	for(;type == "up";seconds++){
		
		if(seconds >= 60){minutes += 1; seconds = 0;}
		if(minutes >= 60){hours += 1; minutes = 0;}
		if(seconds < 10){extraSeconds = "0";}
		else{extraSeconds = "";}
		if(minutes < 10){extraMinutes = "0";}
		else{extraMinutes = "";}
		if(hours < 10){extraHours = "0";}
		else{extraHours = "";}
		
		System.out.print("\n\n\n"+extraHours+hours+":"+extraMinutes+minutes+":"+extraSeconds+seconds+"\n\n\n\n\n");
		Thread.sleep(1000);
		
	}
for(;type == "down";seconds--){
		
		if(seconds <= -1){minutes -= 1; seconds = 59;}
		if(minutes <= -1){hours -= 1; minutes = 59;}
		if(seconds < 10){extraSeconds = "0";}
		else{extraSeconds = "";}
		if(minutes < 10){extraMinutes = "0";}
		else{extraMinutes = "";}
		if(hours < 10){extraHours = "0";}
		else{extraHours = "";}
		
		System.out.print("\n\n\n"+extraHours+hours+":"+extraMinutes+minutes+":"+extraSeconds+seconds+"\n\n\n\n\n");
		Thread.sleep(1000);
		
	}
}
}

