public class Euler109 {
public static void main (String[] args)
{
int count = 0;
for (int i = 0; i < 100; i++)
{
count += ways(i);
}
System.out.println(count);

