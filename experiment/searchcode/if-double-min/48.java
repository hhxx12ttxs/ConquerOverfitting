private static final long serialVersionUID = -7894056466361895473L;

public double nextDouble(double min, double max)
{
if (min == max)
return min;
public double clamp(double value, double min, double max)
{
if (value < min)
value = min;
if (value > max)
value = max;

return value;
}

}

