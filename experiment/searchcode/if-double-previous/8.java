package talkhouse;

public class DoubleDeltaFilter {
double[][] previous;

public DoubleDeltaFilter() {
public double[][][] apply(double[][] data) {
if (previous == null) {
previous = new double[3][];
for (int i=0; i<previous.length; ++i) {

