// get today&#39;s timezone standard offset
long standardOffset = dtz.getStandardOffset(now);
long standardOffsetPositive = (standardOffset < 0 ? standardOffset*-1 : standardOffset);
int hours = (int)(standardOffsetPositive/1000/60/60);

