package search;

public class FindMinArgs {
public static double findMin(double a, double... vars) {
double min = a;
for (double i : vars) {
if (i < min) {

