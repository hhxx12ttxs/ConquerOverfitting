public Odds(int numerator, int denominator) {
super();
if (numerator < 1) {
throw new IllegalArgumentException(&quot;numerator cannot be less than one: &quot; + denominator);
}
if (denominator < 1) {

