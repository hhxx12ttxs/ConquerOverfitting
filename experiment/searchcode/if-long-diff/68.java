for(int j=i+1;j<5000;j++){
long l=pgs.get(j);
if(num==l)continue;
long sum=l+num;
long diff=l-num;
if(diff<0)diff=diff*-1;
if(pgs.contains(sum)&amp;&amp;pgs.contains(diff))
{
if(min>diff){
min=diff;
}
System.out.println(l+&quot;,&quot;+num+&quot;, diff=&quot;+min);
}
}

}

}
}

