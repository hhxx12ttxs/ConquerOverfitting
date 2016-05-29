for (String token : tokens) {
String normalized = OrdinalValuesNormalization.getNormalizeOrdinalValues(token);
if(normalized != null) {
String normalizeStreet = StreeNameNormalizerMap.checkValue(token);
if(normalizeStreet != null) {
address += normalizeStreet+&quot; &quot;;
}
else{

