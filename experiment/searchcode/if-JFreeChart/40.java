public static String creatChart(List<DataInfo> datas,ChartType type,ChartBaseInfo info,HttpServletRequest request){
JfreechartSetting setting = new DefaultCategorySetting();
if(type.equals(ChartType.PIE)){
JFreeChart jFreeChart = null;
switch (type) {
case BAR:
if(info.isIs3D()){
jFreeChart =  ChartFactory.createBarChart3D(info.getTitle(), info.getCategoryAxisLabel(), info.getValueAxisLabel(), new CategoryDataset(datas), PlotOrientation.VERTICAL,

