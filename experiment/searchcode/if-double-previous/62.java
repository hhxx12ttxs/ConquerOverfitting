System.out.println(&quot;Введіть x&quot;);
Scanner scanner = new Scanner(System.in);
double x = scanner.nextDouble();

if (x <= -1.0 || x >= 1.0) {
return;
}

double previousX = 1;
double sum = 1;

while (true) {

