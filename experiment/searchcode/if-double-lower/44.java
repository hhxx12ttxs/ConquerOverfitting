if(value > upper) return upper;
if(value < lower) return lower;
return value;}

public static double bracketDouble(double value, double lower, double upper){
if(upper < lower) return 0d;
if(value > upper) return upper;

