System.out.println(pr);
pr = &quot;&quot;;
for (i = 0; i < dim; i++) {

int h = checkHeight(dim-1, i, -1, 0);
pr += h;
if (i != dim - 1) {
for (i = 0; i < dim; i++) {

int h = checkHeight(i, 0, 0, 1);
pr += h;
if (i != dim - 1) {
pr += &quot;,&quot;;

