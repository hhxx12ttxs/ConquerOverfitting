Calculadora multiplica = (n1, n2) -> n1 * n2;
Calculadora division = (n1, n2) -> {
if (n2 == 0) {
return 0;
System.out.println(&quot;DIV= &quot; + divisionCorta.operacion(x, y));
}

public interface Calculadora {
double operacion(double n1, double n2);
}
}

