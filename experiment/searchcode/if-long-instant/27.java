Instant start = Instant.now();
long res = fibonacci(i);
Instant end = Instant.now();
System.out.println(&quot;Input: &quot; + i + &quot; Result: &quot; + res + &quot;, Time: &quot; + Duration.between(start, end).toMillis());
}

}

private static long fibonacci(int n)  {
if(n == 0)

