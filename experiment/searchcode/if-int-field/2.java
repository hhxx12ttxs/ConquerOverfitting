int h = sc.nextInt();
if (w == 0 &amp;&amp; h == 0)
break;
int[][] var = new int[w - 1][h];
int[][] hor = new int[w][h - 1];
hor[j][i] = sc.nextInt();
}
for (int i = 0; i < w - 1; i++)
var[i][h - 1] = sc.nextInt();
int[][] field = new int[w][h];

