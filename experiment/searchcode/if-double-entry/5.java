private void update(Double key, Double value, double nof){
Double v = distr.get(key);
if(v == null){
distr.put(key, value / nof);
}
else{
public void update(Map<Double,Double> d){
nofDistrInside++;
for(Map.Entry<Double, Double> entry : d.entrySet()){
update(entry.getKey(), entry.getValue(), nofDistrInside);

