package week4.day1.interfaces;

public class Student implements IStudent, IWorker, Imusician {
	private int playHours;
	@Override
	public void study() {
		System.out.println("Im studying");
	}

	@Override
	public void work() {
		System.out.println("Im working");
	}

	@Override
	public void play(int hours) {
		playHours += hours;
		System.out.println("Student play " + hours + " hours");		
	}

	@Override
	public boolean goToConcert() {
		if (playHours > 100) {
			return true;
		} else {
			return false;
		}
	}
	
}

