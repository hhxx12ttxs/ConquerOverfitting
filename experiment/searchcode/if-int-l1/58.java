public class HammingLoss implements ILoss {
public double calc(List l1, List l2) {
int ne = 0;
for(int i=0; i<l1.size(); i++) {
if (!l1.get(i).equals(l2.get(i)))
public double calc(int[] l1,int[] l2) {
int ne = 0;
for(int i=0; i<l1.length; i++) {
if (l1[i] != l2[i])
ne++;
}
return ne;

