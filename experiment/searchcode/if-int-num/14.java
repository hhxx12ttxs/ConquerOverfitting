public class UglyNumber {
public boolean isUgly(int num) {
while (num % 2 == 0)
while (num % 5 == 0)
num = num / 5;
if(num == 1)
return true;
return false;

}
}

