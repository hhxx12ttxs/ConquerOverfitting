long a = Math.abs((long)dividend);
long b = Math.abs((long)divisor);
long result = 0;
while (a >= b){
result += 1 << i;
i ++;
}
}
if ((dividend < 0 &amp;&amp; divisor > 0) || (dividend > 0 &amp;&amp; divisor < 0))
result = -result;
return (int)result;
}
}

