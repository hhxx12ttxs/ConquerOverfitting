public int[] calculate(Type map[][], Creature parent) {
int toReturn[] = new int[5];
int i = 0;
if(prevX == parent.x &amp;&amp; prevY == parent.y)
prevX = parent.x;
prevY = parent.y;

if(i != -1)
toReturn[i] = 100;
return toReturn;
}

public double getMultiplyer() {

