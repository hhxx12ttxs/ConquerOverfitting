qmin1index = keyTimes.length - 1;
}

double deltan = 1;
double deltanmin1 = 1;
if (keyTimes[q1index] - keyTimes[q0index] > 0 &amp;&amp; keyTimes[q0index] - keyTimes[qmin1index] > 0)
deltanmin1 = deltan;
}


double mul0 = 2 * deltanmin1 / (deltanmin1 + deltan);
double mul1 = 2 * deltan / (deltanmin1 + deltan);

