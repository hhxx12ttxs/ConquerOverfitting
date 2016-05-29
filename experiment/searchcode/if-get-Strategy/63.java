public DiscountContext(DiscountStrategy discountStrategy) {
super();
if(discountStrategy == null){
this.discountStrategy = new MemberDiscount(); //默认为普通会员打折率
this.discountStrategy = discountStrategy;
}

public double getDiscountPrice(double orginalPrice){
return this.discountStrategy.getDiscountPrice(orginalPrice);
}
}

