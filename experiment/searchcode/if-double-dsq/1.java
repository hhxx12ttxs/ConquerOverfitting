
public class SaferQuadraticSolver {
public static void main(String[] args) {
double a = Double.parseDouble(args[0]);
double c = Double.parseDouble(args[2]);

double dsq = b * b - 4 * a * c;
if (dsq >= 0) {
double d = Math.sqrt(dsq);
if (a != 0) {

