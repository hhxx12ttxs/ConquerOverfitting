public double doPID(double value) {
double error = setpoint - value;
double correction;

if(firstRun) {
// Only cap output if capOutput() function has been called
if(min != max) {
correction = MathX.clamp(correction, min, max);

