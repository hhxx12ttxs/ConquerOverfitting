for (int i=0;i<config.TOTAL_CPUs;i++){
double tempCoefficient = 1;
double currentTemp = s.getCpuById(i).getTemp();
if (currentTemp==0){
double rangeMax = 1.2;
double rangeMin = 0.8;
double randomValue = rangeMin + (rangeMax - rangeMin) * r.nextDouble();

