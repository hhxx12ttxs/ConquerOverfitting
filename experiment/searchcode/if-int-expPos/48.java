rst = parseExp(rst, in, expPos, expSignPos, isExpNegative);
}
}
else if (exp) {
float b4exp = (float)Integer.parseInt(in.substring(0, expPos));
//System.out.println(expPos);
int pos = 0;
if (expSignPos != -1) {
pos = isExpNegative ? (-Integer.parseInt(in.substring(expSignPos + 1))) :

