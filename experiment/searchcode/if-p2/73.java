return false;
}
if (p1.x == 0) {
if (p.y > p2.y) {
while (p.y > ++p2.y) {
pp = b.getPiece(p2);
if (pp != null) {
return false;
}
}
return true;
}

else if (p.y < p2.y) {

