int flag = x&amp;0x80000000;
int result = 0;
x = Math.abs(x);

while(x!=0){
result *= 10;
result += x%10;
x /= 10;
}
if(flag!=0)
result = 0 - result;
return result;
}
}

