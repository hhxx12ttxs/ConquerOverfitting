double actMoney = 0;
List<TbConCz> where = TbConCzDao.where(sql);
if(where!=null &amp;&amp; where.size()>0){
for (TbConCz tbConCz : where) {
conMoney += tbConCz.getCon_total_price();
TbConAddcon supplyByCz = supplyService.getConSupplyByCz(conId);
if(supplyByCz!=null){

