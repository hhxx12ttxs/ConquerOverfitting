import java.util.*; 
import java.io.*; 

public class LifeGuard{
	

	private String name; 
	private double currentHours;
	public Availability[] availability; 
	private File file; 
	private Scanner scanner; 
	private PrintWriter out; 

	public LifeGuard(double hours,String name){
		this.name = name; 
		this.currentHours = hours;
		availability = new Availability[7]; 
	}

	public String getName(){
		return name; 
	}

	public double getHours(){
		return currentHours; 
	}

	public void giveHours(double hours){
		boolean check; 
		if(hours < 0){
			hours = hours * -1; 
			check = false; 
		}else{
			check = true; 
		}
		if(((hours*100)%100)<=30){
			hours = Math.floor(hours) + (((hours*100)%100)/60);
		}else{
			hours = Math.floor(hours) + ((((hours*100)%100)-40)/60); 
		} 
		if(check){
			currentHours += hours; 
		}else{
			currentHours -= hours;
		}
	}

	public void writeHours(){
		try{
			file = new File(ScheduleDriver.facility + " Employees.txt"); 
			scanner = new Scanner(file); 
			String total = ""; 
			String lineTemp = ""; 
			while(scanner.hasNext()){
				String line = scanner.nextLine(); 
				Scanner lineScan = new Scanner(line);
				String type = lineScan.next() + " "; 
				String name = lineScan.next() + " " + lineScan.next() + " ";
				if(name.equalsIgnoreCase(getName() + " ")){  
					lineTemp = type + name + getHours() + " ";
					lineScan.next(); 
					while(lineScan.hasNext()){
						lineTemp = lineTemp + lineScan.next() + " "; 
					}
				}else{
					lineTemp = type + name; 
					while(lineScan.hasNext()){
						lineTemp = lineTemp + lineScan.next() + " "; 
					}
				}  
				if(scanner.hasNext()){
					total = total + lineTemp + "\n"; 
				}else{
					total = total + lineTemp; 
				}
			}
			out = new PrintWriter(new BufferedWriter(new FileWriter(ScheduleDriver.facility + " Employees.txt"))); 
			out.print(total); 
			out.close();
		}catch(IOException e){
			System.exit(0); 
		}
	}
}
