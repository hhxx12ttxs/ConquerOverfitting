String sql) {
String tbName_ = null;
int tbIndexInDb = -1;
if (consistent) {
/* 计算平均每个数据库的表的数量 */
int tbSizeInDb = tbSize / dbSize;
tbIndexInDb = dbIndex * tbSizeInDb + tbIndex;
} else {
tbIndexInDb = tbIndex;
}
if (tbIndexInDb < 10) {

