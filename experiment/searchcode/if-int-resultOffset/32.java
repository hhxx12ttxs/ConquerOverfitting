final DSCompiler[][] newCache = new DSCompiler[maxParameters + 1][maxOrder + 1];

if (cache != null) {
// preserve the already created compilers
for (int i = 0; i < cache.length; ++i) {
public void pow(final double[] operand, final int operandOffset, final int n,
final double[] result, final int resultOffset) {

if (n == 0) {

