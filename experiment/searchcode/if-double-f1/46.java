public static Function add(Function f1, Function f2){
if(f1 instanceof Polynomial &amp;&amp; f2 instanceof Polynomial){
Polynomial p1 = (Polynomial)f2, p2 = (Polynomial)f2;
return Polynomial.add(p1, p2);
} else{
if(f1 instanceof Transcendental &amp;&amp; f2 instanceof Transcendental){

