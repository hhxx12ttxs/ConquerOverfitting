import java_algorithm.equation.algebraic.LU;

/**
*  行列の逆行列を求める(LU 法)
*/
public class LUMatInv {
private LUMatInv() {}
public static double inverse(double[][] a, double[][] a_inv) {
int n = a.length;
int[] ivec = new int[n];

