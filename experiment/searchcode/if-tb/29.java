for(TbPurchaseEntity tbPurchase:tbPurchaseList){
if(tbPurchase!=null){
//外键设置
tbPurchase.setTbPurchaseContract(tbPurchaseContract);
for(TbPurchaseEntity e:tbPurchaseList){
if(null!=e)
newMap.put(e.getId(), e);
}

for(TbPurchaseEntity e:oldList){

