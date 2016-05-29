public Double not(Double a) throws ExpressionExceptions {
throw new ExpressionExceptions(&quot;you cannot use ~ operation with double type, sorry&quot;);
}
public Double getNumber(String str) {
return new Double(str);
}
public Double abs(Double a) {
if ( a < 0)
a = -a;
return a;
}
}

