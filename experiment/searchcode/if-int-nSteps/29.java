public int nsteps;
public double stepamt;

public int curstep;

public static final String[] pnames = {&quot;alpha&quot;, &quot;beta&quot;, &quot;q&quot;, &quot;rho&quot;, &quot;num_ants&quot;};
public Variator (String name, double s, double e, int steps) {
name = name.toLowerCase();
if (name.equals(pnames[0])) param = 0;
if (name.equals(pnames[1])) param = 1;

