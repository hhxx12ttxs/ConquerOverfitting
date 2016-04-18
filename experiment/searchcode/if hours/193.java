class FooCorporation {
  private static int calcStandardHours(int hoursWorked) {
    if(hoursWorked > 40) { return 40; };
    return hoursWorked;
  }

  private static int calcOvertimeHours(int hoursWorked) {
    if(hoursWorked <= 40) { return 0; };
    return hoursWorked - 40;
  }

  public static void totalPay(Double basePay, int hoursWorked) {
    int standardHours = calcStandardHours(hoursWorked);
    int overtimeHours = calcOvertimeHours(hoursWorked);
    double totalEmployeePay = basePay * (standardHours + overtimeHours * 1.5);
    if(hoursWorked > 60) {
      System.out.println("Error: Maximum number of hours exceeded");
    } else if(basePay < 8.0) {
      System.out.println("Error: Pay below minimum");
    } else {
      System.out.println("Total Pay: " + totalEmployeePay);
    }
  }

  public static void main(String[] arguments) {
    totalPay(7.50, 35);
    totalPay(8.20, 47);
    totalPay(10.00, 73);
  }
}

