Object[] param = (Object[]) parameters;
if (param.length == 3 &amp;&amp; param[2] instanceof int[][][]) {
int nVars = (Integer) param[0]; // the first nVars variables in &#39;all&#39; must be the sequence variables
return new MultiCostRegular(vs, z, pi, csts, solver);
}
} else if (param.length == 2) {
int nVars = (Integer) param[0]; // the first nVars variables in &#39;all&#39; must be the sequence variables

