serialize(categoryPlot, jgen);

List<String> categoryNames = categoryPlot.getCategoryNames();
if (categoryNames != null) {
List<CategoryGraphics> categoryGraphicsList = categoryPlot.getGraphics();
if (categoryGraphicsList != null) {
for (CategoryGraphics categoryGraphics : categoryGraphicsList) {

