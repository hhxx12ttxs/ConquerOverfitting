public BlockCluster(Cluster rowCluster, Cluster colCluster, int blockRows,
int blockCols) {
this.rowCluster = rowCluster;
this.colCluster = colCluster;
int blockRows = row.getNrOfSons();
int blockCols = col.getNrOfSons();

if (blockRows > 0 &amp;&amp; blockCols > 0) {

