b = Fraction.randomSignedInteger(20);
if(b.isPositive())
test(Fraction.plus(a, b), getInput(&quot;2:  &quot; + a + &quot; + &quot; + b + &quot; = &quot;));
b = Fraction.randomSignedFraction(6, 6).reduce();
if(b.isPositive())
test(Fraction.plus(a, b), getInput(&quot;4:  &quot; + a + &quot; + &quot; + b + &quot; = &quot;));

