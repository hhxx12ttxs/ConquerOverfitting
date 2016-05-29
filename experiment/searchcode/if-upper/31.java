public static float round(float lower, float upper, float n) {
if (n > upper) {
return upper;
}
if (n < lower) {
return lower;
}
return n;
}
}

