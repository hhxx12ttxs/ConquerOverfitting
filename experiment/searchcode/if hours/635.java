public class Employee {

  private static int topSalary = 195000;
  private int hoursPerWeek;

  private static void setTopSalary(int s) {
    if (s > topSalary)
      topSalary = s;
  }

  public Employee(int i) {
    hoursPerWeek = setHoursPerWeek(i);

  }

  public static void main(String[] args) {
    Employee e1, e2;
    e1 = new Employee(40);
    e2 = new Employee(45);

    Employee.setTopSalary(199000); 
    Employee.setTopSalary(199001); 
    e1.hoursPerWeek = 40;
    e2.hoursPerWeek = 45;

    System.out.println("Employee 1 works " + e1.hoursPerWeek + " hours per week.");
    System.out.println("Employee 2 works " + e2.hoursPerWeek + " hours per week.");
    System.out.println("The top salary at this company is $" + topSalary + ".");

  }

  public int getHoursPerWeek() {
    return hoursPerWeek;
  }

  public int setHoursPerWeek(int hoursPerWeek) {
    return this.hoursPerWeek = hoursPerWeek;
  }

  public static int getTopSalary() {
    return topSalary;
  }

}

