final XYSeriesCollection dataset = new XYSeriesCollection();

int opcao  = 2;
int entrada  = 3;
if (opcao==1) {
final XYSeries op1 = new XYSeries(&quot;f(n)=K&quot;);
op1.add(x, entrada);
}
dataset.addSeries(op1);
}

if (opcao == 2) {
final XYSeries op2 = new XYSeries(&quot;f(n)=n&quot;);

