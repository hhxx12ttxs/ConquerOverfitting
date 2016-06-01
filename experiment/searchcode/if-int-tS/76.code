class MultiWriter {
class MRSW {
public int val = 0, ts = 0, pid = 0;
public synchronized void setValue(int x, int seq, int id) {
public void setValue(int w, int x) { // writer w
int maxseq = V[0].ts;
for (int i = 1; i < n; i++)
if (maxseq < V[i].ts) maxseq = V[i].ts;

