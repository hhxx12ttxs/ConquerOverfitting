return;
}
double hourDegree;
if (hour == 12) {
hour = 0;
}
double minRatio = min / 60.0;
hourDegree = (hour + minRatio) * 30;
double minDegree = min * 6;
double diff = Math.abs(hourDegree - minDegree);

