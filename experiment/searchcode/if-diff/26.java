List<Diff> subDiffs = getDiffContentService().getSubDiffs(diff);
return subDiffs;
}

public Diff getRelatedDiff(Diff diff) {
if (!diff.getRefinedBy().isEmpty()) {
Diff refiningDiff = diff.getRefinedBy().get(0);
if (!refiningDiff.getRequiredBy().isEmpty()) {

