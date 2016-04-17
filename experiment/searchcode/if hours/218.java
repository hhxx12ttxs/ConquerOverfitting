class PagoSemana { 
public static void pay(double basePay, int hours) { 
if (basePay < 8.0) {
System.out.println("Debe pagar al menos 8 horas");
} else if (hours > 60) {
System.out.println("No puede trabajar mas de 60 horas semanales");
} else {

int overtimeHours = 0;

if (hours > 40) {

overtimeHours = hours - 40;  
hours = 40; 
}
double pay = basePay * hours;
pay += overtimeHours * basePay * 1.5;
System.out.println("Pago del empleado $" + pay); 
}
} 
public static void main(String[] arguments) { 
pay(7.5, 35);
pay(8.2, 47);
pay(10.0, 73); 
}
}
