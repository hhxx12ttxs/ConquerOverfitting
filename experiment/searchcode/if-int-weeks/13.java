private int beginInterval;// 开始上课节数
private int endInterval;// 结束节数
private int[] weeks = null;// 上课周数
public void setWeeks(int[] weeks, boolean isSetBinary) {
this.weeks = weeks;
if (!isSetBinary) {
return;
}
int i;
this.weeksBinary = 0;

