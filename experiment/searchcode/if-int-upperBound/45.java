package Chap12;

public class Q07 {
int squareRoot(int val) {
if (val == 0 || val == 1)
return val;

int max = (int)Math.sqrt(Integer.MAX_VALUE);
int upperBound = val / 2 > max ? max : val / 2;

