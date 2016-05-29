this.discountStrategy = discountStrategy;
}

// 根据实际所选择的打折策略返回不同的折后价
public double getDiscountPrice(double price) {
// 如果没有主动选择打折策略，则使用旧版本的打折策略
if(discountStrategy == null) {
discountStrategy = new OldDiscount();

