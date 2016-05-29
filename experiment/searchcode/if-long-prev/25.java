super(20, &quot;Compute the Fibonacci number. Fib(n)=Fib(n-1)+Fib(n-2), where Fib(0) = 0 and Fib(1)=1. E.g. &#39;4&#39;=>&#39;3&#39;&quot;);
}

long fibonacci(int num) {
if (num == 0) return 0;
if (num == 1) return 1;

long prevPrev = 0;
long prev = 1;
long current = 1;

