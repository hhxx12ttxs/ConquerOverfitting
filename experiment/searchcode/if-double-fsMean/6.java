* <p>From the analytical expression, we can compute two primitives :
* <pre>
*     If2  (t) = &amp;int; f<sup>2</sup>  = a<sup>2</sup> &amp;times; [t + S (t)] / 2
*     If&#39;2 (t) = &amp;int; f&#39;<sup>2</sup> = a<sup>2</sup> &amp;omega;<sup>2</sup> &amp;times; [t - S (t)] / 2
double fcMean = 0.0;
double fsMean = 0.0;

double currentX = observations[0].getX();
double currentY = observations[0].getY();

