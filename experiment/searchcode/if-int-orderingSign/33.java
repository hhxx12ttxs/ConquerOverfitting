SpacecraftState state = interpolator.getInterpolatedState();

// evaluate step size
final double stepSize;
if (getMode() == MASTER_MODE) {
// chronological or reverse chronological sorter, according to propagation direction
final int orderingSign = interpolator.isForward() ? +1 : -1;
final Comparator<EventState<?>> sorter = new Comparator<EventState<?>>() {

