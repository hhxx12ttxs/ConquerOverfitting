} catch (InputMismatchException e) {
vi = false;
}
scan.nextLine();

if (input > 0 &amp;&amp; vi) {
double dsq = Math.sqrt(input);
int isq = (int) Math.round(dsq);
if (dsq == isq) {
System.out.println(input + &quot; is the square of &quot; + isq + &quot;.&quot;);

