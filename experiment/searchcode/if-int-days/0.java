public class Problem19
{
public static void main(String[] args)
{
int days = 1;
int sundays = 0;
for(int year = 1901; year<=2000; year++)
{
days++;		//jan
if(days%7==0)
sundays++;

