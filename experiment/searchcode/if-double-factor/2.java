public static void main(String[] args)
{
double x = 600851475143.0;

int factor = 2;
while(x > 1)
{
if(x % factor == 0)
{
x /= factor;
if(x > 1)
factor = 2;
}
else
factor++;
}
System.out.println(factor);
}
}

