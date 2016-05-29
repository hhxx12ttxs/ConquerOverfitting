public DiscountContext(DiscountStrategy strategy)
{
this.strategy = strategy;
}

// 根据实际所使用的DiscountStrategy对象得到折扣价
public double getDiscountPrice(double price)
{
// 如果strategy为null，系统自动选择OldDiscount类
if (strategy == null)
{
strategy = new OldDiscount();

