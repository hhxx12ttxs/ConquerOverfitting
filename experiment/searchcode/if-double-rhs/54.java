public Number add( Value rhs )
{
if (rhs instanceof IntVal) {
return new DoubleVal(val + ((IntVal)rhs).val);
}
else if (rhs instanceof DoubleVal) {
return new DoubleVal(val + ((DoubleVal)rhs).val);
}
else

