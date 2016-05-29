int[][] CakewalkSimulator(int steps, String direction) {
int x=0,y=0,step=steps;
if(steps<0)
else if(direction.equals(&quot;right&quot;)){
x=1;
y=step;
}
else if(direction.equals(&quot;down&quot;)){
x=step;
y=1;
}
int[][] map = new int[x][y];

