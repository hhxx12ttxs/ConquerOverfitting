package motors;

public class Motor implements MotorStandard{
	private String serialNumber;
	private int hoursOfService;
	private int maximumHoursBeforeService;

	public Motor(String inSerialNumber, int inMaximumHoursBeforeService) {
		serialNumber = inSerialNumber;
		hoursOfService = 0;
		maximumHoursBeforeService = inMaximumHoursBeforeService;
	}
	
	@Override
	public boolean equals(Object what) {
		Motor another = (Motor) what;
		
		if(serialNumber.equals(another.serialNumber)) {
			return true;
		}
		
		
		return false;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public int getHoursOfService() {
		return hoursOfService;
	}

	public int getMaximumHoursBeforeService() {
		return maximumHoursBeforeService;
	}

	public void addHoursOfService(int moreFlightHours) {
		hoursOfService += moreFlightHours;
	}

	public int hoursOfServiceLeft() {
		int hoursLeft = maximumHoursBeforeService - hoursOfService;
		return hoursLeft;
	}
	
	
	@Override
	public int compareTo(MotorStandard another) {
		int difference = serialNumber.compareTo(another.getSerialNumber());
		if(difference != 0) {
			return difference;
		}
		
		difference = hoursOfService - another.getHoursOfService();
		return difference;
	}
	
	public boolean isPastService() {
		if(hoursOfService > maximumHoursBeforeService) {
			return true;
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		String result = "";
		
		result += "serialNumber " + serialNumber;
		result += " maximumHoursBeforeService " + maximumHoursBeforeService;
		result += " hoursOfService "  + hoursOfService;
		
		return result;
	}
}

