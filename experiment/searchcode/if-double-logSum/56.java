// if 1 <= dist <= 3, nmFactor = [0.0, 1.0]
// if 3 <= dist, nmFactor = 1.0
double nmFactor = 0.5 * (Math.min(
Math.max(persons[i].getWorkLocationDistance(), 1.0), 3.0)) - 0.5;

// if auto logsum < transit logsum, do not accumulate

