public interface DayOfMonth {

enum Shorthands implements DayOfMonth {
LAST {
@Override
public int getDayNumber() {
return 0;
}
},
PENULTIMATE {
@Override
public int getDayNumber() {

