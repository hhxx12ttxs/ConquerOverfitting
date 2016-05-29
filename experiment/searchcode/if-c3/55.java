Complex c1, c2, c3;
double d;
@Before
public void setUp() throws Exception {
}

@Test
c1 = new Complex(1.0, 3.0);
c2 = new Complex(-2.0, 5.0);

c3 = new Complex(c1.getRe(), c1.getIm());

if(c1 == c3)

