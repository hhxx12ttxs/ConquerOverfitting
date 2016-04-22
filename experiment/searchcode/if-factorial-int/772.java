#include<iostream.h>
#include<conio.h>

long factorial (long a)
{
	if (a>1)
		return (a* factorial (a-1));
	else
		return (1);
}
int main()
{
	long 1;
	cout << "Tuliskan bilangan : ";
	cin >> 1;
	cout << "1" << 1 << " = " << factorial(1);
	return 0;
}

Baldwin _ 6706144064 ;
