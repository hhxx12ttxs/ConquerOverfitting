public double calcAmount(int days) {
double amount = 2;
if (days > 2) {
amount += (days - 2) * 1.5;
return super.calcPoints(days) + (days > 1? 1: 0);
}
},
Childrens {
@Override
public double calcAmount(int days) {
if (days > 3) {

