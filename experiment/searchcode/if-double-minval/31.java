public <T extends Number> Range(T min, T max) throws InvalidFormatException{
double minVal = min.doubleValue();
double maxVal = max.doubleValue();
if(min == max){
throw new InvalidFormatException();
} else if(minVal > maxVal){
this.min = maxVal;

