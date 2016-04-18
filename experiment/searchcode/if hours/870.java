package wrappers;

import java.util.ArrayList;
import java.util.List;

public class Day {

	private int hoursOfDay;
	private List<Hour> listOfHours;
	public int numberOfSlots;
	

	public Day(int hoursOfDay) {
		this.hoursOfDay = hoursOfDay;
		this.listOfHours = new ArrayList<Hour>();
	}

	public int getHoursOfDay() {
		return hoursOfDay;
	}

	public void setHoursOfDay(int hoursOfDay) {
		this.hoursOfDay = hoursOfDay;
	}

	public List<Hour> getListOfHours() {
		return listOfHours;
	}

	public void setListOfHours(List<Hour> listOfHours) {
		this.listOfHours = listOfHours;
	}

	public void addHourToList(Hour hour) {
		if (listOfHours.size() <= hoursOfDay) {
			this.listOfHours.add(hour);
		}
	}

	public String toString() {
		String s = "";
		s += "Planned Day: \n";
		int i = 0;
		for (Hour hour : listOfHours) {
			s += "hour: " + i + "\n";
			s += hour.toString();
			s += "\n";
			i++;
		}
		return s;

	}
	
	public int calNumberOfSlosts(){
		int counter = 0;
		for(Hour hour: listOfHours){
			counter += hour.getHourSlots().size();
		}
		return counter;
	}

}

