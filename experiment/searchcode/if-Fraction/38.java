Fraction f = new Fraction(3,10);
Fraction g = new Fraction(1,2);
Fraction h = new Fraction(3,5);
if (!f.equals(g.multiply(h))) System.out.println(&quot;Multiply failed&quot;);
// extend with extra tests
}

static void test(Fraction f1, Fraction f2, String msg){
if (! f1.equals(f2))
System.out.println(msg);
}
}

