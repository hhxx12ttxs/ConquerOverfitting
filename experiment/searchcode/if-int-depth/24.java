public class Recursion {

public static int fibonacci(int depth) {
if (depth == 0) return 0;
if (depth == 1) return 1;

return (fibonacci(depth - 1) + fibonacci(depth - 2));

