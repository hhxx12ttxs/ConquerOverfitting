public VectorMath sumar(VectorMath vec) throws DistDimException{
if (dim != vec.dim)
throw new DistDimException(&quot; Distinta Dimension &quot;);

VectorMath aux = new VectorMath(dim);
for (int i=0; i<dim; i++)

