package application;

public class OffHoursFactory {
	private static boolean mock = false;
	private static boolean isOffHours = false;
	private OffHoursFactory() {}
	
	public static void setMock(boolean mock){
		OffHoursFactory.mock = mock;
	}
	
	public static void setIsOffHours(boolean isOffHours){
		OffHoursFactory.isOffHours = isOffHours;
	}
	
	public static Hours getInstance() {
		if (mock) {
			if (isOffHours) {
				return OffHoursTrueMock.getInstance();
			} else {
				return OffHoursFalseMock.getInstance();
			}
		} else {
			return OffHours.getInstance();
		}
	}
}

