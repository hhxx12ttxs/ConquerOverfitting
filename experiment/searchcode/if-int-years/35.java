public static void main(String[] args) {
//        for (int i = 5; i > 0; i--) {
//            int years = 100 * (i - 1);
for (int i = -5; i < 6; i++) {
int years = 100 * (Math.abs(i) - 1);
if (i < 0) {
System.out.println(&quot;Century &quot; + Math.abs(i) + &quot; BC &quot; + years+&quot;-&quot;+(years+99));

