public Epcie(String threshold) {
this.threshold = Double.parseDouble(threshold);  // Set threshold
GaGeneratorList = new ArrayList<GaGenerator>();  // Record GA structure
GaGenerator GA = new GaGenerator(i);  // Multiple GA at layer i

if (i == 1) GA.buildFirstGaList();
else {
Boolean flag = GA.buildHGA(GaDbnList.get(i - 2), GaGeneratorList.get(i - 2), GaEscList.get(i - 2), retrain);

