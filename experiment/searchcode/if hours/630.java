package home7;

public class Hourworker extends Worker {

	double hours;

	public Hourworker(double hours) {
		this.hours = hours;
	}

	@Override
	public double Zp() {
		double k = (((stavka / (weekhours * work_days)) * hours) + premia());
		return k;
	}

	protected double premia() {
		if (hours >= 15 * weekhours & hours < (work_days * weekhours)) {
			System.out.println("hours=" + hours);
			return .1 * stavka;
		} else if (hours >= (work_days * weekhours)) {
			System.out.println("hours=" + hours);
			return .2 * stavka;
		} else {
			System.out.println("hours=" + hours);
			return 0;
		}
	}

	public void zarplata() {
		if (hours >= work_days * weekhours) {
			System.out.println("�������� ���������� "
					+ getClass().getSimpleName() + " � " + Zp()
					+ "-- ��������� ��������� ��� ���쳿--" + premia());

		} else {
			System.out.println("�������� ���������� "
					+ getClass().getSimpleName() + " � " + Zp());
		}
	}

	@Override
	public String toString() {
		return Hourworker.class.getSimpleName() + "[Zp()=" + Zp()
				+ ", premia()=" + premia() + "]";
	}
}

