
public class PiCalculator {

static double Pi(){

double pi=4;
int step=1;
for (int i=3; i<400000;i+=2) {//400.000 değeri nekadar artarsa hassasiyet okadar artar.
pi-=(double)4/i;
i+=2;
pi+=(double)4/i;
step+=1;
System.out.println(pi);
if (pi>3.14158 &amp;&amp; pi<3.14160) {//istenilen pi değerine ulaşıldığında döngüden çıkar

