System.out.println(new ZigZagConversion().convert(&quot;PAYPALISHIRING&quot;, 4));
}

public String convert(String s, int nRows) {
if (s == null || s.length() <= 1 || nRows <= 1) {
int length = s.length();
int u = nRows * 2 - 2;
int a = length % u;
a = a > nRows ? (a - nRows + 1) : 1;

