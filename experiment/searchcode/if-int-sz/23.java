* Created by Anton Kosyakin on 10.04.2015.
*/
public class UF {
public UF(int n) {
id = new int[n];
sz = new int[n];

for (int i = 0; i < n; i++) {
id[i] = i;
sz[i] = 1;

