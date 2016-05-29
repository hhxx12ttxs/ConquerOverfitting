minDiff = max( (x - (i)/n ), minDiff );
//minDiff = max( Math.sqrt(n) * data[i] * (i/n - data[i]), minDiff );
}
double diff;
if(maxDiff > minDiff)
//minDiff = max( Math.sqrt(n) * data[i] * (i/n - data[i]), minDiff );
}

double diff;
if(maxDiff > minDiff)
diff = maxDiff;
else
diff = minDiff;

System.out.println(&quot;2 - Kolmogorov-Smirnov Triangular Test: &quot; + minDiff + &quot; &quot; + 1.358);

