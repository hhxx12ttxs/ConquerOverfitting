int nearConnectColumns = 0;
int lastConnectColumn = -1;
int startColumn = -1;
for (int k = 0; k < W; ++k) {
if (map[i][k] == &#39;.&#39; || map[j][k] == &#39;.&#39;) {
if (startColumn != -1) {
int add = (connectColumns * (connectColumns - 1) / 2) - nearConnectColumns;

