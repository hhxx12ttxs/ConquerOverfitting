public static int  mergesort(int a[],int l,int r)
{
if(l>=r)
{
return 0;
}
int mid = (l+r)/2;
int inversions=0;
int l1=l;
int r1 = mid;
int l2=mid+1;
int r2=r;
int temp[] = new int[r-l+2];
int k=0;

