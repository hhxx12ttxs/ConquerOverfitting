private Double compute(Double[] record) {

Double hTheta = theta0;
for (int fieldIndex = 0; fieldIndex < record.length; fieldIndex++) {
theta0 += alpha * delta;
for (int fieldIndex = 0; fieldIndex < thetas.length; fieldIndex++) {

