static void acopera(int [][]m, int xa, int ya, int xb, int yb, int lg, int cg){

if((int)Math.abs(xa - xb) == 1){

// pozitia goala este in stanga sus
if(lg % 2 == 0 &amp;&amp; cg % 2 == 0){
m[lg][cg - 1] = m[lg - 1][cg - 1] =  m[lg - 1][cg] = piesa % 10;
piesa++;
}
return;
}

int xm = (xa + xb) / 2, ym = (ya + yb) / 2;

