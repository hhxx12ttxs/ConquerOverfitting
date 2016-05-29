int seq = 400;

for(int i=30;i>=0;i--){
Gap2Calc gap2=new Gap2Calc();
ArrayList<CalcVO> gap2Result = gap2.calc(list, 1, seq, i);
System.out.println(gap2Result.get(5).get(Gap2Calc.GAP));

