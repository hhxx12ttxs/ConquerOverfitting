public class Config {
// Frequency
public double sxz;
public double sy;

// Density
public int cutoff;
} else {
sxz = 200;
}

if(c.containsKey(&quot;sy&quot;)) {
sy = Double.parseDouble(c.get(&quot;sy&quot;).toString());

