import java.util.*;

class Solution {
int divide(int numerator, int denominator) {
if (denominator==0) {
return 0;
}
int result = 0;

numerator = Math.abs(numerator);
denominator = Math.abs(denominator);

