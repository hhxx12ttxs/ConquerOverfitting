TreeSet<Integer> seriesIds = reverseMap.get(word);
if (seriesIds == null) {
seriesIds = new TreeSet<Integer>();
Set<Series> series = new TreeSet<Series>();
if (seriesIds == null) {
return series;
}
for (int id : seriesIds) {

