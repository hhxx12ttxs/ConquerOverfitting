arr[node] = Math.min(arr[node * 2], arr[node * 2 + 1]);
}

public int query(int node, int l, int r, int lowerBound, int upperBound) {
if (r < l || l > upperBound || r < lowerBound)
public void update(int node, int l, int r, int lowerBound, int upperBound,
int val) {
if (l > upperBound || l > r || r < lowerBound)
return;
if (l == r) {

