public void calculate(double x)
{
double newResult = 0;
if (view.getLastCommand().equals(&quot;+&quot;)) newResult = view.getResult() + x;
else if (view.getLastCommand().equals(&quot;-&quot;)) newResult -= x;

