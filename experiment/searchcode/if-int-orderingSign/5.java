final double convergence,
final int maxIterationCount) {
addEventHandler(handler, maxCheckInterval, convergence,
// search for next events that may occur during the step
final int orderingSign = interpolator.isForward() ? +1 : -1;
SortedSet<EventState> occuringEvents = new TreeSet<EventState>(new Comparator<EventState>() {

