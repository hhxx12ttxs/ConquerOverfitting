package com.july;

public class PowerOfTwo {

public boolean isPowerOfTwo(int n) {
long res = 1;
while (res <= n) {
if (res == n)
return true;
res *= 2;
}
return false;
}

public static void main(String[] args) {

