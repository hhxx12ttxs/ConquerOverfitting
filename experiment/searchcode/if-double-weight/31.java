private final int v, w;
private final double weight;

public Edge(int v, int w, double weight)
{
this.v = v;
this.w = w;
public int other(int vertex)
{
if(vertex == v)
return w;
return v;
}

public double weight()
{
return weight;

