
public class Tree {


public int minDepth(Tree t,int depth) {

if (t.left != null) {
int left = minDepth(t.left,depth++);
} else {
left = depth;
}

if (t.left != null) {
int right = minDepth(t.right,depth++);

