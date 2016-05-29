public class ActorComparator implements Comparator < Actor > {
@Override
public int compare(Actor arg0, Actor arg1) {
if (arg0.getZIndex() < arg1.getZIndex()) {
return -1;
} else if (arg0.getZIndex() == arg1.getZIndex()) {

