double fofupperBound = Function.output(x, Main.upperBound[1]);
double fofXAndY = Function.output(x, Main.startPoint[1]);
if (foflowerBound > fofupperBound &amp;&amp; foflowerBound > fofXAndY) {
Main.upperBound[1] = Main.startPoint[1];
}
double[] midpoint = new double[2];
midpoint[0] = x;
midpoint[1] = (Main.upperBound[1] + Main.lowerBound[1]) / 2;

