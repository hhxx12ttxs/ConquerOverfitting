@Override
public boolean isCompatible(Signal signal, int scale) {
if (signal.getDimension() < 1 || signal.getDimension() % 2 != 0) {
int N = Math.round((float)signalDimension / 4);
int M = Math.min((int)Math.floor(Math.log(signalDimension) / Math.log(1.1)), N);

// If M is odd, make it even

