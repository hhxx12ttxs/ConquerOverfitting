public class Inventory {
public int monthlyOrder(int[] sales, int[] daysAvailable){
int n = daysAvailable.length;
double items = 0;
int months = 0;
for(int i=0;i<n;i++){
if(daysAvailable[i]!=0){

