double v2 = Dataset.toDouble(o2);
if(v1>v2) {
return 1;
for(Object value2 : flatDataCollection) {

double v;
String str = value2.toString();
if(str.endsWith(CensoredDescriptives.CENSORED_NUMBER_POSTFIX)) {

