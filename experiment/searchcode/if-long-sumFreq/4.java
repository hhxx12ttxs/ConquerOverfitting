List<EnergyPoint> sampleEps = new ArrayList<>();
long cnt = 0;
double sumTime = 0;
double sumFreq = 0;
sumEnergy += Double.parseDouble(rawArr[2]);
if (cnt % 10 == 0) {
sampleEps.add(new EnergyPoint(sumTime / 10, sumFreq / 10, sumEnergy / 10));

