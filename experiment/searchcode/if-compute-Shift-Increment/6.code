for (Entry<E> match : get(search.getX(), search.getY())) {
Coordinate value = match.value;
if (computeOrthodromicDistance(location, value) <= distance) {
calc.setDirection((distance < 0) ? 180 : 0, Math.min(Math.abs(distance), HALF_WAY_AROUND_THE_WORLD));

// If the resulting point is in the opposite hemisphere, then shift has wrapped around

