double cos = (v1.x * v2.x + v1.y * v2.y) / l1 / l2;
double angle = Math.acos(cos);
if (cos >= 1) {
return 0;
}
double sin = v1.x * v2.y - v1.y * v2.x;
if (sin > 0) {
return angle;
} else {
return -angle;
}
}
}

