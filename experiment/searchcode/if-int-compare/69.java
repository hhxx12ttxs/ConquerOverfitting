public class GameReleaseDateComparator implements Comparator<Game> {

@Override
public int compare(Game lhs, Game rhs) {
compare = -1;
} else if (rel2.getYear() != 0) {
compare = rel1.compareTo(rel2);
if (compare == 0) {
int q1 = lhs.getExpectedReleaseQuarter();

