insertSet.addAll(server.inserts);
insertSet.addAll(local.inserts);
for (LineDiff.Diff diff : insertSet) {
if (local.deletes.contains(new LineDiff.Diff(LineDiff.Operation.DELETE, diff.text))) {

