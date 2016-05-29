public DIFF diff(DATA from, DATA to) {
if (from == null) {
return createDiff(Diff.Type.CREATED, from, to);
}
if (to == null) {
return createDiff(Diff.Type.DROPPED, from, to);

