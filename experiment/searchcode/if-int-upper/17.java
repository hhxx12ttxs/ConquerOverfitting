
public class Q30 {
void quick(int a[],int lower,int upper)
{
int loc;
if(lower<upper)
{
loc=partition(a,lower,upper);
quick(a,lower,loc-1);

