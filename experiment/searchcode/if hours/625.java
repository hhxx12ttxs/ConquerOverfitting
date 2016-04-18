//Data represents GPA/hours
public class Data {

	//declare variables
	private int studentCreditHours;
	private double studentGPA;
	//holds the actual type of this
	private Type type;
	
	//use enum to determine type of student info either GPA or hours
	enum Type {GPA, HOURS}

	//sets student credit hours
	public void setStudentCreditHours(int studentCreditHours) {
		this.studentCreditHours = studentCreditHours;
		//set the type to HOURS
		type = Type.HOURS;
	}
	//Set student GPA
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
	//get student creditHours
	public int getCreditHours(){
		return studentCreditHours;
	}
	//get student GPA
	public double getGPA(){
		return studentGPA;
	}
}

