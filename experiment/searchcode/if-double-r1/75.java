Object r1 = getParameterList()[0].returns();
Object r2 = getParameterList()[1].returns();

if (r1 instanceof Double &amp;&amp; r2 instanceof Double) {
return operate( (Double) r1, (Double) r2 );

