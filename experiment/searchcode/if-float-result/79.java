public int compareTo(JavaFloat other) {
if (floatValue < other.floatValue)
return -1;
if (floatValue > other.floatValue)
int result = 1;
result = prime * result + Float.floatToIntBits(floatValue);
return result;

