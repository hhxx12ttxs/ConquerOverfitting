public class Solution {
public int addDigits(int num) {
if(num < 0) return 0;


while((num / 10) > 0) {
int sum = 0;
while(num > 0){

