double tend = xValues[xValues.length - 1];

if (first20 <= last20) {// positive average slope
pod[0] = -Math.exp((tstart * Math.log(fudgec) - tend * Math.log(last20 - first20 + fudgec)) / (tstart - tend));

} else {// if concave up, positive slope

