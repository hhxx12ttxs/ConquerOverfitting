long nextX = (x + begin / x) / 2;
if (nextX != x) {
search(begin, begin, nextX);
}
begin++;
high = Math.min(high, end);
long nextX = (x + div) / 2;
if (nextX != x) {
search(begin, high, nextX);
}
begin = high + 1;
}
}
}

