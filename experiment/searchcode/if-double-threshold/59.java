public static void main(String[] args) {
Double currentValue = 18.7d;
Double thresholdMin = -2d;
Double thresholdMax = 15d;
if(currentValue.compareTo(thresholdMin) >=0  &amp;&amp; currentValue.compareTo(thresholdMax)<=0){

Double midRange = (thresholdMax - thresholdMin)/2;
if(currentValue.compareTo(midRange) >= 0){

