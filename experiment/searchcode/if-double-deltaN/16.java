public void probkowanie(ArrayList <Double> xSygnalu, ArrayList <Double> ySygnalu){

int rozmiarSygnalu = xSygnalu.size();
int deltaN = rozmiarSygnalu / (int)iloscProbek;
for (double i=rounding(xSygnalu.get(0)); rounding(i) < rounding(xSygnalu.size()); i=i+deltaN)
{
Double tempX, tempY;
tempX = rounding(i);

for (int a=0; a < xSygnalu.size(); a++)

