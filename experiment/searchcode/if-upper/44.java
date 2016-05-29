if (o1.lower > o2.lower) {
return 1;
}
if (o1.upper < o2.upper) {
return -1;
}
if (o1.upper > o2.upper) {
return 1;
}
return 0;
}
});

int count = 0;
int currentUpper = 0;

