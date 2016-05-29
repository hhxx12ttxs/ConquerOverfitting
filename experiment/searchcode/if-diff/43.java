public void seprateDiffs(LinkedList<Diff> diffs) {

for (Diff diff : diffs) {

if (diff.operation == Operation.EQUAL) {
addToList(diff, sourceDiff);
addToList(diff, targetDiff);
} else if (diff.operation == Operation.INSERT) {

