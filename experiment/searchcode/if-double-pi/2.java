package neustadt.PI;

public class CalculatePi {

public double calculate(long iterations) {
double pi = 0;
double numerator =4;
double denominator =1;
boolean positive = true;

for (int i = 1; i < iterations; i++) {

