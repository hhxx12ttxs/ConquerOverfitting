package Model;

public class Patient extends Human {
	
	private int codeDisease; //code of the disease 
	
	public int getCodeDisease() {
		return codeDisease;
	}
	public void setCodeDisease(int codeDisease) {
		this.codeDisease = codeDisease;
	}
	public double getHours() {
		return hours;
	}
	public void setHours(double hours) {
		this.hours = hours;
	}
	double hours; //the hours of reception 

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + codeDisease;
		long temp;
		temp = Double.doubleToLongBits(hours);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Patient other = (Patient) obj;
		if (codeDisease != other.codeDisease)
			return false;
		if (Double.doubleToLongBits(hours) != Double
				.doubleToLongBits(other.hours))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "Patient [codeDisease=" + codeDisease + ", hours=" + hours + "]";
	}
	
	
}

