c.add(Calendar.DAY_OF_MONTH,1);
Calendar c3 = Calendar.getInstance();
c3.set(Calendar.YEAR,c.get(Calendar.YEAR));
c3.set(Calendar.MONTH,c.get(Calendar.MONTH)+1);
System.out.println(c3.getTime());

if(c.MONTH!=c2.MONTH){

System.out.println(&quot;当前月的最后一天是：&quot;+c.getTime());

