Unless the two values are the same, then
return double their sum.

sumDouble(1, 2) → 3
sumDouble(3, 2) → 5
sumDouble(2, 2) → 8

*/

class sumDouble {
public static int sumDouble(int a, int b) {
if (a == b) { return 2*(a+b);}
return a+b;
}


}

