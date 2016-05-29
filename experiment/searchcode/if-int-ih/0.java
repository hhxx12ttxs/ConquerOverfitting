private int[][] colors;

@Override
public void generate(int n) {
colors = new int[n][3];

double h;
int x, ih;
x = (int) (255 * (1 - Math.abs((h % 2) - 1)));
ih = (int) h;

colors[i][0] = (ih == 0 || ih == 5) ? 255 : ((ih == 1 || ih == 4) ? x : 0); //r

