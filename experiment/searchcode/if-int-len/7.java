// !! n >= 0 !!
// if n==0 then 1
// else 12345->5
int digLen(long n) {
int len = 1;
while (n >= 10) {
len++;
n /= 10;
}
return len;
}

