float discount = 0.1f;
//	int discountLimit = 10000;
@Override
public int calculcate(int amount) {
/*
int n = 0;
if (amount>=discountLimit){
n = amount-(int)(amount*discount);
}else{
n = amount;
}
*/
int n = (amount>=discountLimit) ? amount-(int)(amount*discount):amount;

