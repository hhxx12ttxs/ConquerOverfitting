final double groups = list.length;
final double randomValue = Math.random();
for (int i=0 ; i < groups ; i++) {
double max = (double)(i + 1) * ((double)1 / groups);
if (randomValue <= max) {
value = list[i];
break;
}
}
return value;
}
}

