public ModelAndView projectTrackingDetail(TbPurchaseEntity tbPurchase, HttpServletRequest req) {
if (StringUtil.isNotEmpty(tbPurchase.getId())) {
tbPurchase = tbPurchaseService.getEntity(TbPurchaseEntity.class, tbPurchase.getId());
Number = Integer.parseInt(tbPurchase.getNumber());
}
double UnitPrice=0;

if(tbPurchase.getUnitPrice()!=null&amp;&amp;!&quot;&quot;.equals(tbPurchase.getUnitPrice())){

