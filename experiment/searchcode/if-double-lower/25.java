public class P235 {
public static void main(String args[]) {
double lower = 1;
double upper = 1.01;
double r = 1;
while (true) {
double middle = (lower + upper) / 2;
if (Math.abs(middle - r) < 5E-13) {

