System.out.print(&quot;Enter r2&#39;s center x-, y-coordinates, width, and height: &quot;);
NewRectangle r2 = readRect(s);

if (r1.equals(r2)) {
System.out.println(&quot;r1 and r2 are overlapped (r1, r2 are same)&quot;);
} else if (r1.in_the(r2)) {
System.out.println(&quot;r1 in the r2&quot;);
} else if (r2.in_the(r1)) {

