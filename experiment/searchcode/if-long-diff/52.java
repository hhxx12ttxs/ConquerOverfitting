private int equi(int arr[], int n) {
if (n==0) return -1;
long sum = 0;
int i;
for(i=0;i<n;i++) sum+=(long) arr[i];

long sum_left = 0;
long diff = 1001;

