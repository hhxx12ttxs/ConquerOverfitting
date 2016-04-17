class Employee {
  
  public static int topSalary = 195000;
  int hoursPerWeek;

  public static void setTopSalary (int s) {
     if (s > topSalary)  
       topSalary = s;   
  }  
                                 
 public void addMoreHours () {
     hoursPerWeek++; 
  }
    
public static void main(String[] args){
  Employee e1, e2;
  e1 = new Employee();
  e2 = new Employee();
  
  Employee.setTopSalary(199001);     
  e1.hoursPerWeek = 40;
  e2.hoursPerWeek = 45;
  System.out.println(e1.hoursPerWeek);
  System.out.println(e2.hoursPerWeek);
  System.out.println(Employee.topSalary);
  }
}
