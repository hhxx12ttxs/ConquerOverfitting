return fibonacci(number - 1) + fibonacci(number - 2);
}
}

public Long fibonacciLoop(int number) {
long prev = 0;
long current = 1;
long fib = 1;

for(int i = 2; i <= number; i++) {
fib = prev + current;
prev = current;

