int j = 0;

StringBuilder output = new StringBuilder();
while(i < xa.length &amp;&amp; j < xb.length){
if(xa[i] < xb[j]){
i++;
}else if(xa[i] > xb[j]){
j++;
}else{
output.append(xa[i]);
i++;
j++;
}
}
System.out.println(output);
}
sc.close();
}
}

