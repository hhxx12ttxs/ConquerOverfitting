public static long factorial(long number) {

long result;

if ((number == 1) || (number == 0)) {
result = number * factorial(--number);
}
return result;
}

public static long fibonacci(long number) {

long result;

