package duty6;

public class HourlyEmployee extends Employee{
	//private int month;//���յ��·�
	
	private int hours;//ÿ�¹�����Сʱ��
	
	private double moneyhour;//ÿСʱ����Ǯ
	
	public HourlyEmployee() {
		// TODO Auto-generated constructor stub
	}

	public HourlyEmployee(int month, int hours, double moneyhour) {
		this.month = month;
		this.hours = hours;
		this.moneyhour = moneyhour;
	}
	
	
	//��д��Сʱ�����ʵķ���
	@Override
	public double getSalary(int month) {
		if(this.month==month&&this.hours>160){
			return 160*this.moneyhour+1.5*(this.hours-160)*moneyhour+100;
		}else if(this.month==month&&this.hours<160){
			return 160*this.moneyhour+100;
		}else if(this.month!=month&&this.hours>160){
			return 160*this.moneyhour+1.5*(this.hours-160)*moneyhour;
		}else if(this.month!=month&&this.hours<160){
			return 160*this.moneyhour;
		}else{
			return 0;
		}
	}
	
	

}

