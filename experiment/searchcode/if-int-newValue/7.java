int[] lastTwo = new int[2];
int newValue = 0;
int sum = 2;
lastTwo[0] = 1;
lastTwo[1] = 2;
while (newValue <= 4000000) {
lastTwo[1] = newValue;
if (newValue%2 == 0) {
System.out.println(&quot;Somme: &quot; + sum + &quot; even: &quot; + newValue);

