StringBuilder builder = new StringBuilder();

if (range[START] == Double.MIN_VALUE) {
builder.append(LESS_THAN);
} else {
range[START] = new Double(startValue);
range[END] = Double.MAX_VALUE;
} else if (token.indexOf(HYPHEN) != -1) {

