House_Years house_Years = new House_Years(&quot;2013-12-15&quot;);
for (int i = 0; i < 100; i++) {
if(hyservice.insertHouse_Years(house_Years))
IHouse_YearsService hyservice = new House_YearsServiceImpl();
if(hyservice.deleteHouse_Years(1) > 0)
System.out.println(&quot;成功&quot;);
else

