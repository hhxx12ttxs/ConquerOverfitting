this.step = deltaStep;
}

private void changeStep(double previousError, double currentError) {
if(decreases) {
if(1 != step) {
if(previousError > currentError) {
step += deltaStep;

