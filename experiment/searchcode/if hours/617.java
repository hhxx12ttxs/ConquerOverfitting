//Data represents GPA/hours
public class Data {

	private int studentCreditHours;
	private double studentGPA;
	//holds the actual type of this
	private Type type;
	
	//used to know witch type of data this will hold
	enum Type {GPA, HOURS}

	public void setStudentCreditHours(int studentCreditHours) {
		this.studentCreditHours = studentCreditHours;
		//set the type to HOURS
		type = Type.HOURS;
	}

	public void setStudentGPA(double studentGPA) {
		this.studentGPA = studentGPA;
		//set the type to GPA
		type = Type.GPA;
	}	
	
	//returns a description of this object
	public String toString(){
		if( type == Type.GPA )
			return "GPA: " + studentGPA;
		else
			return "Hours: " + studentCreditHours;
	}
}

