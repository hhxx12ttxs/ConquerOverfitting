for (int i = 0; !needToNormalize &amp;&amp; i < vars.length; i++)
if (vars[i].equals(quantFormula.boundVariable()))
needToNormalize = true;

if (needToNormalize) quantFormula.normalize();
return null;

