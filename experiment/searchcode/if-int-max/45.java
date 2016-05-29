int len = A.length;
int max = 0;
for(int i = 0; i < len &amp;&amp; i <= max; i++){
max = Math.max(max, i + A[i]);
if(max >= len - 1)  return true;
}

return false;
}
}

