fe_sub.fe_sub(check,vxx,u);    /* vx^2-u */
if (fe_isnonzero.fe_isnonzero(check) != 0) {
fe_add.fe_add(check,vxx,u);  /* vx^2+u */
if (fe_isnonzero.fe_isnonzero(check) != 0) return -1;
fe_mul.fe_mul(h.X,h.X,sqrtm1);
}

if (fe_isnegative.fe_isnegative(h.X) == ((s[31] >>> 7) &amp; 0x01)) {

