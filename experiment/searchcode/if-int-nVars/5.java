public class BigTruthTable implements Stateful {

/**
*
*/
public int nvars;

/**
*
*/
public BigInteger tt;
private static BigInteger nandeval1(int nvars,BigInteger N,BigInteger M,BigInteger B) {
BigInteger r;
if(B.compareTo(N)<0) {
int b=B.intValue();
r=    lvar2bigint(nvars,b);

