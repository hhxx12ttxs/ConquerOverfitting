this.strategy = strategy;
}
//根据实际所使用的DiscountStrategy对象得到折扣价
public double getDiscountPrice(double price){
if(strategy==null)
strategy=new OldDiscount();
return this.strategy.getDiscount(price);
}

//提供切换算法的方法
public void changeDiscount(DiscountStrategy strategy){

