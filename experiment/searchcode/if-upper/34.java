public class PrimeNumbers {
private int upperBound;
private StackOfIntegers primeNumbers;

PrimeNumbers() {
this(120);
for (int i = 0; i < upperBound; i++) {
if (isPrime(i))
primeNumbers.push(i);
}
primeNumbers.reverse();

