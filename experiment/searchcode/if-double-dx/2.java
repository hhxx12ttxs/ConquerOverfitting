if (x >= 0) {
double dx = 0.0;
int t = x;
while (t != 0) {
dx = dx * 10 + t % 10;
t /= 10;
}
return dx == (double)x;
}
return false;
}
}

