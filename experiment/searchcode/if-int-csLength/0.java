CharSequence cs = getSubSequence(o, Math.min(len, o + BUFFER_SIZE));
int csLength = cs.length();

for (int i = 0; i < csLength; i++) {
if (cs.charAt(i) == c) {
return o + i;
}
}

o += csLength;
}

return -1;
}
}

