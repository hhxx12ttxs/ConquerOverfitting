public static long solution_1(int upperLimit) {
long pre_prev = 1, prev = 2, next = 1,  sum = 0;
sum = prev;
while ((next = pre_prev + prev) < upperLimit) {
pre_prev = prev;
prev = next;
if ((next % 2) == 0) {

