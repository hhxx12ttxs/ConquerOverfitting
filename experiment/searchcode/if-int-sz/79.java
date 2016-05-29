
public class PathCompressedQuickUnionUF {

private int id[];
private int sz[];

public PathCompressedQuickUnionUF(int N)
public void union(int x, int y){

int rootx = root(x);
int rooty = root(y);
if(sz[rootx]<sz[rooty]){

