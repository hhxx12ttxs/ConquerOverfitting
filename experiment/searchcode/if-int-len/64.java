public int sumLimit(int a, int b) {
String a = a + &quot;&quot;;
int lenA = a.length();
int sum = a + b;
String sumString = sum + &quot;&quot;;
int lenS = sumString.length();
if (lenA == lenS) {
return sum;
}
return a;
}

