double nxtArrival = model.getClock() + interArrDistFP.nextDouble(1.0 / mean);
if (nxtArrival > model.closingTime) {
nxtArrival = -1.0; // Ends time sequence
double mean = getMeanTimeGI();
double nxtArrival = model.getClock() + interArrDistGI.nextDouble(1.0 / mean);
if (nxtArrival > model.closingTime) {

