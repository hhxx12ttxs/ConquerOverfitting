public double range;
public double minVal;
public double maxVal;
public double midVal;

public PerlinMap(int width,int height){
for(int y = 0; y < height; y++){
double p = PerlinNoise.getPerlin((double)x, (double)y,seed);
values[x][y] = p;
if(minVal >p){p = minVal;}

