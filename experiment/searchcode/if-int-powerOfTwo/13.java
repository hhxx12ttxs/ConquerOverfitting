package PowerofTwo;

public class PowerofTwo {
public boolean isPowerOfTwo(int n) {

int count = 0;

if(n < 0){
return false;
}

while(n != 0){
int temp = n &amp; 1;

