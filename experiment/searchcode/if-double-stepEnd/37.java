if (sim == null) {
stepEnd = stepAmount;
runSim();
}
else {
if (!paused) {
return;
}
paused = false;
pauseSem.release();
if (stepEnd < 0)

