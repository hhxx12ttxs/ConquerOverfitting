public static int test(){
int i = 0;
long l1 =  1073741824L;
long l2 = 2147483648L;
if(l1 % l2 != 1073741824L) i = i + 1;
l1 = -l1;
if(l1 % l2 != -1073741824L) i = i + 2;
l2 = -l2;

