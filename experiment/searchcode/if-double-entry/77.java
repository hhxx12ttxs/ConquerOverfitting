double v = entry.getDoubleValue() - value.get(entry.getIntKey());
if (v > 0) {
result.add(entry.getIntKey(), v);
double max = Double.MIN_VALUE;
for (Int2DoubleMap.Entry entry : int2DoubleEntrySet()) {
if (entry.getDoubleValue() > maxvalue) {

