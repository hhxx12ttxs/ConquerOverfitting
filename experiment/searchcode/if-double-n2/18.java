import servicos.interfaces.ICalculadora;
public class Calculadora implements ICalculadora{

@Override
public double Soma(double n1, double n2) {
@Override
public double Divisao(double n1, double n2) throws MyException {
if(n2 <= 0)
throw new MyException(&quot;Não é possivel realizar divisão por 0&quot;);

