public abstract String calc(String formula) throws Exception;

public long faculty(long n) {
long result = 1;
if (n > 0) {
result = n * faculty(n - 1);
}
return result;
}

public double squareRoot(double n) {
double result = 0;
if (n > 0) {

