package examTimetable;

import java.util.*;

public class Days implements Day {
	private final Hours[] hours = new Hours[]{Hour.A,Hour.B,Hour.C,Hour.D,Hour.E,Hour.F,Hour.G};
	
	public Days() {
		
	}
	
	@Override
	public int getFreeTime() {
		int freeTime = 0;
		for(Hours h: hours) {
			if(h.isBooked() == false) {
				freeTime++;
			}
		}
		return freeTime;
	}
	
	public Boolean hourCheck(Hours hour) {
		return (hour.isBooked());
	}

	@Override
	public int getFirstAvailableTime() {
		for(Hours h: hours) {
			if(h.isBooked() == false) {
				h.getTime(h);
			}	 
		}
		return -1;
	}

	@Override
	public List<Hours> getAvailableHours() {
		List <Hours> free = new ArrayList<Hours>();
		for(Hours h: hours) {
			if(h.isBooked() == false) {
				free.add(h);
			}
		}
		return free;
	}

	@Override
	public Hours getFirstHour() {
		for(Hours h: hours) {
			if(h.isBooked()) {
				return h;
			}
		}
		//day is full
		return null;
	}

	@Override
	public List<Hours> getAllHours() {
		return Arrays.asList(hours);
	}

	@Override
	public Boolean containsFreeSpace() {
		for(Hours h: hours) {
			if(h.isBooked() == false) {
				return true;
			}
		}
		//day is full
		return false;
	}

	@Override
	public void setHourBooked(Hours hour) {
		hour.book();
	}
}

