public void add(double d) {
this.temp += d;
}

public Double evaluate() {
double f0 = 0;
double f1 = 1;

while(f0 <= temp || f1 <= temp) {
double temp = f1;
f1 += f0;

