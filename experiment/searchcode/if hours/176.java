package task2;

public class HourlyEmployee extends Employee{

	/* ÿСʱ���� */
	private double hoursalary;
	
	/* ĳ�¹���Сʱ��  */
	private int hours;
	
	
	/**
	 * @param name
	 * @param month
	 * @param hoursalary
	 * @param hours
	 */
    public HourlyEmployee() {}
	
    /* ���캯�� */	
	public HourlyEmployee(String name, int month, double hoursalary, int hours) {
		
		super(name, month);
		
		this.hoursalary = hoursalary;
		
		this.hours = hours;
	}

	/* ��ȡÿСʱ���� */	
	public double getHoursalary() {
		return hoursalary;
	}

	/* ��ȡĳ�¹���Сʱ */	
	public int getHours() {
		return hours;
	}

	/* ��ȡԱ��ĳ�¹��� */	
	public double getSalary(int month)
	{
		if(super.getMonth()==month)
		{
			/* ��Ա�����¹���ʱ�䳬��160��Сʱ�Ҹ�����������  */
			 if(hours>160)
		       return (hours-160)*hoursalary*1.5+160*hoursalary+100;
			 
			 /* ��Ա�����¹���ʱ��û�г���160��Сʱ�Ҹ�����������  */
			 else return hours*hoursalary+100;
		}
		else {
			/* ��Ա�����¹���ʱ�䳬��160��Сʱ�Ҹ��²���������  */
			 if(hours>160)
			       return (hours-160)*hoursalary*1.5+160*hoursalary;
			 
			 /* ��Ա�����¹���ʱ��û�г���160��Сʱ�Ҹ��²���������  */	 
			 else return hours*hoursalary;
			}
	}
}

