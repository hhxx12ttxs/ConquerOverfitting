public int countEven() {
int count = 0;
for (int number : numbers) {
if (number % 2 == 0) count++;
for (int number : numbers) {
if (number % 2 == 1) count++;
}
return count;

