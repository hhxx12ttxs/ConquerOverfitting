public static Double addDouble(Double a, Double b) {
if(a==null){
a = 0.0d;
}

if(b==null){
b = 0.0d;
}
Double c = a + b;
Double f1 = bc.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
return f1;
}

public static Double getNoFloat(Double a) {
if(a==null){
a = 0.0d;

