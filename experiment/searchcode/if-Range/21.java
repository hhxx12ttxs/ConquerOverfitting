List<Range> rangeList1 = new ArrayList<>();
for (Range range : rangeList) {
if (rangeList1.size() == 0) {
rangeList1.add(range);
} else {
if (range.getStart() <= rangeList1.get(rangeList1.size() - 1).getEnd()) {

