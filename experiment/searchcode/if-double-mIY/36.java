p22 = p2.y < miY;
p23 = p2.y > maY;

// trivial acceptance - contained within the view port
if (!(p10 || p11 || p12 || p13 || p20 || p21 || p22 || p23)) {
} else if (p12) {
p1.x = calculateXintercept(p1, p2, miY);
p1.y = miY;
} else if (p13) {
p1.x = calculateXintercept(p1, p2, maY);

