static int count=1;

static ArrayList<Fraction> fract_list = new ArrayList<Fraction>();




public static void main(String[] args)
public void generateFractionsRecursively(Fraction f)
{

if(count<100)
{

Fraction fnew = new Fraction(Fraction.ONE, new Fraction(Fraction.ONE, f).reciprocate());

