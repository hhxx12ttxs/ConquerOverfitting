
double getPrice( void )
{
return basePrice() * discountFactor();
}

private int basePrice( void )
{
return _quantity * _itemPrice;
}

private double discountFactor( void )
{
double  discountFactor;

